package net.wavem.rclkotlin.rosidl.message.sensor_msgs

import id.jrosmessages.Message
import net.wavem.rclkotlin.rosidl.infra.RCLMessage
import net.wavem.rclkotlin.rosidl.infra.RCLTypeSupport
import java.nio.ByteBuffer
import java.nio.ByteOrder

class NavSatStatus() : Message {

    val STATUS_NO_FIX : Byte = -1
    val STATUS_FIX : Byte = 0
    val STATUS_SBAS_FIX : Byte = 1
    val STATUS_GBAS_FIX : Byte = 2
    val SERVICE_GPS : UShort = 1u
    val SERVICE_GLONASS : UShort = 2u
    val SERVICE_COMPASS : UShort = 4u
    val SERVICE_GALILEO : UShort = 8u
    var status : Byte = 0
    var service : UShort = 0u

    constructor(status : Byte, service : UShort) : this() {
        this.status = status
        this.service = service
    }

    companion object {
        @JvmStatic
        fun read(data : ByteArray) : NavSatStatus {
            val buf : ByteBuffer = ByteBuffer.wrap(data)
            buf.order(ByteOrder.LITTLE_ENDIAN)

            var len : Int = buf.getInt()
            val status : Byte = buf.get()
            val service : UShort = buf.getShort().toUShort()

            return NavSatStatus(
                status = status,
                service = service
            )
        }
    }
}