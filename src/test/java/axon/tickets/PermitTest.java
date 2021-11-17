package axon.tickets;

import axon.cards.command.Card;
import axon.cards.api.CardRechargedEvent;
import axon.cards.api.CardIssuedEvent;
import axon.cards.api.IssuePermitCmd;
import axon.cards.api.PayOutstandingCmd;
import axon.cards.api.PermitIssuedEvent;
import axon.cards.api.PaymentEvent;
import axon.util.GarageId;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.*;

import java.time.Instant;

import static org.axonframework.test.matchers.Matchers.*;
import static org.axonframework.test.matchers.Matchers.andNoMore;

class PermitTest {

    private FixtureConfiguration<Card> fixture;

    @BeforeEach
    void setup() {
        fixture = new AggregateTestFixture<>(Card.class);
    }

    @Test
    void testIssueTicketsWorks() {
        var uId = Card.CardId.create().toString();
        var gId = GarageId.generate().toString();
        var start = Instant.now();
        fixture
            .given(new CardIssuedEvent(uId), new CardRechargedEvent(uId, 10.0))
            .when(new IssuePermitCmd(uId, gId, start))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<PermitIssuedEvent>predicate(e -> e.getStart().equals(start) && e.getGid().equals(gId) && e.getUid().equals(uId))),
                    andNoMore()
                )
            );
    }

    @Test
    void testIssueTicketsMoreThanOncePublishesNoMoreTicketIssuedEvents() {
        var uId = Card.CardId.create().toString();
        var gId = GarageId.generate().toString();
        var start = Instant.now();
        fixture
            .given(new CardIssuedEvent(uId), new CardRechargedEvent(uId, 10.0))
            .andGivenCommands(new IssuePermitCmd(uId, gId, start))
            .when(new IssuePermitCmd(uId, gId, start.plusSeconds(100)))
            .expectSuccessfulHandlerExecution()
            .expectNoEvents();
    }

    @Test
    void testPayTicketsWorks() {
        var uId = Card.CardId.create().toString();
        var gId = GarageId.generate().toString();
        var start = Instant.now();
        var stop = start.plusSeconds(3600);
        fixture
            .given(new CardIssuedEvent(uId), new CardRechargedEvent(uId, 10.0))
            .andGivenCommands(new IssuePermitCmd(uId, gId, start))
            .when(new PayOutstandingCmd(uId, gId, stop))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<PaymentEvent>predicate(e -> e.getStop().equals(stop) && (int) e.getPrice() == 3)),
                    messageWithPayload(Matchers.<CardRechargedEvent>predicate(e -> (int) e.getAmount() == 7)),
                    andNoMore()
                )
            );
    }

    @Test
    void testPayTicketsWithNoEnoughCreditFails() {
        var uId = Card.CardId.create().toString();
        var gId = GarageId.generate().toString();
        var start = Instant.now();
        var stop = start.plusSeconds(3600);
        fixture
            .given(new CardIssuedEvent(uId), new CardRechargedEvent(uId, 1.0))
            .andGivenCommands(new IssuePermitCmd(uId, gId, start))
            .when(new PayOutstandingCmd(uId, gId, stop))
            .expectException(IllegalArgumentException.class);
    }

    @Test
    void testPayingTicketsMoreThanOnceWorksAndUpdatesStopTime() {
        var uId = Card.CardId.create().toString();
        var gId = GarageId.generate().toString();
        var start = Instant.now();
        fixture
            .given(new CardIssuedEvent(uId), new CardRechargedEvent(uId, 10.0))
            .andGivenCommands(new IssuePermitCmd(uId, gId, start), new PayOutstandingCmd(uId, gId, start.plusSeconds(600)))
            .when(new PayOutstandingCmd(uId, gId, start.plusSeconds(900)))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<PaymentEvent>predicate(e -> e.getStop().equals(start.plusSeconds(900)))),
                    messageWithPayload(Matchers.<CardRechargedEvent>predicate(e -> e.getAmount() == 9.25)),
                    andNoMore()
                )
            );
    }
}
