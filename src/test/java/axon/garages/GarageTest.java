package axon.garages;

import axon.cards.command.Card;
import axon.garages.api.ConfirmEntryCmd;
import axon.garages.api.ConfirmExitCmd;
import axon.garages.api.EnsureCapacityCmd;
import axon.garages.api.EntryConfirmedEvent;
import axon.garages.api.ExitConfirmedEvent;
import axon.garages.api.GarageRegisteredEvent;
import axon.garages.api.RegisterGarageCmd;
import axon.garages.command.Garage;
import axon.util.GarageId;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import static org.axonframework.test.matchers.Matchers.*;

class GarageTest {

    private FixtureConfiguration<Garage> fixture;

    @BeforeEach
    void setup() {
        fixture = new AggregateTestFixture<>(Garage.class);
    }

    @Test
    void testGarageRegister() {
        fixture
            .given()
            .when(new RegisterGarageCmd(5))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<GarageRegisteredEvent>predicate(e -> e.getCapacity() == 5)),
                    andNoMore()
                )
            );
    }

    @ParameterizedTest
    @ValueSource(ints = { -5, -1, 0 })
    void testGarageRegisterFailWithZeroOrNegativeCapacity(int capacity) {
        fixture
            .given()
            .when(new RegisterGarageCmd(capacity))
            .expectException(IllegalArgumentException.class);
    }

    @Test
    void testParkingAllowedWhenFreeSlotsAreAvailable() {
        var gId = GarageId.generate().toString();
        var uId = Card.CardId.create().toString();
        fixture
            .given(new GarageRegisteredEvent(gId, 6))
            .when(new EnsureCapacityCmd(gId, uId))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<EntryAllowedEvent>predicate(e -> e.getGId().equals(gId) && e.getUId().equals(uId))),
                    andNoMore()
                )
            );
    }

    @Test
    void testNoParkingAllowedWhenNoFreeSlots() {
        var gId = GarageId.generate().toString();
        var uId = Card.CardId.create().toString();
        fixture
            .given(new GarageRegisteredEvent(gId, 0))
            .when(new EnsureCapacityCmd(gId, uId))
            .expectException(IllegalArgumentException.class);
    }

    @Test
    void testConfirmingEntryDecreasesCapacity() {
        var gId = GarageId.generate().toString();
        var uId = Card.CardId.create().toString();
        fixture
            .given(new GarageRegisteredEvent(gId, 5))
            .when(new ConfirmEntryCmd(gId, uId))
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<CapacityUpdatedEvent>predicate(e -> e.getUsed() == 4)),
                    messageWithPayload(Matchers.<EntryConfirmedEvent>predicate(e -> e.getGarageId().equals(gId) && e.getUid().equals(uId))),
                    andNoMore()
                )
            );
    }

    @Test
    void testConfirmingExitIncreasesCapacity() {
        var gId = GarageId.generate().toString();
        var uId = Card.CardId.create().toString();
        fixture
            .given(new GarageRegisteredEvent(gId, 5))
            .when(new ConfirmExitCmd(gId, uId))
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<CapacityUpdatedEvent>predicate(e -> e.getUsed() == 6)),
                    messageWithPayload(Matchers.<ExitConfirmedEvent>predicate(e -> e.getGarageId().equals(gId))),
                    andNoMore()
                )
            );
    }

}
