package net.wavem.rclkotlin.rosidl.message.geometry_msgs

import id.jrosmessages.Message
import id.xfunction.XJson
import net.wavem.rclkotlin.rosidl.message.std_msgs.Header
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PoseStamped() : Message {
    var header : Header = Header()
    var pose : Pose = Pose()

    constructor(header : Header, pose : Pose) : this() {
        this.header = header
        this.pose = pose
    }

    fun write() : ByteArray {
        val buf : ByteBuffer = ByteBuffer.allocate(Integer.BYTES * 2)
        buf.order(ByteOrder.LITTLE_ENDIAN)

        val headerBytes : ByteArray = this.header.write()
        buf.put(headerBytes)

        val poseBytes : ByteArray = this.pose.write()
        buf.put(poseBytes)

        return buf.array()
    }

    override fun toString(): String {
        return XJson.asString(
            "header", this.header,
            "pose", this.pose
        )
    }
}