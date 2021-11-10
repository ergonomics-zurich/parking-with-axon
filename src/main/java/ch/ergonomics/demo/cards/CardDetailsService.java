package ch.ergonomics.demo.cards;

import ch.ergonomics.demo.cards.api.CardBalance;
import ch.ergonomics.demo.cards.api.CardBalanceUpdatedEvent;
import ch.ergonomics.demo.cards.api.AllCardIdsQuery;
import ch.ergonomics.demo.cards.api.CardBalanceQuery;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class CardDetailsService {

    private final Map<String, Double> cards = new HashMap<>();

    @Autowired
    QueryUpdateEmitter queryUpdateEmitter;

    @QueryHandler
    public List<String> allCards(AllCardIdsQuery query) {
        return List.copyOf(cards.keySet());
    }

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
