package net.wavem.rclkotlin.rosidl.infra

import id.jrosmessages.Message
import net.wavem.rclkotlin.rosidl.application.RCLBufferReadingService
import net.wavem.rclkotlin.rosidl.domain.RCLBufferType
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class RCLMessageSerialization {

    val rclBufferReadingService : RCLBufferReadingService = RCLBufferReadingService()

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
                println("parameters : $parameters")

                val args : MutableList<Any?> = mutableListOf()

                for((index, param) in parameters.withIndex()) {
                    println("buf remained : ${buf.remaining()}")
                    val type : String = param.type.toString()
                    println("type : $type")

                    val rclBufferType : RCLBufferType? = RCLBufferType.TYPE_NAME_MAP[type]

                    if (rclBufferType != null) {
                        when (rclBufferType) {
                            RCLBufferType.STRING -> {
                                if (index != parameters.lastIndex) {
                                    val strData : String = rclBufferReadingService.readString(buf)
                                    args.add(strData)
                                } else if (index != parameters.lastIndex) {
                                    val strData : String = rclBufferReadingService.readString(index, buf)
                                    args.add(strData)
                                } else {
                                    val strData : String = rclBufferReadingService.readString(buf)
                                    args.add(strData)
                                }
                            }
                            RCLBufferType.BYTE -> {
                                if (index == PARAMETERS_FIRST_INDEX) {
                                    val bufByte : Byte = rclBufferReadingService.readByte(buf)
                                    args.add(bufByte)
                                } else if (index != parameters.lastIndex) {
                                    val bufByte : Byte = rclBufferReadingService.readByte(index, buf)
                                    args.add(bufByte)
                                } else {
                                    if (buf.remaining() > Byte.SIZE_BYTES) {
                                        println("${RCLBufferType.BYTE} remained buf is over ${Byte.SIZE_BYTES}")
                                        buf.position(Byte.SIZE_BYTES * index)
                                    }
                                    val bufByte : Byte = rclBufferReadingService.readByte(buf)
                                    args.add(bufByte)
                                }
                            }
                            RCLBufferType.UBYTE -> {
                                if (index == PARAMETERS_FIRST_INDEX) {
                                    val bufUByte : UByte = rclBufferReadingService.readUByte(buf)
                                    args.add(bufUByte)
                                } else if (index != parameters.lastIndex) {
                                    val bufUByte : UByte = rclBufferReadingService.readUByte(index, buf)
                                    args.add(bufUByte)
                                } else {
                                    if (buf.remaining() > UByte.SIZE_BYTES) {
                                        println("${RCLBufferType.UBYTE} remained buf is over $UByte.SIZE_BYTES")
                                        buf.position(UByte.SIZE_BYTES * index)
                                    }
                                    val bufUByte : UByte = rclBufferReadingService.readUByte(buf)
                                    args.add(bufUByte)
                                }
                            }
                            RCLBufferType.INT -> {
                                if (index == PARAMETERS_FIRST_INDEX) {
                                    val bufInt : Int = rclBufferReadingService.readInt(buf)
                                    args.add(bufInt)
                                } else if (index != parameters.lastIndex) {
                                    val bufInt : Int = rclBufferReadingService.readInt(index, buf)
                                    args.add(bufInt)
                                } else {
                                    if (buf.remaining() > Int.SIZE_BYTES) {
                                        println("${RCLBufferType.INT} remained buf is over ${Int.SIZE_BYTES}")
                                        buf.position(Int.SIZE_BYTES * index)
                                    }
                                    val bufInt : Int = rclBufferReadingService.readInt(buf)
                                    args.add(bufInt)
                                }
                            }
                            RCLBufferType.SHORT -> {
                                if (index == PARAMETERS_FIRST_INDEX) {
                                    val bufShort : Short = rclBufferReadingService.readShort(buf)
                                    args.add(bufShort)
                                } else if (index != parameters.lastIndex) {
                                    val bufShort : Short = rclBufferReadingService.readShort(index, buf)
                                    args.add(bufShort)
                                } else {
                                    if (buf.remaining() > Short.SIZE_BYTES) {
                                        println("${RCLBufferType.SHORT} remained buf is over ${Short.SIZE_BYTES}")
                                        buf.position(Short.SIZE_BYTES * index)
                                    }
                                    val bufShort : Short = rclBufferReadingService.readShort(buf)
                                    args.add(bufShort)
                                }
                            }
                            RCLBufferType.USHORT -> {
                                if (index == PARAMETERS_FIRST_INDEX) {
                                    val bufUShort : UShort = rclBufferReadingService.readUShort(buf)
                                    args.add(bufUShort)
                                } else if (index != parameters.lastIndex) {
                                    val bufUShort : UShort = rclBufferReadingService.readUShort(index, buf)
                                    args.add(bufUShort)
                                } else {
                                    if (buf.remaining() > UShort.SIZE_BYTES) {
                                        println("${RCLBufferType.USHORT} remained buf is over ${UShort.SIZE_BYTES}")
                                        buf.position(UShort.SIZE_BYTES * index)
                                    }
                                    val bufUShort : UShort = rclBufferReadingService.readUShort(buf)
                                    args.add(bufUShort)
                                }
                            }
                            RCLBufferType.LONG -> {
                                if (index == PARAMETERS_FIRST_INDEX) {
                                    val bufLong : Long = rclBufferReadingService.readLong(buf)
                                    args.add(bufLong)
                                } else if (index != parameters.lastIndex) {
                                    val bufLong : Long = rclBufferReadingService.readLong(index, buf)
                                    args.add(bufLong)
                                } else {
                                    if (buf.remaining() > Long.SIZE_BYTES) {
                                        println("${RCLBufferType.LONG} remained buf is over ${Long.SIZE_BYTES}")
                                        buf.position(Long.SIZE_BYTES * index)
                                    }
                                    val bufLong : Long = rclBufferReadingService.readLong(buf)
                                    args.add(bufLong)
                                }
                            }
                            RCLBufferType.DOUBLE -> {
                                if (index == PARAMETERS_FIRST_INDEX) {
                                    val bufDouble : Double = rclBufferReadingService.readDouble(buf)
                                    args.add(bufDouble)
                                } else if (index != parameters.lastIndex) {
                                    val bufDouble : Double = rclBufferReadingService.readDouble(index, buf)
                                    args.add(bufDouble)
                                } else {
                                    if (buf.remaining() > Double.SIZE_BYTES) {
                                        println("${RCLBufferType.DOUBLE} remained buf is over ${Double.SIZE_BYTES}")
                                        buf.position(Double.SIZE_BYTES * index)
                                    }
                                    val bufDouble : Double = rclBufferReadingService.readDouble(buf)
                                    args.add(bufDouble)
                                }
                            }
                            RCLBufferType.FLOAT -> {
                                if (index == PARAMETERS_FIRST_INDEX) {
                                    val bufFloat : Float = rclBufferReadingService.readFloat(buf)
                                    args.add(bufFloat)
                                } else if (index != parameters.lastIndex) {
                                    val bufFloat : Float = rclBufferReadingService.readFloat(index, buf)
                                    args.add(bufFloat)
                                } else {
                                    if (buf.remaining() > Float.SIZE_BYTES) {
                                        println("${RCLBufferType.FLOAT} remained buf is over ${Float.SIZE_BYTES}")
                                        buf.position(Float.SIZE_BYTES * index)
                                    }
                                    val bufFloat : Float = rclBufferReadingService.readFloat(buf)
                                    args.add(bufFloat)
                                }
                            }
                            RCLBufferType.DOUBLEARRAY -> {
                                val size : Int = buf.getInt()
                                val array : DoubleArray = DoubleArray(size)
                                for(i in 0..<size) {
                                    array[i] = buf.getDouble()
                                }
                                args.add(array)
                            }
                            RCLBufferType.FLOATARRAY -> {
                                val size : Int = buf.getInt()
                                val array : FloatArray = FloatArray(size)
                                for(i in 0..<size) {
                                    array[i] = buf.getFloat()
                                }
                                args.add(array)
                            }
                            RCLBufferType.BOOLEAN -> args.add(buf.get() != 0.toByte())
                            RCLBufferType.BOOLEANARRAY -> {
                                val size : Int = buf.getInt()
                                val array : BooleanArray = BooleanArray(size)
                                for (i in 0..size) {
                                    array[i] = buf.get() != 0.toByte()
                                }
                                args.add(array)
                            }
                        }
                    } else {

                    }

                    if(args.size == parameters.size) {
                        val instance : M = constructor.call(*args.toTypedArray())
                        println("instance : $instance")
                        return instance
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