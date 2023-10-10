package net.wavem.rclkotlin.rosdds.topic

import id.jrosmessages.Message
import net.wavem.rclkotlin.rosdds.infra.DDSQoS
import java.util.concurrent.Flow.Subscriber
import java.util.concurrent.Flow.Subscription
import net.wavem.rclkotlin.rosdds.infra.DDSSupport
import pinorobotics.rtpstalk.RtpsTalkClient
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage
import rx.Observable
import rx.subjects.PublishSubject

class Subscription {
    val ddsSupport : DDSSupport = DDSSupport()
    var ddsClient : RtpsTalkClient = ddsSupport.createDDSClient()
    val dataObservable : PublishSubject<ByteArray> = PublishSubject.create()

    fun getDataObservable() : Observable<ByteArray> {
        return dataObservable
    }

    inline fun <reified M : Message> registerSubscription(topic : String) {
        val ddsTopic : String = ddsSupport.qualifyTopic(topic)
        val ddsMessageType : String = ddsSupport.qualifyMessageType(M::class)

        ddsClient.subscribe(ddsTopic, ddsMessageType, DDSQoS.DEFAULT_SUBSCRIBER_QOS, object : Subscriber<RtpsTalkDataMessage> {
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