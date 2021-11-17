package axon.garages;

import axon.garages.api.GarageIdsQuery;
import axon.garages.api.GarageRegisteredEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class GarageIdsView {

    private final Set<String> ids = new LinkedHashSet<>();

    @QueryHandler
    public List<String> garageIds(GarageIdsQuery query) {
        return List.copyOf(ids);
    }

    @EventHandler
    public void on(GarageRegisteredEvent event) {
        ids.add(event.getGId());
    }
}
