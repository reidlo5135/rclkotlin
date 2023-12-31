package net.wavem.rclkotlin.rosidl.message.std_msgs

import id.jrosmessages.Message
import id.xfunction.XJson
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


class String() : Message {
    var data : kotlin.String = ""

    constructor(data : kotlin.String) : this() {
        this.data = data
    }

    fun write() : ByteArray {
        val dataLen : Int = this.data.length + 1
        val buf : ByteBuffer = ByteBuffer.allocate(Integer.BYTES * 2 + dataLen)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        buf.putInt(dataLen)
        buf.put(this.data.toByteArray())

        return buf.array()
    }

    override fun toString() : kotlin.String {
        return XJson.asString(
            "data", this.data
        )
    }

    override fun hashCode(): Int {
        return Objects.hash(data)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as String

        return data == other.data
    }
}