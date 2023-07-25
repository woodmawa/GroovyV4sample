package rsocket

import io.rsocket.Payload
import io.rsocket.RSocket
import io.rsocket.util.DefaultPayload
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Simple first socket with default behaviour
 */
class SimpleRSocket implements RSocket {
    @Override
    public Mono<Void> fireAndForget(Payload payload) {
        // just print the received string
        var str = payload.getDataUtf8()
        System.out.println("Received :: " + str)
        return Mono.empty();
    }

    @Override
    public Mono<Payload> requestResponse(Payload payload) {
        // just convert to upper case
        var str = payload.getDataUtf8()
        return Mono.just(DefaultPayload.create(str.toUpperCase()))
    }

    @Override
    public Flux<Payload> requestStream(Payload payload) {
        // convert the given str to char array and return
        var str = payload.getDataUtf8()
        return Flux.fromStream(str.chars().mapToObj(i -> (char) i))
                .map(Object::toString)
                .map(DefaultPayload::create)
    }
}