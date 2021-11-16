package ch.ergonomics.demo.cards;

import ch.ergonomics.demo.cards.api.*;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.security.SecureRandom;

@Aggregate
public class Card {
    @AggregateIdentifier
    public String uid;

    private double balance = 0.0;

    @CommandHandler
    public Card(IssueCardCmd cmd) {
        var id = CardId.create();
        AggregateLifecycle
            .apply(new CardIssuedEvent(id.toString()))
            .andThenApply(() -> new CardBalanceUpdatedEvent(id.toString(), 0.0));
    }

    protected Card() {
    }

    @CommandHandler
    public void creditCard(CreditCmd cmd) {
        if (cmd.getCredit() <= 0)
            throw new IllegalArgumentException("credit must be > 0");
        if (this.balance + cmd.getCredit() > 500)
            throw new IllegalArgumentException("card may hold no more than 500");

        AggregateLifecycle.apply(new CardBalanceUpdatedEvent(this.uid, this.balance + cmd.getCredit()));
    }

    @CommandHandler
    public void debitCard(DebitCmd cmd) {
        if (cmd.getDebit() <= 0)
            throw new IllegalArgumentException("debit must be > 0");
        if (this.balance - cmd.getDebit() < 0)
            throw new IllegalArgumentException("card may not go negative");

        AggregateLifecycle.apply(new CardBalanceUpdatedEvent(this.uid, this.balance - cmd.getDebit()));
    }

    @EventSourcingHandler
    public void on(CardIssuedEvent event) {
        this.uid = event.getUid();
    }

    @EventSourcingHandler
    public void on(CardBalanceUpdatedEvent event) {
        this.balance = event.getBalance();
    }

    public static class CardId {

        private final String id;

        private CardId(final String id) {
            this.id = id;
        }

        public static CardId create() {
            var rnd = new SecureRandom();
            var sb = new StringBuilder();
            while (sb.length() < 14) {
                sb.append(Integer.toHexString(rnd.nextInt(16)));
            }
            return new CardId(sb.toString());
        }

        @Override
        public String toString() {
            return id;
        }
    }

}
