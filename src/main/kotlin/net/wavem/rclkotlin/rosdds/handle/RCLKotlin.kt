package net.wavem.rclkotlin.rosdds.handle

import net.wavem.rclkotlin.rosdds.handle.topic.RCLPublisher
import net.wavem.rclkotlin.rosdds.handle.topic.RCLSubscription
import net.wavem.rclkotlin.rosidl.infra.RCLMessage

open class RCLKotlin {
    fun <T : RCLMessage> createPublisher(topic : String, messageType : String) : RCLPublisher<T> {
        val rclPublisher : RCLPublisher<T> = RCLPublisher<T>()
        rclPublisher.registerPublisher(topic, messageType)

        println("$topic publisher created")

        return rclPublisher
    }

    fun <T : RCLMessage> createSubscription(topic : String, messageType: String) : RCLSubscription<T> {
        val rclSubscription : RCLSubscription<T> = RCLSubscription<T>()
        rclSubscription.registerSubscription(topic, messageType)

        println("$topic subscription created")

        return rclSubscription
    }
}

fun main(args : Array<String>) {
    val rclKotlin : RCLKotlin = RCLKotlin()
    val rclPublisher : RCLPublisher<net.wavem.rclkotlin.rosidl.message.std_msgs.String> = rclKotlin.createPublisher<net.wavem.rclkotlin.rosidl.message.std_msgs.String>("/chatter", "std_msgs/String")

    val string : net.wavem.rclkotlin.rosidl.message.std_msgs.String = net.wavem.rclkotlin.rosidl.message.std_msgs.String("hi chatter")

    for (i in 1..10) {
        rclPublisher.publish(
            net.wavem.rclkotlin.rosidl.message.std_msgs.String.write(
                net.wavem.rclkotlin.rosidl.message.std_msgs.String.build(string)
            )
        )
        Thread.sleep(500)
    }
}