package net.wavem.rclkotlin.rosidl.infra

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
            println("clazz name : ${clazz.simpleName}")

            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            for (constructor in constructors) {
                val parameters : List<KParameter> = constructor.parameters
                if (parameters.isEmpty()) continue

                val args : MutableList<Any?> = mutableListOf()
                var cBuffCount : Int = 0

                for((index, param) in parameters.withIndex()) {
                    val type : String = param.type.toString()
                    println("type : $type")

                    val rclBufferPrimitiveType : RCLBufferPrimitiveType? = RCLBufferPrimitiveType.TYPE_NAME_MAP[type]

                    if (rclBufferPrimitiveType == null) {
                        val cJClazz : Class<*> = Class.forName(type)
                        val cKClazz : KClass<out Any> = cJClazz.kotlin
                        println("cKClazz : $cKClazz")

                        val cAnalyzed : Any? = analyzeCustomClass(parameters, index, data, 0)

                        if (cAnalyzed == null) {
                            val cArgs : MutableList<Any?> = mutableListOf()
                            println("cArgs : $cArgs")

                            val cKConstructors : Collection<KFunction<Any>> = cKClazz.constructors

                            for (cKConstructor in cKConstructors) {
                                val cParameters : List<KParameter> = cKConstructor.parameters
                                if (cParameters.isEmpty()) continue

                                for((cIndex, cParam) in cParameters.withIndex()) {
                                    val cType : String = cParam.type.toString()
                                    println("cType : $cType")

                                    val cRclBufferPrimitiveType : RCLBufferPrimitiveType? = RCLBufferPrimitiveType.TYPE_NAME_MAP[cType]

                                    var dBuffCount : Int = 0

                                    if (cRclBufferPrimitiveType == null) {
                                        val dJClazz : Class<*> = Class.forName(cType)
                                        val dKClazz : KClass<out Any> = dJClazz.kotlin
                                        val dKConstructors : Collection<KFunction<Any>> = dKClazz.constructors

                                        for (dKConstructor in dKConstructors) {
                                            val dParameters : List<KParameter> = dKConstructor.parameters
                                            if(dParameters.isEmpty()) continue

                                            val dArgs : MutableList<Any?> = mutableListOf()

                                            for ((dIndex, dParam) in dParameters.withIndex()) {
                                                val dType : String = dParam.type.toString()
                                                println("dType : $dType")
                                                println("dIndex : $dIndex")

                                                val dRclBufferPrimitiveType : RCLBufferPrimitiveType? = RCLBufferPrimitiveType.TYPE_NAME_MAP[dType]

                                                if (dRclBufferPrimitiveType != null) {
                                                    var position : Int = 0
                                                    position = if (dIndex == 0) {
                                                        0
                                                    } else {
                                                        dBuffCount
                                                    }

                                                    dArgs.add(analyzeCustomClass(dParameters, dIndex, data, position))

                                                    when (dRclBufferPrimitiveType) {
                                                        RCLBufferPrimitiveType.INT -> {
                                                            dBuffCount += Int.SIZE_BYTES
                                                        }
                                                        else -> {}
                                                    }

                                                    println("dBuffCount : $dBuffCount")
                                                    println("dArgs : $dArgs")
                                                }
                                            }

                                            if (dArgs.size == dParameters.size) {
                                                cBuffCount += dBuffCount
                                                val dInstance = dKConstructor.call(*dArgs.toTypedArray())
                                                println("dInstance : $dInstance")
                                                cArgs.add(dInstance)
                                            }
                                        }
                                    } else {
                                        println("ccType : $cType")
                                        println("cBuffCount : $cBuffCount")
                                        buf.position(cBuffCount)

                                        when (cRclBufferPrimitiveType) {
                                            RCLBufferPrimitiveType.STRING -> {
                                                cArgs.add(analyzeCustomClass(cParameters, cIndex, data, cBuffCount))
                                                cBuffCount += 8
                                            }
                                            RCLBufferPrimitiveType.BYTE -> {
                                                if (cIndex == 0) {
                                                    cArgs.add(analyzeCustomClass(cParameters, cIndex, data, cBuffCount))
                                                    cBuffCount += Byte.SIZE_BYTES
                                                    println("cParam byte first index : $cIndex, buffCount : $cBuffCount")
                                                } else {
                                                    cBuffCount += Byte.SIZE_BYTES
                                                    cArgs.add(analyzeCustomClass(cParameters, cIndex, data, cBuffCount))
                                                    println("cParam byte other index : $cIndex, buffCount : $cBuffCount")
                                                }
                                            }
                                            RCLBufferPrimitiveType.USHORT -> {
                                                if (cIndex == 0) {
                                                    cArgs.add(analyzeCustomClass(cParameters, cIndex, data, cBuffCount))
//                                                    cBuffCount += UShort.SIZE_BYTES
                                                    println("cParam uShort first index : $cIndex, buffCount : $cBuffCount")
                                                } else {
                                                    cBuffCount += UShort.SIZE_BYTES
                                                    cArgs.add(analyzeCustomClass(cParameters, cIndex, data, cBuffCount))
                                                    println("cParam uShort other index : $cIndex, buffCount : $cBuffCount")
                                                }
                                            }
                                            else -> {

                                            }
                                        }
                                    }
                                }

                                if (cArgs.size == cParameters.size) {
                                    val cInstance = cKConstructor.call(*cArgs.toTypedArray())
                                    println("cInstance : $cInstance")
                                    args.add(cInstance)
                                }
                            }
                            println("cArgs : $cArgs")
                        } else {
                            val ccArgs : MutableList<Any?> = mutableListOf()
                            println("cBufCount : $cBuffCount")
                            println("ccKClazz Name : ${cKClazz.qualifiedName}")

                            val ccKConstructors : Collection<KFunction<Any>> = cKClazz.constructors

                            for (ccKConstructor in ccKConstructors) {
                                val ccParameters : List<KParameter> = ccKConstructor.parameters
                                if (ccParameters.isEmpty()) continue
                                println("ccParameters : $ccParameters")

                                for((ccIndex, ccParam) in ccParameters.withIndex()) {
                                    val ccType : String = ccParam.type.toString()
                                    println("ccType : $ccType")
                                    var ccBuffCount : Int = 0
                                    val ccRclBufferPrimitiveType : RCLBufferPrimitiveType? = RCLBufferPrimitiveType.TYPE_NAME_MAP[ccType]
                                    when (ccRclBufferPrimitiveType) {
                                        RCLBufferPrimitiveType.BYTE -> {
                                            if (ccIndex == 0) {
                                                ccBuffCount = cBuffCount
                                                println("ccParam byte first index : $ccIndex, buffCount : $ccBuffCount")
                                            } else {
                                                ccBuffCount += cBuffCount + Byte.SIZE_BYTES
                                                println("ccParam byte other index : $ccIndex, buffCount : $ccBuffCount")
                                            }
                                        }
                                        RCLBufferPrimitiveType.USHORT -> {
                                            if (ccIndex == 0) {
                                                ccBuffCount = cBuffCount
                                                println("ccParam uShort first index : $ccIndex, buffCount : $ccBuffCount")
                                            } else {
                                                ccBuffCount += cBuffCount + UShort.SIZE_BYTES
                                                println("ccParam uShort other index : $ccIndex, buffCount : $ccBuffCount")
                                            }
                                        }
                                        else -> {}
                                    }
                                    println("ccBufCount : $ccBuffCount")
                                    println("ccIndex : $ccIndex")
                                    ccArgs.add(analyzeCustomClass(ccParameters, ccIndex, data, ccBuffCount))
                                    println("ccArgs[$ccIndex] : ${ccArgs[ccIndex]}")
                                }

                                if(ccArgs.size == ccParameters.size) {
                                    println("ccArgs : $ccArgs")
                                    val ccInstance = ccKConstructor.call(*ccArgs.toTypedArray())
                                    println("ccInstance : $ccInstance")
                                    args.add(ccInstance)
                                }
                            }
                        }
                    } else {

                    }
                    println("args : $args")
                }

                if(args.size == parameters.size) {
                    val instance = constructor.call(*args.toTypedArray())
                    println("instance : $instance")

                    return instance
                }
            }
        } catch (ioe : IOException) {
            ioe.printStackTrace()
        }
        return null
    }

    fun analyzePrimitiveClass(clazz: KClass<out Any>, data: ByteArray, position : Int) : Any? {
        try {
            println("analyzePrimitiveClazz name : ${clazz.qualifiedName}")
            println("analyzePrimitiveClazz position : $position")

            val buf: ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            buf.position(position)

            if (clazz == String::class) {
                val len : Int = buf.getInt()
                val stringBytes = ByteArray(position)
                buf.get(stringBytes)
                return String(stringBytes, Charsets.UTF_8)
            } else if (clazz == Int::class) {
                return buf.getInt()
            }
        } catch (ioe : IOException) {
            ioe.printStackTrace()
        }

        return null
    }

    fun analyzeCustomClass(parameters : List<KParameter>, paramIndex : Int, data: ByteArray, position: Int) : Any? {
        try {
            println("analyzeCustomClazz parameters : $parameters")
            println("analyzeCustomClazz paramIndex : $paramIndex")

            val type : String = parameters[paramIndex].type.toString()
            println("analyzeCustomClazz type : $type")

            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)
            buf.position(position)
            println("analyzeCustomClazz buf remained : ${buf.remaining()}")

            val rclBufferPrimitiveType : RCLBufferPrimitiveType? = RCLBufferPrimitiveType.TYPE_NAME_MAP[type]

            if (rclBufferPrimitiveType != null) {
                when (rclBufferPrimitiveType) {
                    RCLBufferPrimitiveType.STRING -> {
                        val strData : String = if (paramIndex != parameters.lastIndex) {
                            rclBufferReadingService.readString(buf)
                        } else if (paramIndex != parameters.lastIndex) {
                            rclBufferReadingService.readString(paramIndex, buf)
                        } else {
                            rclBufferReadingService.readString(buf)
                        }
                        return strData
                    }
                    RCLBufferPrimitiveType.BYTE -> {
                        val bufByte : Byte = if (paramIndex == PARAMETERS_FIRST_INDEX) {
                            rclBufferReadingService.readByte(buf)
                        } else if (paramIndex != parameters.lastIndex) {
                            rclBufferReadingService.readByte(paramIndex, buf)
                        } else {
                            if (buf.remaining() > Byte.SIZE_BYTES) {
                                println("${RCLBufferPrimitiveType.BYTE} remained buf is over ${Byte.SIZE_BYTES}")
                                buf.position(Byte.SIZE_BYTES * paramIndex)
                            }
                            rclBufferReadingService.readByte(buf)
                        }

                        return bufByte
                    }
                    RCLBufferPrimitiveType.UBYTE -> {
                        val bufUByte : UByte = if (paramIndex == PARAMETERS_FIRST_INDEX) {
                            rclBufferReadingService.readUByte(buf)
                        } else if (paramIndex != parameters.lastIndex) {
                            rclBufferReadingService.readUByte(paramIndex, buf)
                        } else {
                            if (buf.remaining() > UByte.SIZE_BYTES) {
                                println("${RCLBufferPrimitiveType.UBYTE} remained buf is over $UByte.SIZE_BYTES")
                                buf.position(UByte.SIZE_BYTES * paramIndex)
                            }
                            rclBufferReadingService.readUByte(buf)
                        }

                        return bufUByte
                    }
                    RCLBufferPrimitiveType.INT -> {
                        val bufInt : Int = if (paramIndex == PARAMETERS_FIRST_INDEX) {
                            rclBufferReadingService.readInt(buf)
                        } else if (paramIndex != parameters.lastIndex) {
                            rclBufferReadingService.readInt(paramIndex, buf)
                        } else {
                            if (buf.remaining() > Int.SIZE_BYTES) {
                                println("${RCLBufferPrimitiveType.INT} remained buf is over ${Int.SIZE_BYTES}")
                                buf.position(Int.SIZE_BYTES * paramIndex)
                            }
                            rclBufferReadingService.readInt(buf)
                        }

                        return bufInt
                    }
                    RCLBufferPrimitiveType.SHORT -> {
                        val bufShort : Short = if (paramIndex == PARAMETERS_FIRST_INDEX) {
                            rclBufferReadingService.readShort(buf)
                        } else if (paramIndex != parameters.lastIndex) {
                            rclBufferReadingService.readShort(paramIndex, buf)
                        } else {
                            if (buf.remaining() > Short.SIZE_BYTES) {
                                println("${RCLBufferPrimitiveType.SHORT} remained buf is over ${Short.SIZE_BYTES}")
                                buf.position(Short.SIZE_BYTES * paramIndex)
                            }
                            rclBufferReadingService.readShort(buf)
                        }

                        return bufShort
                    }
                    RCLBufferPrimitiveType.USHORT -> {
                        val bufUShort : UShort = if (paramIndex == PARAMETERS_FIRST_INDEX) {
                            rclBufferReadingService.readUShort(buf)
                        } else if (paramIndex != parameters.lastIndex) {
                            rclBufferReadingService.readUShort(paramIndex, buf)
                        } else {
                            if (buf.remaining() > UShort.SIZE_BYTES) {
                                println("${RCLBufferPrimitiveType.USHORT} remained buf is over ${UShort.SIZE_BYTES}")
//                                buf.position(UShort.SIZE_BYTES * paramIndex)
                            }
                            rclBufferReadingService.readUShort(paramIndex, buf)
                        }
                        println("analyzeCustomClazz UShort : $bufUShort")
                        return bufUShort
                    }
                    RCLBufferPrimitiveType.LONG -> {
                        val bufLong : Long = if (paramIndex == PARAMETERS_FIRST_INDEX) {
                            rclBufferReadingService.readLong(buf)
                        } else if (paramIndex != parameters.lastIndex) {
                            rclBufferReadingService.readLong(paramIndex, buf)
                        } else {
                            if (buf.remaining() > Long.SIZE_BYTES) {
                                println("${RCLBufferPrimitiveType.LONG} remained buf is over ${Long.SIZE_BYTES}")
                                buf.position(Long.SIZE_BYTES * paramIndex)
                            }
                            rclBufferReadingService.readLong(buf)
                        }

                        return bufLong
                    }
                    RCLBufferPrimitiveType.DOUBLE -> {
                        val bufDouble : Double = if (paramIndex == PARAMETERS_FIRST_INDEX) {
                            rclBufferReadingService.readDouble(buf)
                        } else if (paramIndex != parameters.lastIndex) {
                            rclBufferReadingService.readDouble(paramIndex, buf)
                        } else {
                            if (buf.remaining() > Double.SIZE_BYTES) {
                                println("${RCLBufferPrimitiveType.DOUBLE} remained buf is over ${Double.SIZE_BYTES}")
                                buf.position(Double.SIZE_BYTES * paramIndex)
                            }
                            rclBufferReadingService.readDouble(buf)
                        }

                        return bufDouble
                    }
                    RCLBufferPrimitiveType.FLOAT -> {
                        val bufFloat : Float = if (paramIndex == PARAMETERS_FIRST_INDEX) {
                            rclBufferReadingService.readFloat(buf)
                        } else if (paramIndex != parameters.lastIndex) {
                            rclBufferReadingService.readFloat(paramIndex, buf)
                        } else {
                            if (buf.remaining() > Float.SIZE_BYTES) {
                                println("${RCLBufferPrimitiveType.FLOAT} remained buf is over ${Float.SIZE_BYTES}")
                                buf.position(Float.SIZE_BYTES * paramIndex)
                            }
                            rclBufferReadingService.readFloat(buf)
                        }

                        return bufFloat
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