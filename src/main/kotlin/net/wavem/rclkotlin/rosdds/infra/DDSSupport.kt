package net.wavem.rclkotlin.rosdds.infra

class DDSSupport {
    fun qualifyTopic(topic : String) : String {
        var ddsQualifiedTopic : String = ""

        return if (!topic.contains("/")) {
            ddsQualifiedTopic = "$DDS_TOPIC_FORMAT/$topic"

            ddsQualifiedTopic
        } else {
            ddsQualifiedTopic = "$DDS_TOPIC_FORMAT$topic"

            ddsQualifiedTopic
        }
    }

    fun qualifyMessageType(messageType : String) : String {
        if (!messageType.contains("/")) {
            throw RuntimeException("RCLMessageType must be contains '/'")
        }

        val messageTypeSplit : List<String> = messageType.split("/")
        val messagePackage : String = messageTypeSplit[0]
        val messageClass : String = messageTypeSplit[1] + "_"

        val ddsQualifiedMessageType : String = messagePackage + DDS_MESSAGE_TYPE_FORMAT + messageClass

        print("qualifyMessage : $ddsQualifiedMessageType")

        return ddsQualifiedMessageType
    }

    companion object {
        const val DDS_NETWORK_INTERFACE_TYPE : String = "lo"
        const val DDS_TOPIC_FORMAT : String = "rt"
        const val DDS_MESSAGE_TYPE_FORMAT : String = "::msg::dds_::"
    }
}