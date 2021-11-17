package axon.tickets;

import axon.cards.command.Card;
import axon.cards.api.CardBalanceUpdatedEvent;
import axon.cards.api.CardIssuedEvent;
import axon.cards.api.IssueTicketCmd;
import axon.cards.api.PayTicketCmd;
import axon.cards.api.TicketIssuedEvent;
import axon.cards.api.TicketPaidEvent;
import axon.shared.GarageId;
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
        var gId = GarageId.create().toString();
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
        var gId = GarageId.create().toString();
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
        var gId = GarageId.create().toString();
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
        var gId = GarageId.create().toString();
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
        var gId = GarageId.create().toString();
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
