package rsocket

import io.rsocket.ConnectionSetupPayload
import io.rsocket.Payload
import io.rsocket.RSocket
import io.rsocket.SocketAcceptor
import io.rsocket.core.RSocketClient
import io.rsocket.core.RSocketConnector
import io.rsocket.core.RSocketServer
import io.rsocket.frame.decoder.PayloadDecoder
import io.rsocket.transport.netty.server.TcpServerTransport
import io.rsocket.util.DefaultPayload
import org.reactivestreams.Publisher
import reactor.core.Disposable
import reactor.core.publisher.*

import static reactor.core.publisher.Mono.just
import static reactor.core.publisher.Mono.just


class RSocketServer {
    private static  Disposable server

    static void start() {
        io.rsocket.core.RSocketServer rss =  io.rsocket.core.RSocketServer.create()
        rss.acceptor(new SimpleRSocketAcceptor())
        rss.payloadDecoder (PayloadDecoder.ZERO_COPY)
        Mono mono = rss.bind (TcpServerTransport.create("localhost", 7000))
        server = mono.subscribe()
    }

    static void stop() {
        server = server.dispose()
    }
}



public class SimpleRSocketAcceptor implements SocketAcceptor {

    @Override
    public Mono<RSocket> accept(ConnectionSetupPayload connectionSetupPayload, RSocket rSocket) {
        return just(new SimpleRSocket())
    }

}



