package net.wavem.rclkotlin.rosdds.handle.topic

import id.jros2client.impl.rmw.RmwConstants
import java.util.concurrent.Flow.Subscriber
import java.util.concurrent.Flow.Subscription
import net.wavem.rclkotlin.rosdds.infra.DDSSupport
import net.wavem.rclkotlin.rosidl.infra.RCLMessage
import pinorobotics.rtpstalk.RtpsTalkClient
import pinorobotics.rtpstalk.RtpsTalkConfiguration
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage
import rx.Observable
import rx.subjects.PublishSubject

class RCLSubscription<T : RCLMessage> {
    private val ddsClient : RtpsTalkClient = RtpsTalkClient(
        RtpsTalkConfiguration.Builder()
            .networkInterface(DDSSupport.DDS_NETWORK_INTERFACE_TYPE)
            .build()
    )

    private val ddsSupport : DDSSupport = DDSSupport()
    private val dataObservable : PublishSubject<ByteArray> = PublishSubject.create()

    fun getDataObservable() : Observable<ByteArray> {
        return dataObservable
    }

    internal fun registerSubscription(topic : String, messageType : String) {
        val ddsTopic : String = ddsSupport.qualifyTopic(topic)
        val ddsMessageType : String = ddsSupport.qualifyMessageType(messageType)

        ddsClient.subscribe(ddsTopic, ddsMessageType, RmwConstants.DEFAULT_SUBSCRIBER_QOS, object : Subscriber<RtpsTalkDataMessage> {
            private lateinit var subscription : Subscription

            override fun onSubscribe(subscription : Subscription) {
                this.subscription = subscription
                println("$ddsTopic subscription registered")
                subscription.request(1)
            }

            override fun onNext(message : RtpsTalkDataMessage) {
                message.data().ifPresent { data ->
                    dataObservable.onNext(data)
                }
                subscription.request(1)
            }

            override fun onError(throwable : Throwable) {
                throwable.printStackTrace()
            }

            override fun onComplete() {
                subscription.cancel()
            }
        })
    }
}