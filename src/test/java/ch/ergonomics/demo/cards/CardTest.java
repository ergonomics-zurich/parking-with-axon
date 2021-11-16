package ch.ergonomics.demo.cards;

import ch.ergonomics.demo.cards.api.CardBalanceUpdatedEvent;
import ch.ergonomics.demo.cards.api.CardIssuedEvent;
import ch.ergonomics.demo.cards.api.CreditCmd;
import ch.ergonomics.demo.cards.api.IssueCardCmd;
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

class CardTest {

    private FixtureConfiguration<Card> fixture;

    @BeforeEach
    void setup() {
        fixture = new AggregateTestFixture<>(Card.class);
    }

    @Test
    void testCardIssuing() {
        fixture
            .given()
            .when(new IssueCardCmd())
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<CardIssuedEvent>predicate(e -> e.getUid() != null)),
                    messageWithPayload(Matchers.<CardBalanceUpdatedEvent>predicate(e -> e.getUid() != null && e.getBalance() == 0.0)),
                    andNoMore()
                )
            )
            .expectState(card -> Assertions.assertNotNull(card.uid));
    }

    @Test
    void testCardCredit() {
        var id = Card.CardId.create().toString();
        fixture
            .given(new CardIssuedEvent(id), new CardBalanceUpdatedEvent(id, 0.0))
            .when(new CreditCmd(id, 10))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<CardBalanceUpdatedEvent>predicate(e -> e.getBalance() == 10)),
                    andNoMore()
                )
            );
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
                    messageWithPayload(
                        Matchers.<TicketIssuedEvent>predicate(e -> e.getStart() == start && e.getGid().equals(gId) && e.getUid().equals(uId))
                    )
                )
            );
    }

    @Test
    void testIssueTicketsMoreThanOnceWorksAndReturnSameTicket() {
        var uId = Card.CardId.create().toString();
        var gId = Garage.GarageId.create().toString();
        var start = Instant.now();
        fixture
            .given(new CardIssuedEvent(uId), new CardBalanceUpdatedEvent(uId, 10.0))
            .andGivenCommands(new IssueTicketCmd(uId, gId, start))
            .when(new IssueTicketCmd(uId, gId, start.plusSeconds(100)))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(
                        Matchers.<TicketIssuedEvent>predicate(e -> e.getStart() == start && e.getGid().equals(gId) && e.getUid().equals(uId))
                    ),
                    andNoMore()
                )
            );
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
                    messageWithPayload(Matchers.<TicketPaidEvent>predicate(e -> e.getStop() == stop && (int) e.getPrice() == 3)),
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
            .andGivenCommands(new IssueTicketCmd(uId, gId, start))
            .when(new IssueTicketCmd(uId, gId, start.plusSeconds(100)))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(
                        Matchers.<TicketIssuedEvent>predicate(e -> e.getStart() == start && e.getGid().equals(gId) && e.getUid().equals(uId))
                    ),
                    andNoMore()
                )
            );
    }

    @Test
    void testCardCreditFailsWithMoreThan500() {
        var id = Card.CardId.create().toString();
        fixture
            .given(new CardIssuedEvent(id), new CardBalanceUpdatedEvent(id, 1.0))
            .when(new CreditCmd(id, 500))
            .expectException(IllegalArgumentException.class);
    }

    @Test
    void testCardCreditFailsWithZeroAmount() {
        var id = Card.CardId.create().toString();
        fixture
            .given(new CardIssuedEvent(id), new CardBalanceUpdatedEvent(id, 10.0))
            .when(new CreditCmd(id, 0))
            .expectException(IllegalArgumentException.class);
    }
}
