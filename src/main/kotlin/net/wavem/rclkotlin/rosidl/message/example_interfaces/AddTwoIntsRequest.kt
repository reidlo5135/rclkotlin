package net.wavem.rclkotlin.rosidl.message.example_interfaces

import id.jrosmessages.Message
import id.xfunction.XJson
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*


class AddTwoIntsRequest : Message {
    var a : Long = 0
    var b : Long = 0

    fun write() : ByteArray {
        val buf : ByteBuffer = ByteBuffer.allocate(Integer.BYTES * 4)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        buf.putLong(this.a)
        buf.putLong(this.b)

        return buf.array()
    }

    override fun hashCode(): Int {
        return Objects.hash(a, b)
    }

    override fun equals(obj: Any?): Boolean {
        val other = obj as AddTwoIntsRequest?
        return Objects.equals(a, other!!.b) && Objects.equals(a, other.b)
    }

    override fun toString(): String {
        return XJson.asString(
            "a", a,
            "b", b
        )
    }
}