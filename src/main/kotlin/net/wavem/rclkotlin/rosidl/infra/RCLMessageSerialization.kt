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

                        val cAnalyzed : Any? = analyzeCustomClass(cKClazz, data, 0)

                        if (cAnalyzed == null) {
                            val cArgs : MutableList<Any?> = mutableListOf()
                            println("cArgs : $cArgs")

                            val cKConstructors : Collection<KFunction<Any>> = cKClazz.constructors

                            for (cKConstructor in cKConstructors) {
                                val cParameters : List<KParameter> = cKConstructor.parameters
                                if (cParameters.isEmpty()) continue

                                for(cParam in cParameters) {
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
                                                    dArgs.add(analyzeCustomClass(dKClazz, data, position))

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
                                                cArgs.add(analyzePrimitiveClass(String::class, data, cBuffCount))
                                                cBuffCount += 8
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
                                            } else {
                                                ccBuffCount += cBuffCount + Byte.SIZE_BYTES
                                            }
                                        }
                                        RCLBufferPrimitiveType.USHORT -> {
                                            if (ccIndex == 0) {
                                                ccBuffCount = cBuffCount
                                            } else {
                                                ccBuffCount += cBuffCount + UShort.SIZE_BYTES
                                            }
                                        }
                                        else -> {}
                                    }
                                    println("ccBufCount : $ccBuffCount")
                                    println("ccIndex : $ccIndex")
                                    ccArgs.add(analyzeCustomClass(cKClazz, data, ccBuffCount))
                                    println("ccArgs[$ccIndex] : ${ccArgs[ccIndex]}")
                                }

                                if(ccArgs.size == ccParameters.size) {
                                    println("ccArgs : $ccArgs")
//                                    val ccInstance = ccKConstructor.call(*ccArgs.toTypedArray())
//                                    println("ccInstance : $ccInstance")
                                    args.add(ccArgs)
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

    fun analyzeCustomClass(clazz: KClass<out Any>, data: ByteArray, position: Int) : Any? {
        try {
            println("analyzeCustomClazz name : ${clazz.qualifiedName}")
            println("analyzeCustomClazz position : $position")

            val constructors : Collection<KFunction<Any>> = clazz.constructors

            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)
            buf.position(position)

            for (constructor in constructors) {
                val parameters : List<KParameter> = constructor.parameters
                if (parameters.isEmpty()) continue
                println("analyzeCustomClazz parameters : $parameters")

                for((index, param) in parameters.withIndex()) {
                    println("analyzeCustomClazz buf remained : ${buf.remaining()}")
                    val type : String = param.type.toString()
                    println("analyzeCustomClazz type : $type")

                    val rclBufferPrimitiveType : RCLBufferPrimitiveType? = RCLBufferPrimitiveType.TYPE_NAME_MAP[type]

                    if (rclBufferPrimitiveType != null) {
                        when (rclBufferPrimitiveType) {
                            RCLBufferPrimitiveType.STRING -> {
                                val strData : String = if (index != parameters.lastIndex) {
                                    rclBufferReadingService.readString(buf)
                                } else if (index != parameters.lastIndex) {
                                    rclBufferReadingService.readString(index, buf)
                                } else {
                                    rclBufferReadingService.readString(buf)
                                }
                                return strData
                            }
                            RCLBufferPrimitiveType.BYTE -> {
                                val bufByte : Byte = if (index == PARAMETERS_FIRST_INDEX) {
                                    rclBufferReadingService.readByte(buf)
                                } else if (index != parameters.lastIndex) {
                                    rclBufferReadingService.readByte(index, buf)
                                } else {
                                    if (buf.remaining() > Byte.SIZE_BYTES) {
                                        println("${RCLBufferPrimitiveType.BYTE} remained buf is over ${Byte.SIZE_BYTES}")
                                        buf.position(Byte.SIZE_BYTES * index)
                                    }
                                    rclBufferReadingService.readByte(buf)
                                }

                                return bufByte
                            }
                            RCLBufferPrimitiveType.UBYTE -> {
                                val bufUByte : UByte = if (index == PARAMETERS_FIRST_INDEX) {
                                    rclBufferReadingService.readUByte(buf)
                                } else if (index != parameters.lastIndex) {
                                    rclBufferReadingService.readUByte(index, buf)
                                } else {
                                    if (buf.remaining() > UByte.SIZE_BYTES) {
                                        println("${RCLBufferPrimitiveType.UBYTE} remained buf is over $UByte.SIZE_BYTES")
                                        buf.position(UByte.SIZE_BYTES * index)
                                    }
                                    rclBufferReadingService.readUByte(buf)
                                }

                                return bufUByte
                            }
                            RCLBufferPrimitiveType.INT -> {
                                val bufInt : Int = if (index == PARAMETERS_FIRST_INDEX) {
                                    rclBufferReadingService.readInt(buf)
                                } else if (index != parameters.lastIndex) {
                                    rclBufferReadingService.readInt(index, buf)
                                } else {
                                    if (buf.remaining() > Int.SIZE_BYTES) {
                                        println("${RCLBufferPrimitiveType.INT} remained buf is over ${Int.SIZE_BYTES}")
                                        buf.position(Int.SIZE_BYTES * index)
                                    }
                                    rclBufferReadingService.readInt(buf)
                                }

                                return bufInt
                            }
                            RCLBufferPrimitiveType.SHORT -> {
                                val bufShort : Short = if (index == PARAMETERS_FIRST_INDEX) {
                                    rclBufferReadingService.readShort(buf)
                                } else if (index != parameters.lastIndex) {
                                    rclBufferReadingService.readShort(index, buf)
                                } else {
                                    if (buf.remaining() > Short.SIZE_BYTES) {
                                        println("${RCLBufferPrimitiveType.SHORT} remained buf is over ${Short.SIZE_BYTES}")
                                        buf.position(Short.SIZE_BYTES * index)
                                    }
                                    rclBufferReadingService.readShort(buf)
                                }

                                return bufShort
                            }
                            RCLBufferPrimitiveType.USHORT -> {
                                val bufUShort : UShort = if (index == PARAMETERS_FIRST_INDEX) {
                                    rclBufferReadingService.readUShort(buf)
                                } else if (index != parameters.lastIndex) {
                                    rclBufferReadingService.readUShort(index, buf)
                                } else {
                                    if (buf.remaining() > UShort.SIZE_BYTES) {
                                        println("${RCLBufferPrimitiveType.USHORT} remained buf is over ${UShort.SIZE_BYTES}")
                                        buf.position(UShort.SIZE_BYTES * index)
                                    }
                                    rclBufferReadingService.readUShort(buf)
                                }

                                return bufUShort
                            }
                            RCLBufferPrimitiveType.LONG -> {
                                val bufLong : Long = if (index == PARAMETERS_FIRST_INDEX) {
                                    rclBufferReadingService.readLong(buf)
                                } else if (index != parameters.lastIndex) {
                                    rclBufferReadingService.readLong(index, buf)
                                } else {
                                    if (buf.remaining() > Long.SIZE_BYTES) {
                                        println("${RCLBufferPrimitiveType.LONG} remained buf is over ${Long.SIZE_BYTES}")
                                        buf.position(Long.SIZE_BYTES * index)
                                    }
                                    rclBufferReadingService.readLong(buf)
                                }

                                return bufLong
                            }
                            RCLBufferPrimitiveType.DOUBLE -> {
                                val bufDouble : Double = if (index == PARAMETERS_FIRST_INDEX) {
                                    rclBufferReadingService.readDouble(buf)
                                } else if (index != parameters.lastIndex) {
                                    rclBufferReadingService.readDouble(index, buf)
                                } else {
                                    if (buf.remaining() > Double.SIZE_BYTES) {
                                        println("${RCLBufferPrimitiveType.DOUBLE} remained buf is over ${Double.SIZE_BYTES}")
                                        buf.position(Double.SIZE_BYTES * index)
                                    }
                                    rclBufferReadingService.readDouble(buf)
                                }

                                return bufDouble
                            }
                            RCLBufferPrimitiveType.FLOAT -> {
                                val bufFloat : Float = if (index == PARAMETERS_FIRST_INDEX) {
                                    rclBufferReadingService.readFloat(buf)
                                } else if (index != parameters.lastIndex) {
                                    rclBufferReadingService.readFloat(index, buf)
                                } else {
                                    if (buf.remaining() > Float.SIZE_BYTES) {
                                        println("${RCLBufferPrimitiveType.FLOAT} remained buf is over ${Float.SIZE_BYTES}")
                                        buf.position(Float.SIZE_BYTES * index)
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
                }
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