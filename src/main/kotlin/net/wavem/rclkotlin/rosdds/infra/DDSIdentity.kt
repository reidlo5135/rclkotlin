package net.wavem.rclkotlin.rosdds.infra

import java.nio.ByteBuffer

@JvmRecord
data class DDSIdentity(val writerGuid : ByteArray, val seqNum : Long) {
    fun toByteArray() : ByteArray {
        val buf = ByteBuffer.allocate(24)
        buf.put(writerGuid)
        val hi = (seqNum shr 31).toInt()
        val lo = (-1L shr 31 and seqNum).toInt()
        buf.putInt(Integer.reverseBytes(hi))
        buf.putInt(Integer.reverseBytes(lo))
        return buf.array()
    }

    companion object {
        fun valueOf(array : ByteArray?) : DDSIdentity {
            val buf = ByteBuffer.wrap(array)
            val writerGuid = ByteArray(16)
            buf.get(writerGuid)
            val hi = Integer.reverseBytes(buf.getInt()).toLong()
            val lo = Integer.reverseBytes(buf.getInt()).toLong()
            val seqNum = hi shl 31 or lo
            return DDSIdentity(writerGuid, seqNum)
        }
    }
}