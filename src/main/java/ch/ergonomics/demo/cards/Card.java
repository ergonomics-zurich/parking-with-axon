package ch.ergonomics.demo.cards;

import ch.ergonomics.demo.cards.api.*;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.distributed.CommandDispatchException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Aggregate
public class Card {
    @AggregateIdentifier
    public String uid;
    private double balance = 0.0;
    private final Map<String, Ticket> tickets = new HashMap<>();

    @Autowired private CommandGateway commandGateway;

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
        if (cmd.getDebit() <= 0) {
            throw new IllegalArgumentException("debit must be > 0");
        }
        if (this.balance - cmd.getDebit() < 0) {
            throw new IllegalArgumentException("card may not go negative");
        }
        AggregateLifecycle.apply(new CardBalanceUpdatedEvent(this.uid, this.balance - cmd.getDebit()));
    }

    @CommandHandler
    public void issueTicket(IssueTicketCmd cmd) {
        AggregateLifecycle.apply(new TicketIssuedEvent(cmd.getGid(), cmd.getStart()));
    }

    @CommandHandler
    public void payTicket(PayTicketCmd cmd) {
        if (!tickets.containsKey(cmd.getGid())) {
            throw new IllegalArgumentException(String.format("No ticket at garage %s", cmd.getGid()));
        }
        var ticket = tickets.get(cmd.getGid());
        var duration = Duration.between(ticket.getStart(), cmd.getStop()).abs().toMinutes();
        var price = duration * cmd.getPricePerMinute();
        if (price >= ticket.getAmountPaid()) {
            var debit = price - ticket.getAmountPaid();
            try {
                Objects.requireNonNull(commandGateway.sendAndWait(new DebitCmd(cmd.getUid(), debit)));
            } catch (NullPointerException | CommandExecutionException | CommandDispatchException ex) {
                throw new IllegalArgumentException(
                    String.format(
                        "Debiting card %s with amount %s for ticket in garage %s failed - paid amount %s",
                        cmd.getUid(),
                        price,
                        cmd.getGid(),
                        ticket.getAmountPaid()
                    ),
                    ex
                );
            }
            AggregateLifecycle.apply(new TicketPaidEvent(cmd.getGid(), cmd.getStop(), price));
        }
    }

    @CommandHandler
    public void invalidateTicket(InvalidateTicketCmd cmd) {
        AggregateLifecycle.apply(new TicketInvalidatedEvent(cmd.getGid()));
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
        tickets.put(event.getGid(), Ticket.create(event.getStart()));
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
