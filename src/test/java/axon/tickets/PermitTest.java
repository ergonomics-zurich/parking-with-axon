package axon.tickets;

import axon.cards.api.*;
import axon.cards.command.Card;
import axon.cards.command.Permit;
import axon.util.CardId;
import axon.util.GarageId;
import axon.util.PermitId;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.axonframework.test.matchers.Matchers.*;

class PermitTest {

    private FixtureConfiguration<Card> fixture;

    @BeforeEach
    void setup() {
        fixture = new AggregateTestFixture<>(Card.class);
    }

    @Test
    void testIssueTicketsWorks() {
        var uId = CardId.generate();
        var gId = GarageId.generate();
        fixture
            .given(new CardIssuedEvent(uId), new CardRechargedEvent(uId, 10.0))
            .when(new IssuePermitCmd(uId, gId))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<PermitIssuedEvent>predicate(e -> e.getPermit().getGarageId().equals(gId) && e.getPermit().getCardId().equals(uId))),
                    andNoMore()
                )
            );
    }

    @Test
    void testIssueTicketsMoreThanOnceFails() {
        var uId = CardId.generate();
        var gId = GarageId.generate();
        fixture
            .given(new CardIssuedEvent(uId), new CardRechargedEvent(uId, 10.0))
            .andGivenCommands(new IssuePermitCmd(uId, gId))
            .when(new IssuePermitCmd(uId, gId))
            .expectException(IllegalArgumentException.class);
    }

    @Test
    void testPayTicketsWorks() {
        var uId = CardId.generate();
        var gId = GarageId.generate();
        fixture
            .given(new CardIssuedEvent(uId), new CardRechargedEvent(uId, 10.0))
            .andGivenCommands(new IssuePermitCmd(uId, gId))
            .when(new PayOutstandingCmd(uId, gId))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<PaymentEvent>predicate(e -> true)),
                    andNoMore()
                )
            );
    }

    @Test
    void testPayTicketsWithNoEnoughCreditFails() {
        var uId = CardId.generate();
        var gId = GarageId.generate();
        var permit = new Permit(PermitId.generate(), uId, gId, Instant.now().minusSeconds(20));
        fixture
            .given(new CardIssuedEvent(uId), new CardRechargedEvent(uId, 0.0), new PermitIssuedEvent(permit))
            .when(new PayOutstandingCmd(uId, gId))
            .expectResultMessagePayloadMatching(Matchers.equalTo(false));
    }

    @Test
    void testPayingTicketsMoreThanOnceWorksAndUpdatesStopTime() {
        var uId = CardId.generate();
        var gId = GarageId.generate();
        var permit = new Permit(PermitId.generate(), uId, gId, Instant.now().minusSeconds(60));
        var paidAt = Instant.now().minusSeconds(30);
        var amount = 0.1;
        fixture
            .given(
                new CardIssuedEvent(uId),
                new CardRechargedEvent(uId, 10.0),
                new PermitIssuedEvent(permit),
                new PaymentEvent(uId, permit.getPermitId(), paidAt, amount)
            )
            .when(new PayOutstandingCmd(uId, gId))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<PaymentEvent>predicate(e -> e.getPaidAt().isAfter(paidAt) && e.getAmount() > amount)),
                    andNoMore()
                )
            );
    }
}
