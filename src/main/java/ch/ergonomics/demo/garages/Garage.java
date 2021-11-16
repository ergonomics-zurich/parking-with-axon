package ch.ergonomics.demo.garages;

import ch.ergonomics.demo.garages.api.CapacityDecCmd;
import ch.ergonomics.demo.garages.api.CapacityIncCmd;
import ch.ergonomics.demo.garages.api.ConfirmEntryCmd;
import ch.ergonomics.demo.garages.api.ConfirmExitCmd;
import ch.ergonomics.demo.garages.api.EntryAllowedEvent;
import ch.ergonomics.demo.garages.api.EntryConfirmedEvent;
import ch.ergonomics.demo.garages.api.ExitConfirmedEvent;
import ch.ergonomics.demo.garages.api.ExitRequestedEvent;
import ch.ergonomics.demo.garages.api.GarageRegisteredEvent;
import ch.ergonomics.demo.garages.api.CapacityUpdatedEvent;
import ch.ergonomics.demo.garages.api.RegisterGarageCmd;
import ch.ergonomics.demo.garages.api.RequestEntryCmd;
import ch.ergonomics.demo.garages.api.RequestExitCmd;
import com.google.common.hash.Hashing;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;

@Aggregate
public class Garage {
    @AggregateIdentifier
    private String garageId;
    private int capacity;

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
        AggregateLifecycle.apply(new EntryAllowedEvent(cmd.getGarageId(), cmd.getCardId()));
    }

    @CommandHandler
    public void handle(RequestExitCmd cmd) {
        AggregateLifecycle.apply(new ExitRequestedEvent(createTicketId(cmd.getGarageId(), cmd.getCardId()), Instant.now()));
    }

    @CommandHandler
    public void handle(ConfirmEntryCmd cmd) {
        AggregateLifecycle.apply(
            new EntryConfirmedEvent(
                createTicketId(cmd.getGarageId(), cmd.getCardId()),
                cmd.getGarageId(),
                cmd.getCardId(),
                0.005,
                Instant.now()
            )
        );
    }

    @CommandHandler
    public void handle(ConfirmExitCmd cmd) {
        AggregateLifecycle.apply(new ExitConfirmedEvent(cmd.getGarageId()));
    }

    @CommandHandler
    public void handle(CapacityIncCmd cmd) {
        AggregateLifecycle.apply(new CapacityUpdatedEvent(cmd.getGarageId(), capacity + 1));
    }

    @CommandHandler
    public void handle(CapacityDecCmd cmd) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("no free slots available");
        }
        AggregateLifecycle.apply(new CapacityUpdatedEvent(cmd.getGarageId(), capacity - 1));
    }

    @EventSourcingHandler
    public void on(GarageRegisteredEvent event) {
        garageId = event.getGarageId();
        capacity = event.getCapacity();
    }

    @EventSourcingHandler
    public void on(CapacityUpdatedEvent event) {
        capacity = event.getCapacity();
    }

    @SuppressWarnings("UnstableApiUsage")
    private static String createTicketId(String garageId, String cardId) {
        return
            String
                .format(
                    "T_%s",
                    Hashing
                        .sha256()
                        .newHasher()
                        .putString(garageId, StandardCharsets.UTF_8)
                        .putString(cardId, StandardCharsets.UTF_8)
                        .hash()
                );
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
