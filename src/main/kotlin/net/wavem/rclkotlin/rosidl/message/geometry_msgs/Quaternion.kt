package net.wavem.rclkotlin.rosidl.message.geometry_msgs

import id.jrosmessages.Message
import id.xfunction.XJson
import net.wavem.rclkotlin.rosidl.infra.RCLTypeSupport
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Quaternion() : Message {
    var x : Double = 0.0
    var y : Double = 0.0
    var z : Double = 0.0
    var w : Double = 0.0

    constructor(
        x : Double, y : Double,
        z : Double, w : Double
    ) : this() {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    fun write() : ByteArray {
        val buf : ByteBuffer = ByteBuffer.allocate(Integer.BYTES * 2)
        buf.order(ByteOrder.LITTLE_ENDIAN)

        buf.putDouble(this.x)
        buf.putDouble(this.y)
        buf.putDouble(this.z)
        buf.putDouble(this.w)

        return buf.array()
    }

    override fun toString() : String {
        return XJson.asString(
            "x", this.x,
            "y", this.y,
            "z", this.z,
            "w", this.w
        )
    }

    companion object : RCLTypeSupport<Quaternion> {
        @JvmStatic
        override fun read(data : ByteArray) : Quaternion {
            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            val x : Double = buf.getDouble()
            val y : Double = buf.getDouble()
            val z : Double = buf.getDouble()
            val w : Double = buf.getDouble()

            return Quaternion(
                x = x,
                y = y,
                z = z,
                w = w
            )
        }
    }
}