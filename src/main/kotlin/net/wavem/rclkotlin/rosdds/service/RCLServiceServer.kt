package net.wavem.rclkotlin.rosdds.service

import id.jrosmessages.Message
import net.wavem.rclkotlin.rosdds.infra.DDSQoS
import net.wavem.rclkotlin.rosdds.infra.DDSSupport
import java.util.concurrent.Flow.Subscriber
import java.util.concurrent.Flow.Subscription
import pinorobotics.rtpstalk.RtpsTalkClient
import pinorobotics.rtpstalk.RtpsTalkConfiguration
import pinorobotics.rtpstalk.messages.RtpsTalkDataMessage
import rx.Observable
import rx.subjects.PublishSubject
import kotlin.reflect.KClass

class RCLServiceServer<M : Message>(
    private val serviceType : KClass<M>
) {
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

    internal fun registerServiceServer(serviceName : String) {
        val ddsTopic : String = ddsSupport.qualifyServiceName(serviceName, serviceType)
        val ddsMessageType : String = ddsSupport.qualifyServiceType(serviceType)

        ddsClient.subscribe(ddsTopic, ddsMessageType, DDSQoS.DEFAULT_SUBSCRIBER_QOS, object : Subscriber<RtpsTalkDataMessage> {
            private lateinit var subscription : Subscription

            override fun onSubscribe(subscription : Subscription) {
                this.subscription = subscription
                println("$ddsTopic service server registered")
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