package axon.garages.command;

import axon.cards.api.PermitInvalidatedEvent;
import axon.cards.api.PermitIssuedEvent;
import axon.garages.api.ConfirmEntryCmd;
import axon.garages.api.ConfirmExitCmd;
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
    void handle(PermitIssuedEvent event) {
        commandGateway.send(new ConfirmEntryCmd(event.getPermit().getGarageId()));
    }

    @EventHandler
    void handle(PermitInvalidatedEvent event) {
        commandGateway.send(new ConfirmExitCmd(event.getPermit().getGarageId()));
    }
}
