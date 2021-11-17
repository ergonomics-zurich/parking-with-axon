package axon.garages;

import axon.garages.api.CapacityUpdatedEvent;
import axon.garages.api.ConfirmEntryCmd;
import axon.garages.api.ConfirmExitCmd;
import axon.garages.api.EnsureCapacityCmd;
import axon.garages.api.EntryAllowedEvent;
import axon.garages.api.EntryConfirmedEvent;
import axon.garages.api.ExitConfirmedEvent;
import axon.garages.api.GarageRegisteredEvent;
import axon.garages.api.RegisterGarageCmd;
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
    public void ensureFreeCapacity(EnsureCapacityCmd cmd) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("no empty slots");
        }
        AggregateLifecycle.apply(new EntryAllowedEvent(cmd.getGId(), cmd.getUId()));
    }

    @CommandHandler
    public void confirmEntry(ConfirmEntryCmd cmd) {
        AggregateLifecycle
            .apply(new CapacityUpdatedEvent(cmd.getGId(), capacity - 1))
            .andThenApply(() -> new EntryConfirmedEvent(garageId, cmd.getUId()));
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
