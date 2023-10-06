package net.wavem.rclkotlin.rosdds.infra

import id.jrosmessages.Message
import kotlin.reflect.KClass

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

    fun <M : Message> qualifyMessageType(messageTypeClass : KClass<M>) : String {
        val messageTypePackageName : String = messageTypeClass.qualifiedName?.substringBeforeLast(".").toString()
        val messageTypePackageNameSplit : List<String> = messageTypePackageName.split(".")

        val rclMessageTypePackage : String = messageTypePackageNameSplit.last()
        val rclMessageTypeClassName : String = "${messageTypeClass.simpleName.toString()}_"

        val ddsQualifiedMessageType : String = "$rclMessageTypePackage$DDS_MESSAGE_TYPE_FORMAT$rclMessageTypeClassName"

        print("qualifyMessage : $ddsQualifiedMessageType")

        return ddsQualifiedMessageType
    }

    fun <M : Message> qualifyServiceName(serviceName : String, serviceTypeClass : KClass<M>) : String {
        val serviceTypePackageName : String = serviceTypeClass.qualifiedName?.substringBeforeLast(".").toString()
        val serviceTypePackageNameSplit : List<String> = serviceTypePackageName.split(".")

        val rclServiceTypePackage : String = serviceTypePackageNameSplit.last()
        val rclServiceTypeClassName : String = serviceTypeClass.simpleName.toString()

        return if (rclServiceTypeClassName.contains("Request")) {
            var rclServiceName : String = ""
            rclServiceName = if (!serviceName.contains("/")) {
                "$DDS_SERVICE_REQUEST_FORMAT/$serviceName"
            } else {
                "$DDS_SERVICE_REQUEST_FORMAT$serviceName"
            }
            rclServiceName
        } else if (rclServiceTypeClassName.contains("Response")) {
            var rclServiceName : String = ""
            rclServiceName = if (!serviceName.contains("/")) {
                "$DDS_SERVICE_RESPONSE_FORMAT/$serviceName"
            } else {
                "$DDS_SERVICE_RESPONSE_FORMAT$serviceName"
            }
            rclServiceName
        } else {
            throw RuntimeException("Invalid RCL Service Name")
        }
    }

    fun <M : Message> qualifyServiceType(serviceTypeClass : KClass<M>) : String {
        val serviceTypePackageName : String = serviceTypeClass.qualifiedName?.substringBeforeLast(".").toString()
        val serviceTypePackageNameSplit : List<String> = serviceTypePackageName.split(".")

        val rclServiceTypePackage : String = serviceTypePackageNameSplit.last()
        val rclServiceTypeClassName : String = serviceTypeClass.simpleName.toString()

        return if (rclServiceTypeClassName.contains("Request")) {
            val rclServiceRequestClassName : String = rclServiceTypeClassName.replace("Request", "_Request_")
            "${rclServiceTypePackage}${DDS_SERVICE_TYPE_FORMAT}${rclServiceRequestClassName}"
        } else if (rclServiceTypeClassName.contains("Response")) {
            val rclServiceResponseClassName : String = rclServiceTypeClassName.replace("Response", "_Response_")
            "${rclServiceTypePackage}${DDS_SERVICE_TYPE_FORMAT}${rclServiceResponseClassName}"
        } else {
            throw RuntimeException("Invalid RCL Service Type")
        }
    }


    companion object {
        const val DDS_NETWORK_INTERFACE_TYPE : String = "lo"
        const val DDS_TOPIC_FORMAT : String = "rt"
        const val DDS_SERVICE_REQUEST_FORMAT : String = "rq"
        const val DDS_SERVICE_RESPONSE_FORMAT : String = "rr"
        const val DDS_MESSAGE_TYPE_FORMAT : String = "::msg::dds_::"
        const val DDS_SERVICE_TYPE_FORMAT : String = "::srv::dds_::"
    }
}