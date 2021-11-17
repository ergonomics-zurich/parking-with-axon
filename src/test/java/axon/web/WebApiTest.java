package axon.web;

import org.awaitility.Awaitility;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.concurrent.TimeUnit;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WebApiTest extends AbstractContainerBaseTest {

    @DynamicPropertySource
    static void axonServerProperties(DynamicPropertyRegistry registry) {
        registry.add("axon.axonserver.servers", () -> String.format("%s:%s", AXON_SERVER.getHost(), AXON_SERVER.getMappedPort(8124)));
    }

    @Autowired WebTestClient client;
    @Autowired CommandGateway gateway;

    String garage;
    String card;
    double credit;

    @Test
    @Order(1)
    void testGaragesEndpointReturnsEmpty() {
        client
            .get()
            .uri("/garages")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.length()")
            .isEqualTo(0);
    }

    @Test
    @Order(2)
    void testCardsEndpointReturnsEmpty() {
        client
            .get()
            .uri("/cards")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.length()")
            .isEqualTo(0);
    }

    @Test
    @Order(3)
    void testRegisterGaragesEndpointWorks() {
        garage =
            client
                .post()
                .uri("/garages?capacity=1&used=0")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
    }

    @Test
    @Order(4)
    void testRegisterCarEndpointWorks() {
        card =
            client
                .post()
                .uri("/cards")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
    }

    @Test
    @Order(5)
    void testGaragesEndpointReturnsOneGarage() {
        client
            .get()
            .uri("/garages")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.length()")
            .isEqualTo(1);
    }

    @Test
    @Order(6)
    void testCardsEndpointReturnsOneCard() {
        client
            .get()
            .uri("/cards")
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .jsonPath("$.length()")
            .isEqualTo(1);
    }

    @Test
    @Order(7)
    void testRequestEntryWorksWithFreeCapacity() {
        client
            .post()
            .uri("/garages/{0}/request-entry/{1}", garage, card)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Boolean.class)
            .isEqualTo(true);
    }

    @Test
    @Order(8)
    void testConfirmEntryWorks() {
        client
            .post()
            .uri("/garages/{0}/confirm-entry/{1}", garage, card)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .isEmpty();

    }

    @Test
    @Order(9)
    void testConfirmUsedSpacesToBeUpdated() {
        Awaitility
            .await()
            .pollDelay(2, TimeUnit.SECONDS)
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() ->
                client
                    .get()
                    .uri("/garages")
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$[0].used")
                    .isEqualTo(1)
            );
    }

    @Test
    @Order(10)
    void testRequestEntryAgainIsRefused() {
        client
            .post()
            .uri("/garages/{0}/request-entry/{1}", garage, card)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Boolean.class)
            .isEqualTo(false);
    }

    @Test
    @Order(11)
    void testConfirmingEntryAgainFails() {
        client
            .post()
            .uri("/garages/{0}/confirm-entry/{1}", garage, card)
            .exchange()
            .expectStatus()
            .is5xxServerError();
    }

    @Test
    @Order(12)
    void testRequestExitRefusedWithNotEnoughCredit() {
        Awaitility
            .with()
            .pollDelay(2, TimeUnit.SECONDS)
            .atMost(5, TimeUnit.SECONDS)
            .await()
            .untilAsserted(() ->
                client
                    .post()
                    .uri("/garages/{0}/request-exit/{1}", garage, card)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody(Boolean.class)
                    .isEqualTo(false)
            );
    }

    @Test
    @Order(13)
    void testCardRechargeWorks() {
        client
            .post()
            .uri("/cards/{0}/credit/{1}", card, 5)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .isEmpty();
    }

    @Test
    @Order(14)
    void testRequestExitWorksWithEnoughCredit() {
        client
            .post()
            .uri("/garages/{0}/request-exit/{1}", garage, card)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Boolean.class)
            .isEqualTo(true);
    }

    @Test
    @Order(15)
    void testCreditReducedAfterExitRequest() {
        Awaitility
            .await()
            .pollDelay(2, TimeUnit.SECONDS)
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() ->
                client
                    .get()
                    .uri("/cards", card)
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .expectBody()
                    .jsonPath("$[0].balance")
                    .value(Matchers.lessThan(5.0))
                    .jsonPath("$[0].balance")
                    .value(v -> credit = (double) v)
            );
    }

    @Test
    @Order(16)
    void testRequestExitAgainWorks() {
        client
            .post()
            .uri("/garages/{0}/request-exit/{1}", garage, card)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody(Boolean.class)
            .isEqualTo(true);
    }

    @Test
    @Order(17)
    void testCreditReducedAfterRepeatedExitRequest() {
        Awaitility
            .await()
            .pollDelay(5, TimeUnit.SECONDS)
            .atMost(10, TimeUnit.SECONDS)
            .untilAsserted(() ->
               client
                   .get()
                   .uri("/cards", card)
                   .exchange()
                   .expectStatus()
                   .isOk()
                   .expectBody()
                   .jsonPath("$[0].balance")
                   .value(Matchers.lessThan(credit))
            );
    }

    @Test
    @Order(18)
    void testConfirmExitWorks() {
        client
            .post()
            .uri("/garages/{0}/confirm-exit/{1}", garage, card)
            .exchange()
            .expectStatus()
            .isOk()
            .expectBody()
            .isEmpty();
    }

    @Test
    @Order(19)
    void testConfirmExitAgainFails() {
        client
            .post()
            .uri("/garages/{0}/confirm-exit/{1}", garage, card)
            .exchange()
            .expectStatus()
            .is5xxServerError();
    }

    @Test
    @Order(20)
    void testConfirmExitUpdatesUsedSpaces() {
        Awaitility
            .await()
            .pollDelay(2, TimeUnit.SECONDS)
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() ->
               client
                   .get()
                   .uri("/garages")
                   .exchange()
                   .expectStatus()
                   .isOk()
                   .expectBody()
                   .jsonPath("$[0].used")
                   .isEqualTo(0)
            );
    }

}
