/* Parking with Axon Demo App
 * This is part of Ergonomics AG's code example for the Axon Framework Workshop Nov 21.
 * Feel free to contact us to discuss your event-driven needs at axon.consulting@ergonomics.ch.
 */
package axon.garages;

import axon.garages.api.EnsureCapacityCmd;
import axon.garages.api.GarageRegisteredEvent;
import axon.garages.api.RegisterGarageCmd;
import axon.garages.command.Garage;
import axon.util.CardId;
import axon.util.GarageId;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.axonframework.test.matchers.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
            .when(new RegisterGarageCmd(5, 2))
            .expectSuccessfulHandlerExecution()
            .expectEventsMatching(
                exactSequenceOf(
                    messageWithPayload(Matchers.<GarageRegisteredEvent>predicate(e -> e.getCapacity() == 5 && e.getUsed() == 2)),
                    andNoMore()
                )
            );
    }

    @Test
    void testGarageRegisterFailWithNegativeCapacity() {
        fixture
            .given()
            .when(new RegisterGarageCmd(-1, 0))
            .expectException(IllegalArgumentException.class);
    }

    @Test
    void testParkingAllowedWhenFreeSlotsAreAvailable() {
        var gId = GarageId.generate();
        var uId = CardId.generate();
        fixture
            .given(new GarageRegisteredEvent(gId, 6, 1))
            .when(new EnsureCapacityCmd(gId, uId))
            .expectSuccessfulHandlerExecution()
            .expectResultMessagePayloadMatching(Matchers.equalTo(true))
            .expectNoEvents();
    }

    @Test
    void testNoParkingAllowedWhenNoFreeSlots() {
        var gId = GarageId.generate();
        var uId = CardId.generate();
        fixture
            .given(new GarageRegisteredEvent(gId, 5, 5))
            .when(new EnsureCapacityCmd(gId, uId))
            .expectResultMessagePayloadMatching(Matchers.equalTo(false))
            .expectNoEvents();
    }

}
