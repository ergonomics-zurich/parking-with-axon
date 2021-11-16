package ch.ergonomics.demo.garages;

import ch.ergonomics.demo.garages.api.GarageIdsQuery;
import ch.ergonomics.demo.garages.api.GarageRegisteredEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

@Component
public class GarageIdsView {

    private final SortedSet<String> ids = new TreeSet<>();

    @QueryHandler
    public List<String> garageIds(GarageIdsQuery query) {
        return List.copyOf(ids);
    }

    @EventHandler
    public void on(GarageRegisteredEvent event) {
        ids.add(event.getGarageId());
    }
}
