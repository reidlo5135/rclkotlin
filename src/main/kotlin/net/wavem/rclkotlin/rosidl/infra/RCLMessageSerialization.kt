package net.wavem.rclkotlin.rosidl.infra

import com.google.gson.Gson
import com.google.gson.JsonObject
import id.jros2client.impl.rmw.DdsNameMapper
import id.jrosmessages.Message
import net.wavem.rclkotlin.rosidl.application.RCLBufferReadingService
import net.wavem.rclkotlin.rosidl.domain.RCLBufferPrimitiveType
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

/**
 * @author reidlo
 * @since 2023.10.07
 */
class RCLMessageSerialization {

    val gson : Gson = Gson()
    private val rclBufferReadingService : RCLBufferReadingService = RCLBufferReadingService()

    inline fun <reified M : Message> read(data : ByteArray) : M? {
        try {
            /**
             * Level : B
             * ClassType : kClass
             * for M class what implements to Message
             */
            val kClass : KClass<M> = M::class
            val kConstructors : Collection<KFunction<M>> = kClass.constructors
            val kClassSimpleName : String = kClass.simpleName.toString()
            println("kClassSimpleName : $kClassSimpleName")

            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            val instanceJson : JsonObject = JsonObject()
            var buffCount : Int = 0

            /**
             * Level : B
             * ClassType : kClass
             * for M's fields class
             */
            for (kConstructor in kConstructors) {
                val kParameters : List<KParameter> = kConstructor.parameters
                if (kParameters.isEmpty()) continue

                val args : MutableList<Any?> = mutableListOf()

                var paramName : String = ""
                var cBuffCount : Int = 0
                val cInstanceJson : JsonObject = JsonObject()

                /**
                 * Level : B
                 * ClassType : kClass
                 * for M's fields class' parameters
                 */
                for((index, param) in kParameters.withIndex()) {
                    paramName = param.name.toString()
                    println("$kClassSimpleName paramName : $paramName")

                    val type : String = param.type.toString()
                    println("$kClassSimpleName type : $type")

                    val rclBufferPrimitiveType : RCLBufferPrimitiveType? = RCLBufferPrimitiveType.TYPE_NAME_MAP[type]

                    var cParamName : String = ""
                    val dInstanceJson : JsonObject = JsonObject()

                    /**
                     * Level : B
                     * ClassType : kClass
                     * when M's fields type is not belongs to kotlin's primitive types. (for Custom Class)
                     */
                    if (rclBufferPrimitiveType == null) {
                        val cJClass : Class<*> = Class.forName(type)
                        val cKClass : KClass<out Any> = cJClass.kotlin
                        val cKClassSimpleName : String = cKClass.simpleName.toString()
                        println("cKClassSimpleName : $cKClassSimpleName")

                        val cAnalyzed : Any? = this.readParameters(kParameters, index, data, 0)

                        if (cAnalyzed == null) {
                            val cArgs : MutableList<Any?> = mutableListOf()
                            println("$cKClassSimpleName cArgs : $cArgs")

                            val cKConstructors : Collection<KFunction<Any>> = cKClass.constructors

                            /**
                             * Level : C
                             * ClassType : cKClass
                             * for M's fields custom classes
                             */
                            for (cKConstructor in cKConstructors) {
                                val cParameters : List<KParameter> = cKConstructor.parameters
                                if (cParameters.isEmpty()) continue

                                var dBuffCount : Int = 0

                                /**
                                 * Level : C
                                 * ClassType : cKClass
                                 * for M's fields custom classes' parameters
                                 */
                                for((cIndex, cParam) in cParameters.withIndex()) {
                                    cParamName = cParam.name.toString()
                                    println("$cKClassSimpleName[$cIndex] paramName : $cParamName")

                                    val cType : String = cParam.type.toString()
                                    println("$cKClassSimpleName[$cIndex] cType : $cType")

                                    val cRclBufferPrimitiveType : RCLBufferPrimitiveType? = RCLBufferPrimitiveType.TYPE_NAME_MAP[cType]

                                    var dParamName : String = ""

                                    /**
                                     * Level : C
                                     * ClassType : cKClass
                                     * when M's fields custom classes' parameters type is not belongs to kotlin's primitive types. (for Custom Class)
                                     */
                                    if (cRclBufferPrimitiveType == null) {
                                        val dJClass : Class<*> = Class.forName(cType)
                                        val dKClass : KClass<out Any> = dJClass.kotlin
                                        val dKClassSimpleName : String = dKClass.simpleName.toString()
                                        println("dKClassSimpleName : $dKClassSimpleName")

                                        val dKConstructors : Collection<KFunction<Any>> = dKClass.constructors

                                        /**
                                         * Level : D
                                         * ClassType : dKClass
                                         * for M's fields custom classes' parameters' classes
                                         */
                                        for (dKConstructor in dKConstructors) {
                                            val dParameters : List<KParameter> = dKConstructor.parameters
                                            if (dParameters.isEmpty()) continue

                                            val dArgs : MutableList<Any?> = mutableListOf()

                                            /**
                                             * Level : D
                                             * ClassType : dKClass
                                             * for M's fields custom classes' parameters' classes' parameters
                                             */
                                            for ((dIndex, dParam) in dParameters.withIndex()) {
                                                dParamName = dParam.name.toString()
                                                println("$dKClassSimpleName[$dIndex] paramName : $dParamName")

                                                val dType : String = dParam.type.toString()
                                                println("$dKClassSimpleName[$dIndex] dType : $dType")

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

                                                    println("$dKClassSimpleName[$dIndex] dBuffCount : $dBuffCount")
                                                    println("$dKClassSimpleName[$dIndex] dArgs : $dArgs")
                                                }
                                            }

                                            /**
                                             * Level : D
                                             * ClassType : dKClass
                                             * calling dKConstructor when building dArgs has been completed.
                                             */
                                            if (dArgs.size == dParameters.size) {
                                                cBuffCount += dBuffCount
                                                val dInstance : Any = dKConstructor.call(*dArgs.toTypedArray())
                                                println("$dKClassSimpleName dInstance : $dInstance")
                                                cInstanceJson.add(cParamName, gson.toJsonTree(dInstance))
                                                println("$cParamName cInstanceJson : $cInstanceJson")
                                            }
                                        }
                                    } else {
                                        /**
                                         * Level : C
                                         * ClassType : cKClass
                                         * when M's fields custom classes' parameters type is belongs to kotlin's primitive types.
                                         */
                                        println("$cKClassSimpleName cType : $cType")
                                        println("$cKClassSimpleName cBuffCount : $cBuffCount")

                                        when (cRclBufferPrimitiveType) {
                                            RCLBufferPrimitiveType.STRING -> {
                                                println("$cKClassSimpleName cParam ${RCLBufferPrimitiveType.STRING} first index : $cIndex, buffCount : $cBuffCount")
                                                val str : String = this.readParameters(cParameters, cIndex, data, cBuffCount).toString()
                                                val strSize : Int = str.toByteArray(Charsets.UTF_8).size
                                                println("$cKClassSimpleName cParam ${RCLBufferPrimitiveType.STRING} str : $str, len : $strSize")
                                                cInstanceJson.addProperty(cParamName, str.replace("\u0000", ""))
                                                cArgs.add(cInstanceJson)
                                                cBuffCount += strSize * 2
                                            }
                                            RCLBufferPrimitiveType.BYTE -> {
                                                if (cIndex == PARAMETERS_FIRST_INDEX) {
                                                    cArgs.add(this.readParameters(cParameters, cIndex, data, cBuffCount))
                                                    println("$cKClassSimpleName cParam ${RCLBufferPrimitiveType.BYTE} first index : $cIndex, buffCount : $cBuffCount")
                                                } else {
                                                    cBuffCount += Byte.SIZE_BYTES
                                                    cArgs.add(this.readParameters(cParameters, cIndex, data, cBuffCount))
                                                    println("$cKClassSimpleName cParam ${RCLBufferPrimitiveType.BYTE} other index : $cIndex, buffCount : $cBuffCount")
                                                }
                                            }
                                            RCLBufferPrimitiveType.USHORT -> {
                                                if (cIndex == PARAMETERS_FIRST_INDEX) {
                                                    println("$cKClassSimpleName cParam ${RCLBufferPrimitiveType.USHORT} first index : $cIndex, buffCount : $cBuffCount")
                                                    cArgs.add(this.readParameters(cParameters, cIndex, data, cBuffCount))
                                                } else {
                                                    cBuffCount += UShort.SIZE_BYTES
                                                    cArgs.add(this.readParameters(cParameters, cIndex, data, cBuffCount))
                                                    println("$cKClassSimpleName cParam ${RCLBufferPrimitiveType.USHORT} other index : $cIndex, buffCount : $cBuffCount")
                                                }
                                            }
                                            else -> {

                                            }
                                        }
                                    }
                                }

                                /**
                                 * Level : C
                                 * ClassType : cKClass
                                 * calling cKConstructor when building cArgs has been completed.
                                 */
                                if (cArgs.size == cParameters.size) {
                                    val cInstance : Any = cKConstructor.call(*cArgs.toTypedArray())
                                    println("$cKClassSimpleName cInstance : $cInstance")

                                    cInstanceJson.add(paramName, gson.toJsonTree(cInstance))

                                    println("$cKClassSimpleName instanceJson : $instanceJson")
                                    println("$cKClassSimpleName cInstanceJson : $cInstanceJson")
                                    args.add(cInstance)
                                }
                            }
                            println("$cKClassSimpleName cArgs : $cArgs")
                        } else {

                        }
                    } else {
                        when (rclBufferPrimitiveType) {
                            RCLBufferPrimitiveType.STRING -> {
                                println("$kClassSimpleName cParam ${RCLBufferPrimitiveType.STRING} first index : $index, buffCount : $buffCount")
                                val str : String = this.readParameters(kParameters, index, data, buffCount).toString()
                                val strSize : Int = str.toByteArray(Charsets.UTF_8).size
                                println("$kClassSimpleName cParam ${RCLBufferPrimitiveType.STRING} str : $str, len : $strSize")
                                args.add(str)
                            }
                            else -> {}
                        }
                    }
                    println("$kClassSimpleName args : $args")
                }
                /**
                 * Level : B
                 * ClassType : kClass
                 * calling cKConstructor when building cArgs has been completed.
                 */
                if(args.size == kParameters.size) {
                    val instance : M = kConstructor.call(*args.toTypedArray())
                    println("$kClassSimpleName instance : $instance")

                    return instance
                }
            }
        } catch (ioe : IOException) {
            ioe.printStackTrace()
        }
        return null
    }

    fun buildInstance(paramKey : String, instance : Any) : String {
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