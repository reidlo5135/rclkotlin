package net.wavem.rclkotlin.rosdds

import id.jrosmessages.Message
import net.wavem.rclkotlin.rosdds.service.RCLServiceClient
import net.wavem.rclkotlin.rosdds.service.RCLServiceServer
import net.wavem.rclkotlin.rosdds.topic.RCLPublisher
import net.wavem.rclkotlin.rosdds.topic.RCLSubscription
import kotlin.reflect.KClass


open class RCLKotlin {
    fun <M : Message> createPublisher(topic : String, messageType : KClass<M>) : RCLPublisher<M> {
        val rclPublisher : RCLPublisher<M> = RCLPublisher(messageType)
        rclPublisher.registerPublisher(topic)

        println("$topic publisher created")

        return rclPublisher
    }

    fun <M : Message> createSubscription(topic : String, messageType : KClass<M>) : RCLSubscription<M> {
        val rclSubscription : RCLSubscription<M> = RCLSubscription(messageType)
        rclSubscription.registerSubscription(topic)

        println("$topic subscription created")

        return rclSubscription
    }

    fun <M : Message> createServiceClient(serviceName : String, serviceType : KClass<M>) : RCLServiceClient<M> {
        val rclServiceClient : RCLServiceClient<M> = RCLServiceClient(serviceType)
        rclServiceClient.registerServiceClient(serviceName)

        println("$serviceName client created")

        return rclServiceClient
    }

    fun <M : Message> createServiceServer(serviceName : String, serviceType : KClass<M>) : RCLServiceServer<M> {
        val rclServiceServer : RCLServiceServer<M> = RCLServiceServer(serviceType)
        rclServiceServer.registerServiceServer(serviceName)

        println("$serviceName server created")

        return rclServiceServer
    }
}