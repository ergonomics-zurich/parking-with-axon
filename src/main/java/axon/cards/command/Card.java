package axon.cards.command;

import axon.cards.api.*;
import axon.util.CardId;
import axon.util.PermitId;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Aggregate
public class Card {
    @AggregateIdentifier
    private String cardId;
    private double balance;
    private Set<Permit> permits;
    private HashMap<String, Double> partialPayments;

    protected Card() {
    }

    @CommandHandler
    public Card(IssueCardCmd cmd) {
        AggregateLifecycle.apply(new CardIssuedEvent(CardId.generate()));
    }

    private Optional<Permit> findByGarage(String garageId) {
        return this.permits.stream()
            .filter(permit -> permit.getGarageId().equals(garageId))
            .findFirst();
    }

    @CommandHandler
    public void handle(RechargeCardCmd cmd) {
        if (cmd.getAmount() <= 0) throw new IllegalArgumentException("credit must be > 0");
        if (this.balance + cmd.getAmount() > 500) throw new IllegalArgumentException("card may not hold > 500");

        AggregateLifecycle.apply(new CardRechargedEvent(this.cardId, cmd.getAmount()));
    }

    @CommandHandler
    public void handle(IssuePermitCmd cmd) {
        if (findByGarage(cmd.getGarageId()).isPresent()) throw new IllegalArgumentException("card already used here");

        var permit = new Permit(PermitId.generate(), cmd.getCardId(), cmd.getGarageId(), Instant.now());
        AggregateLifecycle.apply(new PermitIssuedEvent(permit));
    }

    @CommandHandler
    public boolean handle(PayOutstandingCmd cmd) {
        var permit = this.findByGarage(cmd.getGarageId())
            .orElseThrow(() -> new IllegalArgumentException("card not parking here"));
        var price = permit.calcPrice() - this.partialPayments.getOrDefault(permit.getPermitId(), 0.0);
        if (price <= this.balance) {
            AggregateLifecycle.apply(
                new PaymentEvent(this.cardId, permit.getPermitId(), Instant.now(), price));
            return true;
        } else {
            return false;
        }
    }

    @CommandHandler
    public void handle(InvalidatePermitCmd cmd) {
        var permit = this.findByGarage(cmd.getGarageId())
            .orElseThrow(() -> new IllegalArgumentException("card not parking here"));
        AggregateLifecycle.apply(new PermitInvalidatedEvent(permit, Instant.now()));
    }

    @EventSourcingHandler
    public void on(CardIssuedEvent event) {
        this.cardId = event.getCardId();
        this.balance = 0.0;
        this.permits = new HashSet<>();
        this.partialPayments = new HashMap<>();
    }

    @EventSourcingHandler
    public void on(CardRechargedEvent event) {
        this.balance += event.getAmount();
    }

    @EventSourcingHandler
    public void on(PermitIssuedEvent event) {
        this.permits.add(event.getPermit());
    }

    @EventSourcingHandler
    public void on(PaymentEvent event) {
        var amountPaid = this.partialPayments.getOrDefault(event.getPermitId(), 0.0) + event.getAmount();
        this.partialPayments.put(event.getPermitId(), amountPaid);
    }

    @EventSourcingHandler
    public void on(PermitInvalidatedEvent event) {
        this.partialPayments.remove(event.getPermit().getPermitId());
        this.permits.remove(event.getPermit());
    }
}
