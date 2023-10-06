package net.wavem.rclkotlin.rosidl.infra

import id.jrosmessages.Message

interface RCLTypeSupport<M : Message> {

    fun read(data : ByteArray) : M
}