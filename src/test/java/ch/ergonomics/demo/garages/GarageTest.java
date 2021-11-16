package ch.ergonomics.demo.garages;

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

}
