package net.wavem.rclkotlin.rosidl.message.geometry_msgs

import id.jrosmessages.Message
import id.xfunction.XJson
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Pose() : Message {
    var position : Point = Point()
    var orientation : Quaternion = Quaternion()

    constructor(position : Point, orientation : Quaternion) : this() {
        this.position = position
        this.orientation = orientation
    }

    fun write() : ByteArray {
        val buf : ByteBuffer = ByteBuffer.allocate(Integer.BYTES * 2)
        buf.order(ByteOrder.LITTLE_ENDIAN)

        val positionBytes : ByteArray = this.position.write()
        buf.put(positionBytes)

        val orientationBytes : ByteArray = this.orientation.write()
        buf.put(orientationBytes)

        return buf.array()
    }

    override fun toString(): String {
        return XJson.asString(
            "position", this.position.toString(),
            "orientation", this.orientation.toString()
        )
    }
}