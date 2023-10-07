package net.wavem.rclkotlin.rosidl.message.example_interfaces

import id.jrosmessages.Message
import id.xfunction.XJson

class AddTwoIntsResponse() : Message {
    var a : Long = 0
    var b : Long = 0

    constructor(a : Long, b : Long) : this() {
        this.a = a
        this.b = b
    }

    override fun toString() : String {
        return XJson.asString(
            "a", this.a,
            "b", this.b
        )
    }
}