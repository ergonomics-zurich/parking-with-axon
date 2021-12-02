/* Parking with Axon Demo App
 * This is part of Ergonomics AG's code example for the Axon Framework Workshop Nov 21.
 * Feel free to contact us to discuss your event-driven needs at axon.consulting@ergonomics.ch.
 */
package axon;

import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventsourcing.AggregateLoadTimeSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public void configureSubscribingProcessors(EventProcessingConfigurer processingConfigurer) {
        processingConfigurer.registerSubscribingEventProcessor("ticket-events-policy");
    }
}
