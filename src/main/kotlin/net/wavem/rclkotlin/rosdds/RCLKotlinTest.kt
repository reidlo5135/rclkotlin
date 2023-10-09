package net.wavem.rclkotlin.rosdds

import id.jros2messages.MessageSerializationUtils
import net.wavem.rclkotlin.rosdds.service.ServiceClient
import net.wavem.rclkotlin.rosdds.service.ServiceServer
import net.wavem.rclkotlin.rosdds.topic.Publisher
import net.wavem.rclkotlin.rosdds.topic.Subscription
import net.wavem.rclkotlin.rosidl.infra.RCLMessageSerialization
import net.wavem.rclkotlin.rosidl.message.example_interfaces.AddTwoIntsRequest
import net.wavem.rclkotlin.rosidl.message.example_interfaces.AddTwoIntsResponse
import net.wavem.rclkotlin.rosidl.message.sensor_msgs.NavSatFix

class RCLKotlinTest {
    fun publishingTest() {
        val publisher : Publisher = RCLKotlin.createPublisher<net.wavem.rclkotlin.rosidl.message.std_msgs.String>("/chatter")

        for (i in 1..10) {
            val string : net.wavem.rclkotlin.rosidl.message.std_msgs.String = net.wavem.rclkotlin.rosidl.message.std_msgs.String()
            string.data = "chatter hi"
            println("string data : ${string.data}")
            publisher.publish(string.write())
            Thread.sleep(500)
        }
    }

    fun subscriptionTest() {
//        val topic : String = "/chatter"
//        val rclSubscription : RCLSubscription<net.wavem.rclkotlin.rosidl.message.std_msgs.String> =
//            rclKotlin.createSubscription(topic, net.wavem.rclkotlin.rosidl.message.std_msgs.String::class)
//
//        rclSubscription.getDataObservable().subscribe { it ->
//            if (it != null) {
//                val d : RCLMessageSerialization = RCLMessageSerialization()
//                val callback : net.wavem.rclkotlin.rosidl.message.std_msgs.String? = d.read(it)
//                println("$topic callback : $callback")
//            }
//        }

        val gpsTopic : String = "/gps/fix"
        val rclGpsSubscription : Subscription = RCLKotlin.createSubscription<NavSatFix>(gpsTopic)

        rclGpsSubscription.getDataObservable().subscribe {
            if (it != null) {
                val d : RCLMessageSerialization = RCLMessageSerialization()
                val callback : NavSatFix? = d.read(it)
                println("$gpsTopic callback : $callback")
            }
        }
    }

    fun serviceRequestTest() {
        val serviceName : String = "/add_two_intsRequest"
        val addTwoIntsRequest : AddTwoIntsRequest = AddTwoIntsRequest()
        addTwoIntsRequest.a = 14
        addTwoIntsRequest.b = 15

        val addTwoIntsRequestBytes : ByteArray = addTwoIntsRequest.write()

        val client : ServiceClient = RCLKotlin.createServiceClient<AddTwoIntsRequest>(serviceName)
        client.requestToServiceServer(null, addTwoIntsRequestBytes)
    }

    fun serviceResponseTest() {
        val serializationUtils = MessageSerializationUtils()
        val serviceName : String = "/add_two_intsResponse"
        val server : ServiceServer = RCLKotlin.createServiceServer<AddTwoIntsResponse>(serviceName)
        server.getDataObservable().subscribe {it ->
            if (it != null) {
//                val response : AddTwoIntsResponse = AddTwoIntsResponse.read(it)
//                println("$serviceName resposne : $response")
            }
        }

    }
}

fun main(args : Array<String>) {
    val rclKotlinTest : RCLKotlinTest = RCLKotlinTest()
//    publishingTest()
    rclKotlinTest.subscriptionTest()

    val d : RCLMessageSerialization = RCLMessageSerialization()
//    d.read<Header>(byteArrayOf(0))
//    serviceRequestTest()
//    rclKotlinTest.serviceResponseTest()
}