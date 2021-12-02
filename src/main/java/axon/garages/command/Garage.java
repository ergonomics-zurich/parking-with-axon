/* Parking with Axon Demo App
 * This is part of Ergonomics AG's code example for the Axon Framework Workshop Nov 21.
 * Feel free to contact us to discuss your event-driven needs at axon.consulting@ergonomics.ch.
 */
package axon.garages.command;

import axon.garages.api.*;
import axon.util.GarageId;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

@Aggregate(snapshotTriggerDefinition = "garageSnapshotTrigger")
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
            new GarageRegisteredEvent(GarageId.generate(), cmd.getCapacity(), cmd.getUsed()));
    }

    @CommandHandler
    public Boolean handle(EnsureCapacityCmd cmd) {
        return used < capacity;
    }

    @CommandHandler
    public void handle(ConfirmEntryCmd cmd) {
        AggregateLifecycle.apply(new EntryConfirmedEvent(garageId));
    }

    @CommandHandler
    public void handle(ConfirmExitCmd cmd) {
        AggregateLifecycle.apply(new ExitConfirmedEvent(garageId));
    }

    @EventSourcingHandler
    public void on(GarageRegisteredEvent event) {
        garageId = event.getGarageId();
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
