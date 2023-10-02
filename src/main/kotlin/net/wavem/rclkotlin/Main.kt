package net.wavem.rclkotlin

import net.wavem.rclkotlin.rosdds.handle.topic.Publisher
import net.wavem.rclkotlin.rosidl.record.std_msgs.String

fun main(args : Array<kotlin.String>) {
    val publisher : Publisher<String> = Publisher<String>()
    publisher.create("/chatter", "std_msgs/String")

    val string : String = String("hi chatter")

    for (i in 1..10) {
        publisher.publish(String.write(String.build(string)))
        Thread.sleep(500)
    }
}