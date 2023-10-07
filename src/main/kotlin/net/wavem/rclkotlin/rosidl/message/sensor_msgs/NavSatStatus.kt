package net.wavem.rclkotlin.rosidl.message.sensor_msgs

import id.jrosmessages.Message
import id.xfunction.XJson

class NavSatStatus() : Message {
    var status : Byte = 0
    var service : UShort = 0u

    constructor(status : Byte, service : UShort) : this() {
        this.status = status
        this.service = service
    }

    override fun toString() : String {
        return XJson.asString(
            "status", this.status,
            "service", this.service
        )
    }

    companion object {
        const val STATUS_NO_FIX : Byte = -1
        const val STATUS_FIX : Byte = 0
        const val STATUS_SBAS_FIX : Byte = 1
        const val STATUS_GBAS_FIX : Byte = 2
        const val SERVICE_GPS : UShort = 1u
        const val SERVICE_GLONASS : UShort = 2u
        const val SERVICE_COMPASS : UShort = 4u
        const val SERVICE_GALILEO : UShort = 8u
    }
}