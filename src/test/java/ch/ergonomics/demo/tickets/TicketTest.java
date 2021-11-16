package ch.ergonomics.demo.tickets;

import ch.ergonomics.demo.cards.Card;
import ch.ergonomics.demo.cards.api.CardBalanceUpdatedEvent;
import ch.ergonomics.demo.cards.api.CardIssuedEvent;
import ch.ergonomics.demo.cards.api.IssueTicketCmd;
import ch.ergonomics.demo.cards.api.PayTicketCmd;
import ch.ergonomics.demo.cards.api.TicketIssuedEvent;
import ch.ergonomics.demo.cards.api.TicketPaidEvent;
import ch.ergonomics.demo.garages.Garage;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.*;

import java.time.Instant;

import static org.axonframework.test.matchers.Matchers.*;
import static org.axonframework.test.matchers.Matchers.andNoMore;

class TicketTest{

    private FixtureConfiguration<Card> fixture;

    @BeforeEach
    void setup() {
        fixture = new AggregateTestFixture<>(Card.class);
    }

    @Test
    void testIssueTicketsWorks() {
        var uId = Card.CardId.create().toString();
        var gId = Garage.GarageId.create().toString();
        var start = Instant.now();
        fixture
            .given(new CardIssuedEvent(uId), new CardBalanceUpdatedEvent(uId, 10.0))
            .when(new IssueTicketCmd(uId, gId, start))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<TicketIssuedEvent>predicate(e -> e.getStart().equals(start) && e.getGid().equals(gId) && e.getUid().equals(uId))),
                    andNoMore()
                )
            );
    }

    @Test
    void testIssueTicketsMoreThanOncePublishesNoMoreTicketIssuedEvents() {
        var uId = Card.CardId.create().toString();
        var gId = Garage.GarageId.create().toString();
        var start = Instant.now();
        fixture
            .given(new CardIssuedEvent(uId), new CardBalanceUpdatedEvent(uId, 10.0))
            .andGivenCommands(new IssueTicketCmd(uId, gId, start))
            .when(new IssueTicketCmd(uId, gId, start.plusSeconds(100)))
            .expectSuccessfulHandlerExecution()
            .expectNoEvents();
    }

    @Test
    void testPayTicketsWorks() {
        var uId = Card.CardId.create().toString();
        var gId = Garage.GarageId.create().toString();
        var start = Instant.now();
        var stop = start.plusSeconds(3600);
        fixture
            .given(new CardIssuedEvent(uId), new CardBalanceUpdatedEvent(uId, 10.0))
            .andGivenCommands(new IssueTicketCmd(uId, gId, start))
            .when(new PayTicketCmd(uId, gId, stop))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<TicketPaidEvent>predicate(e -> e.getStop().equals(stop) && (int) e.getPrice() == 3)),
                    messageWithPayload(Matchers.<CardBalanceUpdatedEvent>predicate(e -> (int) e.getBalance() == 7)),
                    andNoMore()
                )
            );
    }

    @Test
    void testPayTicketsWithNoEnoughCreditFails() {
        var uId = Card.CardId.create().toString();
        var gId = Garage.GarageId.create().toString();
        var start = Instant.now();
        var stop = start.plusSeconds(3600);
        fixture
            .given(new CardIssuedEvent(uId), new CardBalanceUpdatedEvent(uId, 1.0))
            .andGivenCommands(new IssueTicketCmd(uId, gId, start))
            .when(new PayTicketCmd(uId, gId, stop))
            .expectException(IllegalArgumentException.class);
    }

    @Test
    void testPayingTicketsMoreThanOnceWorksAndUpdatesStopTime() {
        var uId = Card.CardId.create().toString();
        var gId = Garage.GarageId.create().toString();
        var start = Instant.now();
        fixture
            .given(new CardIssuedEvent(uId), new CardBalanceUpdatedEvent(uId, 10.0))
            .andGivenCommands(new IssueTicketCmd(uId, gId, start), new PayTicketCmd(uId, gId, start.plusSeconds(600)))
            .when(new PayTicketCmd(uId, gId, start.plusSeconds(900)))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<TicketPaidEvent>predicate(e -> e.getStop().equals(start.plusSeconds(900)))),
                    messageWithPayload(Matchers.<CardBalanceUpdatedEvent>predicate(e -> e.getBalance() == 9.25)),
                    andNoMore()
                )
            );
    }
}
