package net.wavem.rclkotlin.rosidl.message.example_interfaces

import id.jrosmessages.Message
import id.xfunction.XJson
import net.wavem.rclkotlin.rosidl.infra.RCLTypeSupport
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AddTwoIntsResponse() : Message {
    var a : Long = 0
    var b : Long = 0

    constructor(a : Long, b : Long) : this() {
        this.a = a
        this.b = b
    }

    override fun toString() : String {
        return XJson.asString(
            "a", this.a,
            "b", this.b
        )
    }

    companion object : RCLTypeSupport<AddTwoIntsResponse> {
        @JvmStatic
        override fun read(data : ByteArray) : AddTwoIntsResponse {
            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            val a : Long = buf.getLong()
            val b : Long = buf.getLong()

            return AddTwoIntsResponse(
                a = a,
                b = b
            )
        }
    }
}