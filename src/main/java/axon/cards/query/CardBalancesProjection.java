/* Parking with Axon Demo App
 * This is part of Ergonomics AG's code example for the Axon Framework Workshop Nov 21.
 * Feel free to contact us to discuss your event-driven needs at axon.consulting@ergonomics.ch.
 */
package axon.cards.query;

import axon.cards.api.*;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CardBalancesProjection {

    private final Map<String, Double> cards = new HashMap<>();

    @Autowired
    QueryUpdateEmitter queryUpdateEmitter;

    @QueryHandler
    public List<CardBalanceView> allCards(AllCardsQuery query) {
        return this.cards.entrySet().stream()
            .map(entry -> new CardBalanceView(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
    }

    @QueryHandler
    public CardBalanceView cardBalance(CardBalanceQuery query) {
        if (!this.cards.containsKey(query.getCardId()))
            throw new IllegalArgumentException("unknown card");
        return new CardBalanceView(query.getCardId(), this.cards.get(query.getCardId()));
    }

    @EventHandler
    public void on(CardIssuedEvent event) {
        this.cards.put(event.getCardId(), 0.0);
    }

    @EventHandler
    public void on(CardRechargedEvent event) {
        var newBalance = this.cards.get(event.getCardId()) + event.getAmount();
        this.cards.put(event.getCardId(), newBalance);

        this.queryUpdateEmitter.emit(
            CardBalanceQuery.class,
            query -> Objects.equals(query.getCardId(), event.getCardId()),
            new CardBalanceView(event.getCardId(), newBalance)
        );
    }

    @EventHandler
    public void on(PaymentEvent event) {
        var newBalance = this.cards.get(event.getCardId()) - event.getAmount();
        this.cards.put(event.getCardId(), newBalance);

        this.queryUpdateEmitter.emit(
            CardBalanceQuery.class,
            query -> Objects.equals(query.getCardId(), event.getCardId()),
            new CardBalanceView(event.getCardId(), newBalance)
        );
    }
}
