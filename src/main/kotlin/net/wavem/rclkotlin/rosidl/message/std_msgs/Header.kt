package net.wavem.rclkotlin.rosidl.message.std_msgs

import net.wavem.rclkotlin.rosidl.infra.RCLMessage
import net.wavem.rclkotlin.rosidl.infra.RCLTypeSupport
import net.wavem.rclkotlin.rosidl.message.builtin_interfaces.Time
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Header(
    val stamp : Time,
    val frame_id : kotlin.String
) : RCLMessage() {

    companion object : RCLTypeSupport<Header> {
        override fun read(data : ByteArray) : Header {
            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            val time : Time = Time.read(data)
            buf.position(8)

            var len : Int = buf.getInt()
            var frame_id : kotlin.String = ""

            while (len-- > 0) frame_id += Char(buf.get().toUShort())

            return Header(
                stamp = time,
                frame_id = frame_id
            )
        }
    }
}