package ch.ergonomics.demo.tickets;

import ch.ergonomics.demo.cards.api.DebitCmd;
import ch.ergonomics.demo.garages.api.CapacityDecCmd;
import ch.ergonomics.demo.garages.api.CapacityIncCmd;
import ch.ergonomics.demo.garages.api.EntryConfirmedEvent;
import ch.ergonomics.demo.garages.api.ExitAllowedEvent;
import ch.ergonomics.demo.garages.api.ExitConfirmedEvent;
import ch.ergonomics.demo.garages.api.ExitNotAllowedEvent;
import ch.ergonomics.demo.garages.api.ExitRequestedEvent;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.distributed.CommandDispatchException;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Saga
public class TicketManagementSaga {

    private static final String TICKET_ID_ASSOCIATION = "ticketId";
    private static final String GARAGE_ID_ASSOCIATION = "garageId";
    private static final String CARD_ID_ASSOCIATION = "cardId";

    private String cardId;
    private String garageId;
    private Instant start;
    private double ratePerMinute;

    private final EventGateway eventGateway;
    private final CommandGateway commandGateway;

    public TicketManagementSaga(EventGateway eventGateway, CommandGateway commandGateway) {
        this.eventGateway = eventGateway;
        this.commandGateway = commandGateway;
    }

    @StartSaga
    @SagaEventHandler(associationProperty = TICKET_ID_ASSOCIATION)
    public void on(EntryConfirmedEvent event) {
        garageId = event.getGarageId();
        cardId = event.getCardId();
        start = event.getStart();
        ratePerMinute = event.getRatePerMinute();
        SagaLifecycle.associateWith(GARAGE_ID_ASSOCIATION, garageId);
        SagaLifecycle.associateWith(CARD_ID_ASSOCIATION, cardId);
        commandGateway.send(new CapacityDecCmd(garageId));
    }

    @SagaEventHandler(associationProperty = TICKET_ID_ASSOCIATION)
    public void on(ExitRequestedEvent event) {
        var price = ratePerMinute * Duration.between(start, event.getStop()).toMinutes();
        try {
            Objects.requireNonNull(
                commandGateway.sendAndWait(new DebitCmd(cardId, price), 10, TimeUnit.SECONDS),
                "Debit card result cannot be null"
            );
            eventGateway.publish(new ExitAllowedEvent(garageId));
        } catch (NullPointerException | CommandExecutionException | CommandDispatchException ex) {
            eventGateway.publish(new ExitNotAllowedEvent(garageId));
        }
    }

    @SagaEventHandler(associationProperty = GARAGE_ID_ASSOCIATION)
    public void on(ExitConfirmedEvent event) {
        commandGateway.send(new CapacityIncCmd(event.getGarageId()));
        SagaLifecycle.end();
    }

}
