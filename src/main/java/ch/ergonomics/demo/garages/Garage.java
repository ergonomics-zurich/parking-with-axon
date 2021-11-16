package ch.ergonomics.demo.garages;

import ch.ergonomics.demo.cards.api.InvalidateTicketCmd;
import ch.ergonomics.demo.cards.api.IssueTicketCmd;
import ch.ergonomics.demo.cards.api.PayTicketCmd;
import ch.ergonomics.demo.garages.api.CapacityUpdatedEvent;
import ch.ergonomics.demo.garages.api.ConfirmEntryCmd;
import ch.ergonomics.demo.garages.api.ConfirmExitCmd;
import ch.ergonomics.demo.garages.api.EntryAllowedEvent;
import ch.ergonomics.demo.garages.api.EntryConfirmedEvent;
import ch.ergonomics.demo.garages.api.ExitAllowedEvent;
import ch.ergonomics.demo.garages.api.ExitConfirmedEvent;
import ch.ergonomics.demo.garages.api.GarageRegisteredEvent;
import ch.ergonomics.demo.garages.api.RegisterGarageCmd;
import ch.ergonomics.demo.garages.api.RequestEntryCmd;
import ch.ergonomics.demo.garages.api.RequestExitCmd;
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
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Aggregate
public class Garage {
    @AggregateIdentifier
    private String garageId;
    private int capacity;

    @Autowired private CommandGateway commandGateway;

    protected Garage() {
    }

    @CommandHandler
    public Garage(RegisterGarageCmd cmd) {
        if (cmd.getCapacity() <= 0) {
            throw new IllegalArgumentException("capacity must be > 0");
        }
        AggregateLifecycle.apply(new GarageRegisteredEvent(GarageId.create().toString(), cmd.getCapacity()));
    }

    @CommandHandler
    public void handle(RequestEntryCmd cmd) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("no free slots available");
        }
        AggregateLifecycle.apply(new EntryAllowedEvent(cmd.getGId(), cmd.getUId()));
    }

    @CommandHandler
    public void handle(RequestExitCmd cmd) {
        try {
            Objects.requireNonNull(
                commandGateway
                    .sendAndWait(
                        new PayTicketCmd(cmd.getUId(), cmd.getGId(), Instant.now(), 0.005),
                       30,
                       TimeUnit.SECONDS
                    )
            );
        } catch (NullPointerException | CommandExecutionException | CommandDispatchException ex) {
            throw new IllegalArgumentException("exit not allowed", ex);
        }
        AggregateLifecycle.apply(new ExitAllowedEvent(cmd.getGId()));
    }

    @CommandHandler
    public void confirmEntry(ConfirmEntryCmd cmd) {
        var start = Instant.now();
        AggregateLifecycle
            .apply(new EntryConfirmedEvent(cmd.getGId(), cmd.getUId(), start))
            .andThenApply(() -> new CapacityUpdatedEvent(cmd.getGId(), capacity - 1))
            .andThen(() -> commandGateway.send(new IssueTicketCmd(cmd.getUId(), cmd.getGId(), start)));
    }

    @CommandHandler
    public void confirmExit(ConfirmExitCmd cmd) {
        AggregateLifecycle
            .apply(new ExitConfirmedEvent(cmd.getGId()))
            .andThenApply(() -> new CapacityUpdatedEvent(cmd.getGId(), capacity + 1))
            .andThen(() -> commandGateway.send(new InvalidateTicketCmd(cmd.getUId(), cmd.getGId())));
    }

    @EventSourcingHandler
    public void on(GarageRegisteredEvent event) {
        garageId = event.getGId();
        capacity = event.getCapacity();
    }

    @EventSourcingHandler
    public void on(CapacityUpdatedEvent event) {
        capacity = event.getCapacity();
    }

    public static class GarageId {
        private final String id;

        private GarageId(String id) {
            this.id = id;
        }

        public static GarageId create() {
            return new GarageId("G" + (1_000_000 + new SecureRandom().nextInt(8_000_000)));
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
