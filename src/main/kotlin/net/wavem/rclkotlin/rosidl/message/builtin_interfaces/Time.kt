package net.wavem.rclkotlin.rosidl.message.builtin_interfaces

import id.jrosmessages.Message
import id.xfunction.XJson
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Time() : Message {
    private var sec : Int = 0
    private var nanosec : Int = 0

    constructor(sec : Int, nanosec : Int) : this() {
        this.sec = sec
        this.nanosec = nanosec
    }

    fun write() : ByteArray {
        val buf : ByteBuffer = ByteBuffer.allocate(Integer.BYTES * 2)
        buf.order(ByteOrder.LITTLE_ENDIAN)

        buf.putInt(this.sec)
        buf.putInt(this.nanosec)

        return buf.array()
    }

    override fun toString() : String {
        return XJson.asString(
            "sec", this.sec,
            "nanosec", this.nanosec
        )
    }
}