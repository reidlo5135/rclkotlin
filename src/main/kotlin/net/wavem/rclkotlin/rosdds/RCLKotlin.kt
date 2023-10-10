package net.wavem.rclkotlin.rosdds

import id.jrosmessages.Message
import net.wavem.rclkotlin.rosdds.infra.DDSSupport
import net.wavem.rclkotlin.rosdds.service.ServiceClient
import net.wavem.rclkotlin.rosdds.service.ServiceServer
import net.wavem.rclkotlin.rosdds.topic.Publisher
import net.wavem.rclkotlin.rosdds.topic.Subscription
import pinorobotics.rtpstalk.RtpsTalkClient
import pinorobotics.rtpstalk.RtpsTalkConfiguration
import java.lang.RuntimeException


open class RCLKotlin {

    companion object {
        inline fun <reified M : Message> createPublisher(topic : String) : Publisher {
            val publisher : Publisher = Publisher()
            publisher.registerPublisher<M>(topic)

            println("$topic publisher created")

            return publisher
        }

        inline fun <reified M : Message> createSubscription(topic : String) : Subscription {
            val subscription : Subscription = Subscription()
            subscription.registerSubscription<M>(topic)

            println("$topic subscription created")

            return subscription
        }

        inline fun <reified M : Message> createServiceClient(serviceName : String) : ServiceClient {
            val serviceClient : ServiceClient = ServiceClient()
            serviceClient.registerServiceClient<M>(serviceName)

            println("$serviceName client created")

            return serviceClient
        }

        inline fun <reified M : Message> createServiceServer(serviceName : String) : ServiceServer {
            val serviceServer : ServiceServer = ServiceServer()
            serviceServer.registerServiceServer<M>(serviceName)

            println("$serviceName server created")

            return serviceServer
        }
    }
}