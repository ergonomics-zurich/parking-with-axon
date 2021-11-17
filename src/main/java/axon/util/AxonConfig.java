package axon.util;

import org.axonframework.eventsourcing.AggregateLoadTimeSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AxonConfig {

    @Bean
    public SnapshotTriggerDefinition cardSnapshotTrigger(Snapshotter snapshotter) {
        return new AggregateLoadTimeSnapshotTriggerDefinition(snapshotter, 10);
    }

    @Bean
    public SnapshotTriggerDefinition garageSnapshotTrigger(Snapshotter snapshotter) {
        return new EventCountSnapshotTriggerDefinition(snapshotter, 30);
    }

}
