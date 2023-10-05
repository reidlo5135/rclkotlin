package net.wavem.rclkotlin.rosidl.message.std_msgs

import id.jrosmessages.Message
import java.nio.ByteBuffer
import java.nio.ByteOrder

class String() : Message {
    var data : kotlin.String = ""

    fun write() : ByteArray {
        val dataLen : Int = this.data.length + 1
        val buf : ByteBuffer = ByteBuffer.allocate(Integer.BYTES * 2 + dataLen)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(dataLen)
        buf.put(this.data.toByteArray())

        return buf.array()
    }

    companion object {
        @JvmStatic
        fun read(data : ByteArray) : String {
            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            var len : Int = buf.getInt()
            var strData : kotlin.String = ""

            while (len-- > 0) strData += Char(buf.get().toUShort())

            val string : String = String()
            string.data = strData

            return string
        }
    }
}