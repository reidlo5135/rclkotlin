package net.wavem.rclkotlin.rosidl.message.sensor_msgs

import id.jrosmessages.Message
import id.xfunction.XJson
import net.wavem.rclkotlin.rosidl.message.std_msgs.Header

class NavSatFix() : Message {

    var header : Header = Header()
    var status : NavSatStatus = NavSatStatus()
    var latitude : Double = 0.0
    var longitude : Double = 0.0
    var altitude : Double = 0.0
    var position_covariance : DoubleArray = DoubleArray(9)
    var position_covariance_type : UByte  = 0u

    constructor(
        header : Header,
        status : NavSatStatus,
        latitude : Double,
        longitude : Double,
        altitude : Double,
        position_covariance : DoubleArray,
        position_covariance_type : UByte
    ) : this() {
        this.header = header
        this.status = status
        this.latitude = latitude
        this.longitude = longitude
        this.altitude = altitude
        this.position_covariance = position_covariance
        this.position_covariance_type = position_covariance_type
    }

    override fun equals(other : Any?) : Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NavSatFix

        return position_covariance.contentEquals(other.position_covariance)
    }

    override fun hashCode() : Int {
        return position_covariance.contentHashCode()
    }

    override fun toString() : String {
        return XJson.asString(
            "header", this.header.toString(),
            "status", this.status.toString(),
            "latitude", this.latitude,
            "longitude", this.longitude,
            "altitude", this.altitude,
            "position_covariance", this.position_covariance,
            "position_covariance_type", this.position_covariance_type
        )
    }

    companion object {
        const val COVARIANCE_TYPE_UNKNOWN : Byte = 0
        const val COVARIANCE_TYPE_APPROXIMATED : Byte = 1
        const val COVARIANCE_TYPE_DIAGONAL_KNOWN : Byte = 2
        const val COVARIANCE_TYPE_KNOWN : Byte = 3
    }
}