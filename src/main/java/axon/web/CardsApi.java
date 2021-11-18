package axon.web;

import axon.cards.api.*;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class CardsApi {

    final ReactorCommandGateway reactorCommandGateway;

    final ReactorQueryGateway reactorQueryGateway;

    public CardsApi(ReactorCommandGateway reactorCommandGateway, ReactorQueryGateway reactorQueryGateway) {
        this.reactorCommandGateway = reactorCommandGateway;
        this.reactorQueryGateway = reactorQueryGateway;
    }

    @GetMapping("/cards")
    public Mono<List<CardBalanceView>> cards() {
        return reactorQueryGateway.query(
            new AllCardsQuery(),
            ResponseTypes.multipleInstancesOf(CardBalanceView.class)
        );
    }

    @PostMapping("/cards")
    public Mono<String> issue() {
        return reactorCommandGateway.send(new IssueCardCmd());
    }

    @GetMapping(path = "/cards/{uid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CardBalanceView> cardBalance(@PathVariable String uid) {
        return reactorQueryGateway.subscriptionQuery(
            new CardBalanceQuery(uid),
            ResponseTypes.instanceOf(CardBalanceView.class)
        );
    }

    @PostMapping(path = "/cards/{uid}/credit/{amount}")
    public Mono<Void> credit(@PathVariable String uid, @PathVariable double amount) {
        return reactorCommandGateway.send(new RechargeCardCmd(uid, amount));
    }
}
