package ch.ergonomics.demo.garages;

import ch.ergonomics.demo.garages.api.GarageRegisteredEvent;
import ch.ergonomics.demo.garages.api.RegisterGarageCmd;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;

import java.security.SecureRandom;

public class Garage {
    @AggregateIdentifier
    private String garageId;
    private int capacity;

    protected Garage() {}

    @CommandHandler
    public Garage(RegisterGarageCmd cmd) {
        if (cmd.getCapacity() <= 0) throw new IllegalArgumentException("capacity must be > 0");

        var id = "G" + (1_000_000 + new SecureRandom().nextInt(8_000_000));
        AggregateLifecycle.apply(new GarageRegisteredEvent(id, cmd.getCapacity()));
    }

    @EventSourcingHandler
    public void on(GarageRegisteredEvent event) {
        garageId = event.getGarageId();
        capacity = event.getCapacity();
    }

}
