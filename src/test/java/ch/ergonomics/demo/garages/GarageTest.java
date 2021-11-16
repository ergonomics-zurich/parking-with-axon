package ch.ergonomics.demo.garages;

import ch.ergonomics.demo.cards.Card;
import ch.ergonomics.demo.garages.api.CapacityUpdatedEvent;
import ch.ergonomics.demo.garages.api.ConfirmEntryCmd;
import ch.ergonomics.demo.garages.api.ConfirmExitCmd;
import ch.ergonomics.demo.garages.api.EnsureCapacityCmd;
import ch.ergonomics.demo.garages.api.EntryAllowedEvent;
import ch.ergonomics.demo.garages.api.EntryConfirmedEvent;
import ch.ergonomics.demo.garages.api.ExitConfirmedEvent;
import ch.ergonomics.demo.garages.api.GarageRegisteredEvent;
import ch.ergonomics.demo.garages.api.RegisterGarageCmd;
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
        var gId = Garage.GarageId.create().toString();
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
        var gId = Garage.GarageId.create().toString();
        var uId = Card.CardId.create().toString();
        fixture
            .given(new GarageRegisteredEvent(gId, 0))
            .when(new EnsureCapacityCmd(gId, uId))
            .expectException(IllegalArgumentException.class);
    }

    @Test
    void testConfirmingEntryDecreasesCapacity() {
        var gId = Garage.GarageId.create().toString();
        var uId = Card.CardId.create().toString();
        fixture
            .given(new GarageRegisteredEvent(gId, 5))
            .when(new ConfirmEntryCmd(gId, uId))
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<CapacityUpdatedEvent>predicate(e -> e.getCapacity() == 4)),
                    messageWithPayload(Matchers.<EntryConfirmedEvent>predicate(e -> e.getGId().equals(gId) && e.getUId().equals(uId))),
                    andNoMore()
                )
            );
    }

    @Test
    void testConfirmingExitIncreasesCapacity() {
        var gId = Garage.GarageId.create().toString();
        var uId = Card.CardId.create().toString();
        fixture
            .given(new GarageRegisteredEvent(gId, 5))
            .when(new ConfirmExitCmd(gId, uId))
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<CapacityUpdatedEvent>predicate(e -> e.getCapacity() == 6)),
                    messageWithPayload(Matchers.<ExitConfirmedEvent>predicate(e -> e.getGId().equals(gId))),
                    andNoMore()
                )
            );
    }

}
