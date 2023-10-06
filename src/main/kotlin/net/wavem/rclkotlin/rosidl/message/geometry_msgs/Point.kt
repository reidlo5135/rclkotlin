package net.wavem.rclkotlin.rosidl.message.geometry_msgs

import id.jrosmessages.Message
import id.xfunction.XJson
import net.wavem.rclkotlin.rosidl.infra.RCLTypeSupport
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Point() : Message {
    var x : Double = 0.0
    var y : Double = 0.0
    var z : Double = 0.0

    constructor(x : Double, y : Double, z : Double) : this() {
        this.x = x
        this.y = y
        this.z = z
    }

    fun write() : ByteArray {
        val buf : ByteBuffer = ByteBuffer.allocate(Integer.BYTES * 2)
        buf.order(ByteOrder.LITTLE_ENDIAN)
        buf.putDouble(this.x)
        buf.putDouble(this.y)
        buf.putDouble(this.z)

        return buf.array()
    }

    override fun toString() : String {
        return XJson.asString(
            "x", this.x,
            "y", this.y,
            "z", this.z
        )
    }

    companion object : RCLTypeSupport<Point> {
        @JvmStatic
        override fun read(data : ByteArray) : Point {
            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            val x : Double = buf.getDouble()
            val y : Double = buf.getDouble()
            val z : Double = buf.getDouble()

            return Point(
                x = x,
                y = y,
                z = z
            )
        }
    }
}