package net.wavem.rclkotlin.rosidl.message.std_msgs

import net.wavem.rclkotlin.rosidl.infra.RCLMessage
import net.wavem.rclkotlin.rosidl.infra.RCLTypeSupport
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class String(
    val data : kotlin.String
) : RCLMessage() {

    companion object : RCLTypeSupport<String> {
        fun build(data : String) : String {
            return String(data.data)
        }

        fun write(data : String) : ByteArray {
            val stringData : kotlin.String = data.data
            val stringDataLen : Int = stringData.length + 1
            val buf : ByteBuffer = ByteBuffer.allocate(Integer.BYTES * 2 + stringDataLen)
            buf.order(ByteOrder.LITTLE_ENDIAN)
            buf.putInt(stringDataLen)
            buf.put(stringData.toByteArray())

            return buf.array()
        }

        override fun read(data : ByteArray) : String {
            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            var len : Int = buf.getInt()
            var strData : kotlin.String = ""

            while (len-- > 0) strData += Char(buf.get().toUShort())

            return String(
                data = strData
            )
        }
    }
}