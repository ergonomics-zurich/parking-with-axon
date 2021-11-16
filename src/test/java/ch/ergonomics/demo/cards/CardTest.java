package ch.ergonomics.demo.cards;

import ch.ergonomics.demo.cards.api.CardBalanceUpdatedEvent;
import ch.ergonomics.demo.cards.api.CardIssuedEvent;
import ch.ergonomics.demo.cards.api.CreditCmd;
import ch.ergonomics.demo.cards.api.DebitCmd;
import ch.ergonomics.demo.cards.api.IssueCardCmd;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.*;

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
    void testCardDebit() {
        var id = Card.CardId.create().toString();
        fixture
            .given(new CardIssuedEvent(id), new CardBalanceUpdatedEvent(id, 0.0))
            .andGivenCommands(new CreditCmd(id, 10))
            .when(new DebitCmd(id, 5))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<CardBalanceUpdatedEvent>predicate(e -> e.getBalance() == 5)),
                    andNoMore()
                )
            );
    }

    @Test
    void testCardDebitFailsWithBalanceLessThanAmount() {
        var id = Card.CardId.create().toString();
        fixture
            .given(new CardIssuedEvent(id), new CardBalanceUpdatedEvent(id, 0.0))
            .when(new DebitCmd(id, 10))
            .expectException(IllegalArgumentException.class);
    }

    @Test
    void testCardDebitFailsWithZeroAmount() {
        var id = Card.CardId.create().toString();
        fixture
            .given(new CardIssuedEvent(id), new CardBalanceUpdatedEvent(id, 0.0))
            .when(new DebitCmd(id, 0))
            .expectException(IllegalArgumentException.class);
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
            .when(new DebitCmd(id, 0))
            .expectException(IllegalArgumentException.class);
    }
}
