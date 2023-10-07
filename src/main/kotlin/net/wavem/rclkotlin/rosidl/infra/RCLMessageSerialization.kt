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
import kotlin.reflect.jvm.jvmErasure

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

                val args : MutableList<Any?> = mutableListOf()

                for((index, param) in parameters.withIndex()) {
                    val type : String = param.type.toString()
                    println("type : $type")

                    val rclBufferPrimitiveType : RCLBufferPrimitiveType? = RCLBufferPrimitiveType.TYPE_NAME_MAP[type]

                    if (rclBufferPrimitiveType == null) {
                        val cJClazz : Class<*> = Class.forName(type)
                        val cKClazz : KClass<out Any> = cJClazz.kotlin
                        println("cKCLazz : $cKClazz")

                        val cArgs : MutableList<Any?> = mutableListOf()
                        println("cArgs : $cArgs")

                        val cAnalyzed : Any? = analyzeCustomClass(cKClazz, data)
                        var cBuffCount : Int = 0

                        if (cAnalyzed == null) {
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
                                                    dArgs.add(analyzeCustomClass(dKClazz, data))
                                                    when(dRclBufferPrimitiveType) {
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
                                                cBuffCount = dBuffCount
                                                val dInstance = dKConstructor.call(*dArgs.toTypedArray())
                                                println("dInstance : $dInstance")
                                                cArgs.add(dInstance)
                                            }
                                        }
                                    } else {
                                        println("ccType : $cType")
                                        println("cBuffCount : $cBuffCount")
                                        buf.position(cBuffCount)

                                        when(cRclBufferPrimitiveType){
                                            RCLBufferPrimitiveType.STRING -> {
                                                cArgs.add(analyzePrimitiveClass(String::class, data, cBuffCount))
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

    fun analyzePrimitiveClass(clazz: KClass<out Any>, data: ByteArray, count : Int) : Any? {
        try {
            println("analyzePrimitive clazz name : ${clazz.qualifiedName}")

            val buf: ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            buf.position(count)

            if (clazz == String::class) {
                val len : Int = buf.getInt()
                val stringBytes = ByteArray(count)
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

    fun analyzeCustomClass(clazz: KClass<out Any>, data: ByteArray) : Any? {
        try {
            val constructors : Collection<KFunction<Any>> = clazz.constructors
            println("analyzeCustom clazz name : ${clazz.qualifiedName}")

            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            for (constructor in constructors) {
                val parameters : List<KParameter> = constructor.parameters
                if (parameters.isEmpty()) continue
                println("analyzeCustom parameters : $parameters")

                for((index, param) in parameters.withIndex()) {
                    println("analyzeCustom buf remained : ${buf.remaining()}")
                    val type : String = param.type.toString()
                    println("analyzeCustom type : $type")

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

    fun readCustomClass(className: String, buf: ByteBuffer): Any? {
        try {
            // 클래스 이름을 이용하여 Class 객체를 얻습니다.
            val clazz = Class.forName(className)

            // Java Class를 Kotlin KClass로 변환합니다.
            val kClass = clazz.kotlin

            // 커스텀 클래스의 생성자를 찾습니다.
            val constructors = kClass.constructors

            if (constructors.isNotEmpty()) {
                // 첫 번째 생성자를 사용합니다. 필요에 따라 적절한 생성자를 선택할 수 있습니다.
                val constructor = constructors.first()

                // 생성자의 매개변수를 얻습니다.
                val parameters = constructor.parameters

                // 생성자의 매개변수를 읽어올 리스트를 초기화합니다.
                val args = mutableListOf<Any?>()

                // 생성자의 매개변수를 하나씩 읽어옵니다.
                for (param in parameters) {
                    val type = param.type.toString()
                    val rclBufferPrimitiveType = RCLBufferPrimitiveType.TYPE_NAME_MAP[type]

                    if (rclBufferPrimitiveType != null) {
                        // 기본 타입의 처리
                        // ...
                    } else if (param.type.jvmErasure == String::class) {
                        // String 타입 처리
                        // ...
                    } else {
                        // 커스텀 클래스 처리
                        val customClassInstance = readCustomClass(type, buf)
                        args.add(customClassInstance)
                    }
                }

                // 생성자를 호출하여 객체를 생성합니다.
                val instance = constructor.call(*args.toTypedArray())

                return instance
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    companion object {
        const val PARAMETERS_FIRST_INDEX : Int = 0
    }
}