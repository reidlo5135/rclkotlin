package net.wavem.rclkotlin.rosdds.handle.topic

import net.wavem.rclkotlin.rosdds.handle.RCLKotlin
import net.wavem.rclkotlin.rosdds.infra.DDSQoS
import net.wavem.rclkotlin.rosdds.infra.DDSSupport
import net.wavem.rclkotlin.rosidl.infra.RCLMessage
import pinorobotics.rtpstalk.RtpsTalkClient
import pinorobotics.rtpstalk.RtpsTalkConfiguration
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage
import java.util.concurrent.SubmissionPublisher

class RCLPublisher<T : RCLMessage> {
    private val ddsClient : RtpsTalkClient = RtpsTalkClient(
        RtpsTalkConfiguration.Builder()
            .networkInterface(DDSSupport.DDS_NETWORK_INTERFACE_TYPE)
            .build()
    )

    private val ddsSupport : DDSSupport = DDSSupport()
    private val ddsPublisher : SubmissionPublisher<RtpsTalkDataMessage> = SubmissionPublisher<RtpsTalkDataMessage>()

    fun registerPublisher(topic : String, messageType : String) {
        val ddsTopic : String = ddsSupport.qualifyTopic(topic)
        val ddsMessageType : String = ddsSupport.qualifyMessageType(messageType)

        ddsClient.publish(ddsTopic, ddsMessageType, DDSQoS.DEFAULT_PUBLISHER_QOS, ddsPublisher)
    }

    fun publish(data : ByteArray) {
        val ddsMessage : RtpsTalkDataMessage = RtpsTalkDataMessage(data)
        ddsPublisher.submit(ddsMessage)
    }
}