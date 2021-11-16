package ch.ergonomics.demo.garages;

import ch.ergonomics.demo.garages.api.CapacityUpdatedEvent;
import ch.ergonomics.demo.garages.api.ConfirmEntryCmd;
import ch.ergonomics.demo.garages.api.ConfirmExitCmd;
import ch.ergonomics.demo.garages.api.EntryConfirmedEvent;
import ch.ergonomics.demo.garages.api.ExitConfirmedEvent;
import ch.ergonomics.demo.garages.api.GarageRegisteredEvent;
import ch.ergonomics.demo.garages.api.RegisterGarageCmd;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.security.SecureRandom;

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
    public void confirmEntry(ConfirmEntryCmd cmd) {
        AggregateLifecycle
            .apply(new CapacityUpdatedEvent(cmd.getGId(), capacity - 1))
            .andThenApply(() -> new EntryConfirmedEvent(garageId, cmd.getUId() ));
    }

    @CommandHandler
    public void confirmExit(ConfirmExitCmd cmd) {
        AggregateLifecycle
            .apply(new CapacityUpdatedEvent(cmd.getGId(), capacity + 1))
            .andThenApply(() -> new ExitConfirmedEvent(garageId));
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
