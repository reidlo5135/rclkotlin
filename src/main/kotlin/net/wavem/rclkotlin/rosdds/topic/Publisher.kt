package net.wavem.rclkotlin.rosdds.topic

import id.jrosmessages.Message
import net.wavem.rclkotlin.rosdds.infra.DDSQoS
import net.wavem.rclkotlin.rosdds.infra.DDSSupport
import pinorobotics.rtpstalk.RtpsTalkClient
import pinorobotics.rtpstalk.RtpsTalkConfiguration
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage
import java.util.concurrent.SubmissionPublisher

class Publisher {
    val ddsClient : RtpsTalkClient = RtpsTalkClient(
        RtpsTalkConfiguration.Builder()
            .networkInterface(DDSSupport.DDS_NETWORK_INTERFACE_TYPE)
            .build()
    )

    val ddsSupport : DDSSupport = DDSSupport()
    val ddsPublisher : SubmissionPublisher<RtpsTalkDataMessage> = SubmissionPublisher<RtpsTalkDataMessage>()

    inline fun <reified M : Message> registerPublisher(topic : String) {
        val ddsTopic : String = ddsSupport.qualifyTopic(topic)
        val ddsMessageType : String = ddsSupport.qualifyMessageType(M::class)

        ddsClient.publish(ddsTopic, ddsMessageType, DDSQoS.DEFAULT_PUBLISHER_QOS, ddsPublisher)
    }

    fun publish(data : ByteArray) {
        val ddsMessage : RtpsTalkDataMessage = RtpsTalkDataMessage(data)
        ddsPublisher.submit(ddsMessage)
    }
}