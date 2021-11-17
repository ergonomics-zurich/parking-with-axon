package axon.garages.command;

import axon.garages.api.*;
import axon.shared.GarageId;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate
public class Garage {
    @AggregateIdentifier
    private String garageId;
    private int capacity;
    private int used;

    protected Garage() {
    }

    @CommandHandler
    public Garage(RegisterGarageCmd cmd) {
        if (cmd.getCapacity() < 0) throw new IllegalArgumentException("capacity must be >= 0");
        if (cmd.getUsed() < 0) throw new IllegalArgumentException("used must be >= 0");
        if (cmd.getUsed() > cmd.getCapacity()) throw new IllegalArgumentException("used must be <= capacity");

        AggregateLifecycle.apply(
            new GarageRegisteredEvent(GarageId.create().toString(), cmd.getCapacity(), cmd.getUsed()));
    }

    @CommandHandler
    public Boolean handle(EnsureCapacityCmd cmd) {
        System.out.println("Garage " + this.garageId + " capacity: " + this.capacity + " used: " + this.used);
        return used < capacity;
    }

    @CommandHandler
    public void handle(ConfirmEntryCmd cmd) {
        AggregateLifecycle
            .apply(new EntryConfirmedEvent(garageId, cmd.getUid()));
    }

    @CommandHandler
    public void handle(ConfirmExitCmd cmd) {
        AggregateLifecycle
            .apply(new ExitConfirmedEvent(garageId, cmd.getUid()));
    }

    @EventSourcingHandler
    public void on(GarageRegisteredEvent event) {
        garageId = event.getGid();
        capacity = event.getCapacity();
        used = event.getUsed();
    }

    @EventSourcingHandler
    public void on(EntryConfirmedEvent event) {
        used = used + 1;
    }

    @EventSourcingHandler
    public void on(ExitConfirmedEvent event) {
        used = used - 1;
    }
}
