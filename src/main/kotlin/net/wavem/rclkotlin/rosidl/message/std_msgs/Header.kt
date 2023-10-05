package net.wavem.rclkotlin.rosidl.message.std_msgs

import id.jrosmessages.Message
import net.wavem.rclkotlin.rosidl.infra.RCLMessage
import net.wavem.rclkotlin.rosidl.infra.RCLTypeSupport
import net.wavem.rclkotlin.rosidl.message.builtin_interfaces.Time
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Header() : Message {
    var stamp : Time = Time()
    var frame_id : kotlin.String = ""

    constructor(stamp : Time, frame_id : kotlin.String) : this() {
        this.stamp = stamp
        this.frame_id = frame_id
    }

    companion object {
        @JvmStatic
        fun read(data : ByteArray) : Header {
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