package net.wavem.rclkotlin.rosdds.service

import id.jrosmessages.Message
import net.wavem.rclkotlin.rosdds.infra.DDSIdentity
import net.wavem.rclkotlin.rosdds.infra.DDSQoS
import net.wavem.rclkotlin.rosdds.infra.DDSSupport
import net.wavem.rclkotlin.rosidl.message.example_interfaces.AddTwoIntsRequest
import pinorobotics.rtpstalk.RtpsTalkClient
import pinorobotics.rtpstalk.messages.Parameters
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.SubmissionPublisher

class ServiceClient {
    val ddsSupport : DDSSupport = DDSSupport()
    var ddsClient : RtpsTalkClient = ddsSupport.createDDSClient()
    val ddsPublisher : SubmissionPublisher<RtpsTalkDataMessage> = SubmissionPublisher<RtpsTalkDataMessage>()

    inline fun <reified M : Message> registerServiceClient(serviceName : String) {
        val ddsTopic : String = ddsSupport.qualifyServiceName(serviceName, M::class)
        val ddsMessageType : String = ddsSupport.qualifyServiceType(M::class)

        println("ddsServiceName : $ddsTopic")
        println("ddsMessageType : $ddsMessageType")

        ddsClient.publish(ddsTopic, ddsMessageType, DDSQoS.DEFAULT_PUBLISHER_QOS, ddsPublisher)
    }

    fun requestToServiceServer(requestParams : Parameters?, requestMessageBytes : ByteArray) {
        val buf : ByteBuffer = ByteBuffer.allocate(16)
//        buf.put(ddsClient.configuration.guidPrefix())
        buf.putInt(0)

        val params : Parameters = Parameters(
            mapOf(
                FAST_DDS_PID to DDSIdentity(buf.array(), 1).toByteArray()
            )
        )

        println("request to service")
        ddsPublisher.submit(RtpsTalkDataMessage(requestMessageBytes))
        val future : CompletableFuture<AddTwoIntsRequest> = CompletableFuture<AddTwoIntsRequest>()
    }

    companion object {
        const val FAST_DDS_PID : Short = 0x800f.toShort()
    }
}