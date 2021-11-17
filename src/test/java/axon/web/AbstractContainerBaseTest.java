package axon.web;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

abstract class AbstractContainerBaseTest {
    static final GenericContainer<?> AXON_SERVER;

    static {
        AXON_SERVER =
            new GenericContainer<>(DockerImageName.parse("axoniq/axonserver:latest"))
                .withExposedPorts(8024, 8124)
                .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(WebApiTest.class)).withSeparateOutputStreams())
                .waitingFor(Wait.forHttp("/v1/public/me").forPort(8024).forStatusCode(200));
        AXON_SERVER.start();
    }
}
