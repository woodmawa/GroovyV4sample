package rsocket

import io.rsocket.Payload
import io.rsocket.RSocket
import io.rsocket.core.RSocketConnector
import io.rsocket.transport.netty.client.TcpClientTransport
import io.rsocket.util.DefaultPayload
import reactor.core.publisher.Flux

import java.time.Duration


class RSocketApplication {

    static RSocket rSocket

    static void main (args) {
        RSocketServer.start()

        rSocket = RSocketConnector.connectWith(TcpClientTransport.create(7000)).block()

        Flux<Payload> message = Flux.just("hi", "hello", "how", "are", "you")
                .delayElements(Duration.ofSeconds(1))
                .map(DefaultPayload::create)


        println "-- fire and forget mode -- "
        message.flatMap (payload -> rSocket.fireAndForget(payload))
                .blockLast(Duration.ofMinutes(1))

        println "-- request response  mode -- "
        message.flatMap (payload -> rSocket.requestResponse(payload))
        .doOnNext (response -> println "Response from server :: " + response.getDataUtf8())
                .blockLast(Duration.ofMinutes(1))

        RSocketServer.stop()
    }
}
