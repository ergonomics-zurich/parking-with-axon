package axon.garages.query;

import axon.garages.api.*;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BestGarageProjection {
    private final PriorityQueue<GarageView> garages = new PriorityQueue<>(GarageView::compareTo);
    private final Map<String, GarageView> garageById = new HashMap<>();

    @QueryHandler
    public GarageView handle(BestGarageQuery query) {
        return garages.peek();
    }

    @EventHandler
    public void on(GarageRegisteredEvent event) {
        var garage = new GarageView(event.getGid(), event.getCapacity(), event.getUsed());
        garageById.put(event.getGid(), garage);
        garages.add(garage);
    }

    @EventHandler
    public void on(EntryConfirmedEvent event) {
        var garage = garageById.get(event.getGid());
        garages.remove(garage);
        garage.setUsed(garage.getUsed() + 1);
        garages.add(garage);
    }

    @EventHandler
    public void on(ExitConfirmedEvent event) {
        var garage = garageById.get(event.getGid());
        garages.remove(garage);
        garage.setUsed(garage.getUsed() - 1);
        garages.add(garage);
    }
}
