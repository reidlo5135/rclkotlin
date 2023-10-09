package net.wavem.rclkotlin.rosidl.infra

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import id.jrosmessages.Message
import net.wavem.rclkotlin.rosidl.application.RCLBufferReadingService
import net.wavem.rclkotlin.rosidl.domain.RCLBufferPrimitiveType
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class RCLMessageSerialization {

    private val rclBufferReadingService : RCLBufferReadingService = RCLBufferReadingService()

    inline fun <reified M : Message> read(data : ByteArray) : M? {
        try {
            val clazz : KClass<M> = M::class
            val constructors : Collection<KFunction<M>> = clazz.constructors
            val clazzName : String = clazz.simpleName.toString()
            println("clazzName : $clazzName")

            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            val instanceJson : JsonObject = JsonObject()

            for (constructor in constructors) {
                val parameters : List<KParameter> = constructor.parameters
                if (parameters.isEmpty()) continue

                val args : MutableList<Any?> = mutableListOf()

                var paramName : String = ""
                var cBuffCount : Int = 0
                val cInstanceJson : JsonObject = JsonObject()

                for((index, param) in parameters.withIndex()) {
                    paramName = param.name.toString()
                    println("$clazzName paraName : $paramName")

                    val type : String = param.type.toString()
                    println("$clazzName type : $type")

                    val rclBufferPrimitiveType : RCLBufferPrimitiveType? = RCLBufferPrimitiveType.TYPE_NAME_MAP[type]

                    var cParamName : String = ""
                    val dInstanceJson : JsonObject = JsonObject()

                    if (rclBufferPrimitiveType == null) {
                        val cJClazz : Class<*> = Class.forName(type)
                        val cKClazz : KClass<out Any> = cJClazz.kotlin
                        val cKClazzName : String = cKClazz.simpleName.toString()
                        println("cKClazzName : $cKClazzName")

                        val cAnalyzed : Any? = this.readParameters(parameters, index, data, 0)

                        if (cAnalyzed == null) {
                            val cArgs : MutableList<Any?> = mutableListOf()
                            println("$cKClazzName cArgs : $cArgs")

                            val cKConstructors : Collection<KFunction<Any>> = cKClazz.constructors

                            for (cKConstructor in cKConstructors) {
                                val cParameters : List<KParameter> = cKConstructor.parameters
                                if (cParameters.isEmpty()) continue

                                var dBuffCount : Int = 0

                                for((cIndex, cParam) in cParameters.withIndex()) {
                                    cParamName = cParam.name.toString()
                                    println("$cKClazzName[$cIndex] paraName : $cParamName")

                                    val cType : String = cParam.type.toString()
                                    println("$cKClazzName[$cIndex] cType : $cType")

                                    val cRclBufferPrimitiveType : RCLBufferPrimitiveType? = RCLBufferPrimitiveType.TYPE_NAME_MAP[cType]

                                    var dParamName : String = ""

                                    if (cRclBufferPrimitiveType == null) {
                                        val dJClazz : Class<*> = Class.forName(cType)
                                        val dKClazz : KClass<out Any> = dJClazz.kotlin
                                        val dKClazzName : String = dKClazz.simpleName.toString()
                                        println("dKClazzName : $dKClazzName")

                                        val dKConstructors : Collection<KFunction<Any>> = dKClazz.constructors

                                        for (dKConstructor in dKConstructors) {
                                            val dParameters : List<KParameter> = dKConstructor.parameters
                                            if(dParameters.isEmpty()) continue

                                            val dArgs : MutableList<Any?> = mutableListOf()

                                            for ((dIndex, dParam) in dParameters.withIndex()) {
                                                dParamName = dParam.name.toString()
                                                println("$dKClazzName[$dIndex] paramName : $dParamName")

                                                val dType : String = dParam.type.toString()
                                                println("$dKClazzName[$dIndex] dType : $dType")

                                                val dRclBufferPrimitiveType : RCLBufferPrimitiveType? = RCLBufferPrimitiveType.TYPE_NAME_MAP[dType]

                                                if (dRclBufferPrimitiveType != null) {
                                                    var position : Int = 0
                                                    position = if (dIndex == 0) {
                                                        0
                                                    } else {
                                                        dBuffCount
                                                    }

                                                    dArgs.add(this.readParameters(dParameters, dIndex, data, position))

                                                    when (dRclBufferPrimitiveType) {
                                                        RCLBufferPrimitiveType.INT -> {
                                                            dBuffCount += Int.SIZE_BYTES
                                                        }
                                                        else -> {}
                                                    }

                                                    println("$dKClazzName[$dIndex] dBuffCount : $dBuffCount")
                                                    println("$dKClazzName[$dIndex] dArgs : $dArgs")
                                                }
                                            }

                                            if (dArgs.size == dParameters.size) {
                                                cBuffCount += dBuffCount
                                                val dInstance : Any = dKConstructor.call(*dArgs.toTypedArray())
                                                println("$dKClazzName dInstance : $dInstance")
                                                cInstanceJson.add(cParamName, Gson().toJsonTree(dInstance))
//                                                cArgs.add(dInstance)
                                            }
                                        }
                                    } else {
                                        println("$cKClazzName cType : $cType")
                                        println("$cKClazzName cBuffCount : $cBuffCount")

                                        when (cRclBufferPrimitiveType) {
                                            RCLBufferPrimitiveType.STRING -> {
                                                println("$cKClazzName cParam ${RCLBufferPrimitiveType.STRING} first index : $cIndex, buffCount : $cBuffCount")
                                                val str : String = this.readParameters(cParameters, cIndex, data, cBuffCount).toString()
                                                val strSize : Int = str.toByteArray(Charsets.UTF_8).size
                                                println("$cKClazzName cParam ${RCLBufferPrimitiveType.STRING} str : $str, len : $strSize")
                                                cInstanceJson.addProperty(cParamName, str.replace("\u0000", ""))
                                                cArgs.add(cInstanceJson)
                                                cBuffCount += strSize * 2
                                            }
                                            RCLBufferPrimitiveType.BYTE -> {
                                                if (cIndex == PARAMETERS_FIRST_INDEX) {
                                                    cArgs.add(this.readParameters(cParameters, cIndex, data, cBuffCount))
                                                    println("$cKClazzName cParam ${RCLBufferPrimitiveType.BYTE} first index : $cIndex, buffCount : $cBuffCount")
                                                } else {
                                                    cBuffCount += Byte.SIZE_BYTES
                                                    cArgs.add(this.readParameters(cParameters, cIndex, data, cBuffCount))
                                                    println("$cKClazzName cParam ${RCLBufferPrimitiveType.BYTE} other index : $cIndex, buffCount : $cBuffCount")
                                                }
                                            }
                                            RCLBufferPrimitiveType.USHORT -> {
                                                if (cIndex == PARAMETERS_FIRST_INDEX) {
                                                    println("$cKClazzName cParam ${RCLBufferPrimitiveType.USHORT} first index : $cIndex, buffCount : $cBuffCount")
                                                    cArgs.add(this.readParameters(cParameters, cIndex, data, cBuffCount))
                                                } else {
                                                    cBuffCount += UShort.SIZE_BYTES
                                                    cArgs.add(this.readParameters(cParameters, cIndex, data, cBuffCount))
                                                    println("$cKClazzName cParam ${RCLBufferPrimitiveType.USHORT} other index : $cIndex, buffCount : $cBuffCount")
                                                }
                                            }
                                            else -> {

                                            }
                                        }
                                    }
                                }

                                if (cArgs.size == cParameters.size) {
                                    val cInstance : Any = cKConstructor.call(*cArgs.toTypedArray())
                                    println("$cKClazzName cInstance : $cInstance")
                                    println("$cKClazzName cInstanceJson : $cInstanceJson")
                                    args.add(cInstance)
                                }
                            }
                            println("$cKClazzName cArgs : $cArgs")
                        } else {

                        }
                    } else {

                    }
                    println("$clazzName args : $args")
                }

                if(args.size == parameters.size) {
                    val instance : M = constructor.call(*args.toTypedArray())
                    println("$clazzName instance : $instance")

                    return instance
                }
            }
        } catch (ioe : IOException) {
            ioe.printStackTrace()
        }
        return null
    }

    fun buildInstance(paramKey : String, instance : Any) : String {
        val gson : Gson = Gson()
        val instanceJsonObject : JsonObject = JsonObject()
        instanceJsonObject.add(paramKey, gson.toJsonTree(instance))

        return gson.toJson(instanceJsonObject)
    }

    fun readParameters(parameters : List<KParameter>, paramIndex : Int, data: ByteArray, position: Int) : Any? {
        try {
            println("analyzeCustomClazz parameters : $parameters")
            println("analyzeCustomClazz paramIndex : $paramIndex")

            val type : String = parameters[paramIndex].type.toString()
            println("analyzeCustomClazz type : $type")

            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)
            println("analyzeCustomClazz buf position : $position")
            buf.position(position)
            println("analyzeCustomClazz buf remained : ${buf.remaining()}")

            val rclBufferPrimitiveType : RCLBufferPrimitiveType? = RCLBufferPrimitiveType.TYPE_NAME_MAP[type]

            if (rclBufferPrimitiveType != null) {
                when (rclBufferPrimitiveType) {
                    RCLBufferPrimitiveType.STRING -> {
                        return rclBufferReadingService.readString(buf)
                    }
                    RCLBufferPrimitiveType.BYTE -> {
                        return rclBufferReadingService.readByte(buf)
                    }
                    RCLBufferPrimitiveType.UBYTE -> {
                        return rclBufferReadingService.readUByte(buf)
                    }
                    RCLBufferPrimitiveType.INT -> {
                        return rclBufferReadingService.readInt(buf)
                    }
                    RCLBufferPrimitiveType.SHORT -> {
                        return rclBufferReadingService.readShort(buf)
                    }
                    RCLBufferPrimitiveType.USHORT -> {
                        return rclBufferReadingService.readUShort(buf)
                    }
                    RCLBufferPrimitiveType.LONG -> {
                        return rclBufferReadingService.readLong(buf)
                    }
                    RCLBufferPrimitiveType.DOUBLE -> {
                        return rclBufferReadingService.readDouble(buf)
                    }
                    RCLBufferPrimitiveType.FLOAT -> {
                        return rclBufferReadingService.readFloat(buf)
                    }
                    RCLBufferPrimitiveType.DOUBLEARRAY -> {
                        val size : Int = buf.getInt()
                        val array : DoubleArray = DoubleArray(size)
                        for(i in 0..<size) {
                            array[i] = buf.getDouble()
                        }
                    }
                    RCLBufferPrimitiveType.FLOATARRAY -> {
                        val size : Int = buf.getInt()
                        val array : FloatArray = FloatArray(size)
                        for(i in 0..<size) {
                            array[i] = buf.getFloat()
                        }
                    }
                    RCLBufferPrimitiveType.BOOLEAN -> {
                        return buf.get() != 0.toByte()
                    }
                    RCLBufferPrimitiveType.BOOLEANARRAY -> {
                        val size : Int = buf.getInt()
                        val array : BooleanArray = BooleanArray(size)
                        for (i in 0..size) {
                            array[i] = buf.get() != 0.toByte()
                        }
                    }
                }
            } else {
                return null
            }
        } catch (ioe : IOException) {
            ioe.printStackTrace()
        }

        return null
    }

    companion object {
        const val PARAMETERS_FIRST_INDEX : Int = 0
    }
}