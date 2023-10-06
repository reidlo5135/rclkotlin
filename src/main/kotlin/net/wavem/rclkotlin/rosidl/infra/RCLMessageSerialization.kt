package net.wavem.rclkotlin.rosidl.infra

import id.jrosmessages.Message
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

class RCLMessageSerialization {

    inline fun <reified M : Message> read(data : ByteArray) : M? {
        try {
            val clazz : KClass<M> = M::class
            val constructors : Collection<KFunction<M>> = clazz.constructors
            println("clazz name : ${clazz.simpleName}")

            var renewData : ByteArray = data

            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            for (constructor in constructors) {
                val parameters : List<KParameter> = constructor.parameters
                if (parameters.isEmpty()) continue
                println("parameters : $parameters")

                val args : MutableList<Any?> = mutableListOf()

                for(param in parameters) {
                    println("size : ${buf.remaining()}")
                    val type : String = param.type.toString()
                    println("type : $type")

                    when (type) {
                        "kotlin.String" -> {
                            val strDataLen : Int = buf.getInt()
                            val strDataBytes = ByteArray(strDataLen)
                            buf.get(strDataBytes)
                            val strData = String(strDataBytes, Charsets.UTF_8)
                            args.add(strData)
                        }
                        "kotlin.Byte" -> {
                            val remainingDataSize : Int = data.size - Byte.SIZE_BYTES
                            renewData = ByteArray(remainingDataSize)
                            val byte : Byte = buf.get()
                            println("byte : $byte")
                            buf.position(Byte.SIZE_BYTES)
                            println("byte buffer : ${buf.position()}")
                            args.add(byte)
                        }
                        "kotlin.UByte" -> args.add(buf.get().toUByte())
                        "kotlin.Int" -> args.add(buf.getInt())
                        "kotlin.Short" -> args.add(buf.getShort())
                        "kotlin.UShort" -> {
                            buf.position(UShort.SIZE_BYTES)
                            println("ushort buffer : ${buf.position()}")
                            val uShort : UShort = buf.getShort().toUShort()
                            println("uShort : $uShort")
                            args.add(uShort)
                        }
                        "kotlin.Long" -> args.add(buf.getLong())
                        "kotlin.Double" -> args.add(buf.getDouble())
                        "kotlin.Float" -> args.add(buf.getFloat())
                        "kotlin.DoubleArray" -> {
                            val size = buf.getInt()
                            val array = DoubleArray(size)
                            for(i in 0..<size) {
                                array[i] = buf.getDouble()
                            }
                            args.add(array)
                        }
                        "kotlin.FloatArray" -> {
                            val size = buf.getInt()
                            val array = FloatArray(size)
                            for(i in 0..<size) {
                                array[i] = buf.getFloat()
                            }
                            args.add(array)
                        }
                        "kotlin.Boolean" -> args.add(buf.get() != 0.toByte())
                        "kotlin.BooleanArray" -> {
                            val size = buf.getInt()
                            val array = BooleanArray(size)
                            for (i in 0..size) {
                                array[i] = buf.get() != 0.toByte()
                            }
                            args.add(array)
                        } else -> {

                        }
                    }

                    if(args.size == parameters.size) {
                        val instance = constructor.call(*args.toTypedArray())
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
}