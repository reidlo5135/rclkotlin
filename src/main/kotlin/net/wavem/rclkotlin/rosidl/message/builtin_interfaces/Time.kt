package net.wavem.rclkotlin.rosidl.message.builtin_interfaces

import id.jrosmessages.Message
import net.wavem.rclkotlin.rosidl.infra.RCLMessage
import net.wavem.rclkotlin.rosidl.infra.RCLTypeSupport
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Time() : Message {
    private var sec : Int = 0
    private var nanosec : Int = 0

    constructor(sec : Int, nanosec : Int) : this() {
        this.sec = sec
        this.nanosec = nanosec
    }

    companion object {
        @JvmStatic
        fun read(data : ByteArray) : Time {
            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            val sec : Int = buf.getInt()
            val nanosec : Int = buf.getInt()

            return Time(
                sec = sec,
                nanosec = nanosec
            )
        }
    }
}