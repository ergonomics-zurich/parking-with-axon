package axon.web;

import axon.cards.api.*;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class CardsApi {
    @Autowired
    ReactorCommandGateway reactorCommandGateway;

    @Autowired
    ReactorQueryGateway reactorQueryGateway;

    @GetMapping("/cards")
    public Mono<List<String>> cards() {
        return reactorQueryGateway.query(
                new CardIdsQuery("0", "f"),
                ResponseTypes.multipleInstancesOf(String.class)
        );
    }

    @GetMapping(path = "/cards/{uid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Mono<SubscriptionQueryResult<CardBalance, CardBalance>> cardBalance(@PathVariable String uid) {
        return reactorQueryGateway.subscriptionQuery(
                new CardBalanceQuery(uid),
                ResponseTypes.instanceOf(CardBalance.class),
                ResponseTypes.instanceOf(CardBalance.class)
        );
    }

    @PostMapping("/cards/issue")
    public Mono<String> issue() {
        return reactorCommandGateway.send(new IssueCardCmd());
    }

    @PostMapping(path = "/cards/{uid}/credit/{amount}")
    public Mono<Void> credit(@PathVariable String uid, @PathVariable double amount) {
        return reactorCommandGateway.send(new CreditCmd(uid, amount));
    }

}
