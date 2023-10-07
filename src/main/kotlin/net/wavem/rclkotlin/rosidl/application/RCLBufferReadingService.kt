package net.wavem.rclkotlin.rosidl.application

import java.nio.ByteBuffer

class RCLBufferReadingService {

    fun readString(buf : ByteBuffer) : String {
        val strDataLen : Int = buf.getInt()
        val strDataBytes = ByteArray(strDataLen)
        buf.get(strDataBytes)

        return String(strDataBytes, Charsets.UTF_8)
    }

    fun readString(paramIndex : Int, buf : ByteBuffer) : String {
        val strDataLen : Int = buf.getInt()
        buf.position(strDataLen * paramIndex)
        val strDataBytes = ByteArray(strDataLen)
        buf.get(strDataBytes)

        return String(strDataBytes, Charsets.UTF_8)
    }

    fun readByte(buf : ByteBuffer) : Byte {
        return buf.get()
    }

    fun readByte(paramIndex : Int, buf : ByteBuffer) : Byte {
        buf.position(Byte.SIZE_BYTES * paramIndex)
        return buf.get()
    }

    fun readUByte(buf : ByteBuffer) : UByte {
        return buf.get().toUByte()
    }

    fun readUByte(paramIndex : Int, buf : ByteBuffer) : UByte {
        buf.position(UByte.SIZE_BYTES * paramIndex)
        return buf.get().toUByte()
    }

    fun readInt(buf : ByteBuffer) : Int {
        return buf.getInt()
    }

    fun readInt(paramIndex : Int, buf : ByteBuffer) : Int {
        buf.position(Int.SIZE_BYTES * paramIndex)
        return buf.getInt()
    }

    fun readShort(buf : ByteBuffer) : Short {
        return buf.getShort()
    }

    fun readShort(paramIndex : Int, buf : ByteBuffer) : Short {
        buf.position(Short.SIZE_BYTES * paramIndex)
        return buf.getShort()
    }

    fun readUShort(buf : ByteBuffer) : UShort {
        return buf.getShort().toUShort()
    }

    fun readUShort(paramIndex : Int, buf : ByteBuffer) : UShort {
        buf.position(UShort.SIZE_BYTES * paramIndex)
        return buf.getShort().toUShort()
    }

    fun readLong(buf : ByteBuffer) : Long {
        return buf.getLong()
    }

    fun readLong(paramIndex : Int, buf : ByteBuffer) : Long {
        buf.position(Long.SIZE_BYTES * paramIndex)
        return buf.getLong()
    }

    fun readDouble(buf : ByteBuffer) : Double {
        return buf.getDouble()
    }

    fun readDouble(paramIndex : Int, buf : ByteBuffer) : Double {
        buf.position(Double.SIZE_BYTES * paramIndex)
        return buf.getDouble()
    }

    fun readFloat(buf : ByteBuffer) : Float {
        return buf.getFloat()
    }

    fun readFloat(paramIndex : Int, buf : ByteBuffer) : Float {
        buf.position(Float.SIZE_BYTES * paramIndex)
        return buf.getFloat()
    }
}