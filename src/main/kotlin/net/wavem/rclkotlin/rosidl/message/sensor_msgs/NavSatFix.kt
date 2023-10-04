package net.wavem.rclkotlin.rosidl.message.sensor_msgs

import net.wavem.rclkotlin.rosidl.infra.RCLMessage
import net.wavem.rclkotlin.rosidl.infra.RCLTypeSupport
import net.wavem.rclkotlin.rosidl.message.std_msgs.Header
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class NavSatFix(
    val COVARIANCE_TYPE_UNKNOWN : Byte = 0,
    val COVARIANCE_TYPE_APPROXIMATED : Byte = 1,
    val COVARIANCE_TYPE_DIAGONAL_KNOWN : Byte = 2,
    val COVARIANCE_TYPE_KNOWN : Byte = 3,
    val header : Header,
    val status : NavSatStatus,
    val latitude : Double,
    val longitude : Double,
    val altitude : Double,
    val position_covariance : DoubleArray = DoubleArray(9),
    val position_covariance_type : UByte
) : RCLMessage() {

    companion object : RCLTypeSupport<NavSatFix> {
        override fun read(data : ByteArray) : NavSatFix {
            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            val header : Header = Header.read(data)
            val headerSize : Int = 14 + header.frame_id.length
            buf.position(headerSize)

            val status : NavSatStatus = NavSatStatus.read(data)
            buf.position(14)

            val latitude : Double = buf.getDouble()
            val longitude : Double = buf.getDouble()
            val altitude : Double = buf.getDouble()

            val position_covariance : DoubleArray = DoubleArray(9)

            for (i in position_covariance.indices) {
                position_covariance[i] = buf.getDouble()
            }

            val position_covariance_type : UByte = buf.get().toUByte()

            return NavSatFix(
                header = header,
                status = status,
                latitude = latitude,
                longitude = longitude,
                altitude = altitude,
                position_covariance = position_covariance,
                position_covariance_type = position_covariance_type
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NavSatFix

        return position_covariance.contentEquals(other.position_covariance)
    }

    override fun hashCode(): Int {
        return position_covariance.contentHashCode()
    }
}