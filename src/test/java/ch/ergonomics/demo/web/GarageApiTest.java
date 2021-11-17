package ch.ergonomics.demo.web;

import ch.ergonomics.demo.cards.api.IssueCardCmd;
import ch.ergonomics.demo.garages.api.RegisterGarageCmd;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GarageApiTest extends AbstractContainerBaseTest {

    @DynamicPropertySource
    static void axonServerProperties(DynamicPropertyRegistry registry) {
        registry.add("axon.axonserver.servers", () -> String.format("%s:%s", AXON_SERVER.getHost(), AXON_SERVER.getMappedPort(8124)));
    }

    @Autowired WebTestClient client;
    @Autowired CommandGateway gateway;

    String firstGarage;
    String secondGarage;
    String firstCard;
    String secondCard;

    @BeforeAll
    void setup() {
        firstGarage = gateway.sendAndWait(new RegisterGarageCmd(5));
        secondGarage = gateway.sendAndWait(new RegisterGarageCmd(6));
        firstCard = gateway.sendAndWait(new IssueCardCmd());
        secondCard = gateway.sendAndWait(new IssueCardCmd());
    }

    @Test
    void testGarageEndpoint() {
        client
            .get()
            .uri("/garages")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.[*]")
            .exists()
            .jsonPath("$.[*]")
            .isArray()
            .jsonPath("$.length()")
            .isEqualTo(2)
            .jsonPath("$.[0]")
            .isEqualTo(firstGarage)
            .jsonPath("$.[1]")
            .isEqualTo(secondGarage);

    }

    @Test
    void testBestGaragesEndpoint() {
        client
            .get()
            .uri("/garages/best")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.[*]")
            .exists()
            .jsonPath("$.[*]")
            .isArray()
            .jsonPath("$.length()")
            .isEqualTo(2)
            .jsonPath("$.[0]")
            .isEqualTo(secondGarage)
            .jsonPath("$.[1]")
            .isEqualTo(firstGarage);
    }

    @Test
    void testConfirmEntryReducesCapacity() {
        client
            .post()
            .uri("/garages/{0}/confirm-entry/{1}/", secondGarage, firstCard)
            .exchange()
            .expectStatus()
            .isOk();
        client
            .post()
            .uri("/garages/{0}/confirm-entry/{2}/", secondGarage, secondCard)
            .exchange()
            .expectStatus()
            .isOk();
        client
            .get()
            .uri("/garages/best")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.[*]")
            .exists()
            .jsonPath("$.[*]")
            .isArray()
            .jsonPath("$.length()")
            .isEqualTo(2)
            .jsonPath("$.[0]")
            .isEqualTo(firstGarage)
            .jsonPath("$.[1]")
            .isEqualTo(secondGarage);

    }
}
