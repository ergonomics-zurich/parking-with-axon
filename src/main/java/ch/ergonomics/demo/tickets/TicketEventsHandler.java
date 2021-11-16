package ch.ergonomics.demo.tickets;

import ch.ergonomics.demo.cards.api.TicketInvalidatedEvent;
import ch.ergonomics.demo.cards.api.TicketIssuedEvent;
import ch.ergonomics.demo.garages.api.ConfirmEntryCmd;
import ch.ergonomics.demo.garages.api.ConfirmExitCmd;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

@Component
public class TicketEventsHandler {
    private final CommandGateway commandGateway;

    public TicketEventsHandler(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @EventHandler
    void handle(TicketIssuedEvent event) {
        commandGateway.send(new ConfirmEntryCmd(event.getGid(), event.getUid()));
    }

    @EventHandler
    void handle(TicketInvalidatedEvent event) {
        commandGateway.send(new ConfirmExitCmd(event.getGId(), event.getUId()));
    }
}
