package net.wavem.rclkotlin.rosidl.message.builtin_interfaces

import net.wavem.rclkotlin.rosidl.infra.RCLMessage
import net.wavem.rclkotlin.rosidl.infra.RCLTypeSupport
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class Time(
    val sec : Int,
    val nanosec : Int
) : RCLMessage() {
    companion object : RCLTypeSupport<Time> {
        override fun read(data : ByteArray) : Time {
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