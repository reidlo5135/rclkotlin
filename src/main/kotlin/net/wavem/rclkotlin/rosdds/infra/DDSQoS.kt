package net.wavem.rclkotlin.rosdds.infra

import pinorobotics.rtpstalk.qos.DurabilityType
import pinorobotics.rtpstalk.qos.PublisherQosPolicy
import pinorobotics.rtpstalk.qos.ReliabilityType
import pinorobotics.rtpstalk.qos.SubscriberQosPolicy

interface DDSQoS {
    companion object {
        val DEFAULT_PUBLISHER_QOS : PublisherQosPolicy = PublisherQosPolicy(ReliabilityType.RELIABLE, DurabilityType.VOLATILE_DURABILITY_QOS)
        val DEFAULT_SUBSCRIBER_QOS : SubscriberQosPolicy = SubscriberQosPolicy(ReliabilityType.RELIABLE, DurabilityType.VOLATILE_DURABILITY_QOS)
    }
}