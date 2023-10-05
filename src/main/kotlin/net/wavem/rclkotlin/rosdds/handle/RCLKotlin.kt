package net.wavem.rclkotlin.rosdds.handle

import id.jrosmessages.Message
import net.wavem.rclkotlin.rosdds.handle.topic.RCLPublisher
import net.wavem.rclkotlin.rosdds.handle.topic.RCLSubscription
import net.wavem.rclkotlin.rosdds.infra.DDSQoS
import net.wavem.rclkotlin.rosdds.infra.DDSSupport
import net.wavem.rclkotlin.rosidl.message.example_interfaces.AddTwoIntsRequest
import net.wavem.rclkotlin.rosdds.infra.DDSIdentity
import net.wavem.rclkotlin.rosidl.message.sensor_msgs.NavSatFix
import pinorobotics.rtpstalk.RtpsTalkClient
import pinorobotics.rtpstalk.RtpsTalkConfiguration
import pinorobotics.rtpstalk.messages.Parameters
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.SubmissionPublisher


open class RCLKotlin {
    fun <T : Message> createPublisher(topic : String, messageType : String) : RCLPublisher<T> {
        val rclPublisher : RCLPublisher<T> = RCLPublisher<T>()
        rclPublisher.registerPublisher(topic, messageType)

        println("$topic publisher created")

        return rclPublisher
    }

    fun <T : Message> createSubscription(topic : String, messageType: String) : RCLSubscription<T> {
        val rclSubscription : RCLSubscription<T> = RCLSubscription<T>()
        rclSubscription.registerSubscription(topic, messageType)

        println("$topic subscription created")

        return rclSubscription
    }
}

fun publishingTest() {
    val rclKotlin : RCLKotlin = RCLKotlin()
    val rclPublisher : RCLPublisher<net.wavem.rclkotlin.rosidl.message.std_msgs.String> = rclKotlin.createPublisher("/chatter", "std_msgs/String")

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
    val messageType : String = "sensor_msgs/NavSatFix"
    val rclSubscription : RCLSubscription<NavSatFix> = rclKotlin.createSubscription(topic, messageType)

    rclSubscription.getDataObservable().subscribe() { it ->
        if (it != null) {
            val gps : NavSatFix = NavSatFix.read(it)
            println("$topic callback : $gps")
        }
    }
}

fun serviceRequestTest() {
    val rclKotlin : RCLKotlin = RCLKotlin()
    val ddsClient : RtpsTalkClient = RtpsTalkClient(
        RtpsTalkConfiguration.Builder()
            .networkInterface(DDSSupport.DDS_NETWORK_INTERFACE_TYPE)
            .build()
    )

    val ddsPublisher : SubmissionPublisher<RtpsTalkDataMessage> = SubmissionPublisher<RtpsTalkDataMessage>()

    val topic : String = "rq/add_two_intsRequest"
    ddsClient.publish(topic, "example_interfaces::srv::dds_::AddTwoInts_Request_", DDSQoS.DEFAULT_PUBLISHER_QOS, ddsPublisher)

    val buf : ByteBuffer = ByteBuffer.allocate(16)
    buf.put(ddsClient.configuration.guidPrefix())
    buf.putInt(0);

    val FASTDDS_PID : Short = 0x800f.toShort()
    val params : Parameters = Parameters(
        mapOf(
            FASTDDS_PID to DDSIdentity(buf.array(), 1).toByteArray()
        )
    )

    println("params : $params")
    val addTwoIntsRequest : AddTwoIntsRequest = AddTwoIntsRequest()
    addTwoIntsRequest.a = 12
    addTwoIntsRequest.b = 151
    println("addTwoIntsRequest : $addTwoIntsRequest")

    val data : ByteArray = addTwoIntsRequest.write()
    ddsPublisher.submit(RtpsTalkDataMessage(params, data))

    val future : CompletableFuture<AddTwoIntsRequest> = CompletableFuture<AddTwoIntsRequest>()
}

fun serviceResponseTest() {
    val ddsClient : RtpsTalkClient = RtpsTalkClient(
        RtpsTalkConfiguration.Builder()
            .networkInterface(DDSSupport.DDS_NETWORK_INTERFACE_TYPE)
            .build()
    )

    val topic : String = "rr/add_two_intsResponse"
}

fun main(args : Array<String>) {
//    publishingTest()
//    subscriptionTest()
    serviceRequestTest()
}