package axon.garages.query;

import axon.garages.api.GarageRegisteredEvent;
import axon.garages.api.BestGarageQuery;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Component
public class MostFreeGaragesView {

    private final TreeSet<GarageModel> capacitySortedIds = new TreeSet<>(GarageModel::compareTo);

    @QueryHandler
    public List<String> mostFreeIds(BestGarageQuery query) {
        return capacitySortedIds.stream().map(GarageModel::getId).collect(Collectors.toList());
    }

    @EventHandler
    public void on(GarageRegisteredEvent event) {
        capacitySortedIds.add(new GarageModel(event.getGid(), event.getCapacity()));
    }

    @EventHandler
    public void on(CapacityUpdatedEvent event) {
        final GarageModel garage = new GarageModel(event.getGid(), event.getUsed());
        capacitySortedIds.remove(garage);
        capacitySortedIds.add(garage);
    }

    @Value
    private static class GarageModel implements Comparable<GarageModel> {

        String id;

        @EqualsAndHashCode.Exclude Integer capacity;

        @Override
        public int compareTo(final GarageModel other) {
            return other.getCapacity().compareTo(this.getCapacity());

        }
    }
}
