package net.wavem.rclkotlin.rosidl.infra

interface RCLTypeSupport<T> {

    fun read(data : ByteArray) : T
}