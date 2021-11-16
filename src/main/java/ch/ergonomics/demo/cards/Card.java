package ch.ergonomics.demo.cards;

import ch.ergonomics.demo.cards.api.CardBalanceUpdatedEvent;
import ch.ergonomics.demo.cards.api.CardIssuedEvent;
import ch.ergonomics.demo.cards.api.CreditCmd;
import ch.ergonomics.demo.cards.api.InvalidateTicketCmd;
import ch.ergonomics.demo.cards.api.IssueCardCmd;
import ch.ergonomics.demo.cards.api.IssueTicketCmd;
import ch.ergonomics.demo.cards.api.PayTicketCmd;
import ch.ergonomics.demo.cards.api.TicketInvalidatedEvent;
import ch.ergonomics.demo.cards.api.TicketIssuedEvent;
import ch.ergonomics.demo.cards.api.TicketPaidEvent;
import ch.ergonomics.demo.tickets.Ticket;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Aggregate
public class Card {
    @AggregateIdentifier
    public String uid;
    private double balance = 0.0;
    private final Map<String, Ticket> tickets = new HashMap<>();

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
    public void issueTicket(IssueTicketCmd cmd) {
        if (!tickets.containsKey(cmd.getGid())) {
            AggregateLifecycle.apply(new TicketIssuedEvent(cmd.getUid(), cmd.getGid(), cmd.getStart()));
        }
    }

    @CommandHandler
    public void payTicket(PayTicketCmd cmd) {
        if (!tickets.containsKey(cmd.getGid())) {
            throw new IllegalArgumentException(String.format("No ticket at garage %s", cmd.getGid()));
        }
        var ticket = tickets.get(cmd.getGid());
        var duration = Duration.between(ticket.getStart(), cmd.getStop()).abs().toMinutes();
        var price = duration * 0.05;
        if (price >= ticket.getAmountPaid()) {
            var toDebit = price - ticket.getAmountPaid();
            if (toDebit <= 0) {
                throw new IllegalArgumentException("price must be > 0");
            }
            if (this.balance - toDebit < 0) {
                throw new IllegalArgumentException("card may not go negative");
            }
            AggregateLifecycle
                .apply(new TicketPaidEvent(cmd.getGid(), cmd.getStop(), price))
                .andThenApply(() -> new CardBalanceUpdatedEvent(this.uid, this.balance - toDebit));
        } else {
            AggregateLifecycle.apply(new TicketPaidEvent(cmd.getGid(), cmd.getStop(), price));
        }

    }

    @CommandHandler
    public void invalidateTicket(InvalidateTicketCmd cmd) {
        AggregateLifecycle.apply(new TicketInvalidatedEvent(cmd.getGid(), cmd.getUid()));
    }

    @EventSourcingHandler
    public void on(CardIssuedEvent event) {
        this.uid = event.getUid();
    }

    @EventSourcingHandler
    public void on(CardBalanceUpdatedEvent event) {
        this.balance = event.getBalance();
    }

    @EventSourcingHandler
    public void on(TicketIssuedEvent event) {
        tickets.put(event.getGid(), Ticket.create(event.getGid(), event.getUid(), event.getStart()));
    }

    @EventSourcingHandler
    public void on(TicketPaidEvent event) {
        var ticket = tickets.get(event.getGId());
        ticket.setStop(event.getStop());
        ticket.setAmountPaid(event.getPrice());
    }

    @EventSourcingHandler
    public void on(TicketInvalidatedEvent event) {
        tickets.remove(event.getGId());
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
