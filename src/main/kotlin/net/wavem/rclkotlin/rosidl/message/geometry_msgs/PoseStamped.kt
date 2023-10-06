package net.wavem.rclkotlin.rosidl.message.geometry_msgs

import id.jrosmessages.Message
import id.xfunction.XJson
import net.wavem.rclkotlin.rosidl.infra.RCLTypeSupport
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

    companion object : RCLTypeSupport<PoseStamped> {
        @JvmStatic
        override fun read(data : ByteArray) : PoseStamped {
            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            val header : Header = Header.read(data)
            val headerSize : Int = 14 + header.frame_id.length
            println("header size : $headerSize")
            buf.position(headerSize)

            val pose : Pose = Pose.read(data)
            buf.position(56)

            return PoseStamped(
                header = header,
                pose = pose
            )
        }
    }
}