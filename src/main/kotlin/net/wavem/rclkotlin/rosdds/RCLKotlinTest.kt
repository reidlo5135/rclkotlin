package net.wavem.rclkotlin.rosdds

import net.wavem.rclkotlin.rosdds.service.RCLServiceClient
import net.wavem.rclkotlin.rosdds.service.RCLServiceServer
import net.wavem.rclkotlin.rosdds.topic.RCLPublisher
import net.wavem.rclkotlin.rosdds.topic.RCLSubscription
import net.wavem.rclkotlin.rosidl.message.example_interfaces.AddTwoIntsRequest
import net.wavem.rclkotlin.rosidl.message.example_interfaces.AddTwoIntsResponse
import net.wavem.rclkotlin.rosidl.message.sensor_msgs.NavSatFix

class RCLKotlinTest {
    fun publishingTest() {
        val rclKotlin : RCLKotlin = RCLKotlin()
        val rclPublisher : RCLPublisher<net.wavem.rclkotlin.rosidl.message.std_msgs.String> = rclKotlin.createPublisher("/chatter", net.wavem.rclkotlin.rosidl.message.std_msgs.String::class)

        for (i in 1..10) {
            val string : net.wavem.rclkotlin.rosidl.message.std_msgs.String = net.wavem.rclkotlin.rosidl.message.std_msgs.String()
            string.data = "chatter hi"
            println("string data : ${string.data}")
            rclPublisher.publish(string.write())
            Thread.sleep(500)
        }
    }

    fun subscriptionTest() {
        val rclKotlin : RCLKotlin = RCLKotlin()
        val topic : String = "/gps/fix"
        val rclSubscription : RCLSubscription<NavSatFix> = rclKotlin.createSubscription(topic, NavSatFix::class)

        rclSubscription.getDataObservable().subscribe { it ->
            if (it != null) {
                val callback : NavSatFix = NavSatFix.read(it)
                println("$topic callback : $callback")
            }
        }
    }

    fun serviceRequestTest() {
        val rclKotlin : RCLKotlin = RCLKotlin()

        val serviceName : String = "/add_two_intsRequest"
        val addTwoIntsRequest : AddTwoIntsRequest = AddTwoIntsRequest()
        addTwoIntsRequest.a = 14
        addTwoIntsRequest.b = 15

        val addTwoIntsRequestBytes : ByteArray = addTwoIntsRequest.write()

        val client : RCLServiceClient<AddTwoIntsRequest> = rclKotlin.createServiceClient(serviceName, AddTwoIntsRequest::class)
        client.requestToServiceServer(null, addTwoIntsRequestBytes)
    }

    fun serviceResponseTest() {
        val rclKotlin : RCLKotlin = RCLKotlin()

        val serviceName : String = "/add_two_intsResponse"
        val server : RCLServiceServer<AddTwoIntsResponse> = rclKotlin.createServiceServer(serviceName, AddTwoIntsResponse::class)
        server.getDataObservable().subscribe {it ->
            if (it != null) {
                val response : AddTwoIntsResponse = AddTwoIntsResponse.read(it)
                println("$serviceName resposne : $response")
            }
        }

    }
}

fun main(args : Array<String>) {
    val rclKotlinTest : RCLKotlinTest = RCLKotlinTest()
//    publishingTest()
    rclKotlinTest.subscriptionTest()
//    serviceRequestTest()
//    rclKotlinTest.serviceResponseTest()
}