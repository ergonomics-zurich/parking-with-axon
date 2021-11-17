package axon.cards;

import axon.cards.api.CardBalance;
import axon.cards.api.CardBalanceQuery;
import axon.cards.api.CardBalanceUpdatedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class CardBalancesView {

    private final Map<String, Double> cards = new HashMap<>();

    @Autowired
    QueryUpdateEmitter queryUpdateEmitter;

    @QueryHandler
    public CardBalance cardBalance(CardBalanceQuery query) {
        if (!cards.containsKey(query.getUid()))
            throw new IllegalArgumentException("unknown card");
        return new CardBalance(query.getUid(), cards.get(query.getUid()));
    }

    @EventHandler
    public void on(CardBalanceUpdatedEvent event) {
        cards.put(
                event.getUid(),
                event.getBalance()
        );
        queryUpdateEmitter.emit(
                CardBalanceQuery.class,
                query -> Objects.equals(query.getUid(), event.getUid()),
                new CardBalance(event.getUid(), event.getBalance())
        );
    }
}
