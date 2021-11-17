package axon.backoffice.query;

import axon.backoffice.api.PermitView;
import axon.cards.api.PermitInvalidatedEvent;
import axon.cards.api.PermitIssuedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivePermitsProjection {

    private final Map<String, PermitView> permits = new HashMap<>();

    @QueryHandler
    public List<PermitView> activePermits() {
        return List.copyOf(this.permits.values());
    }

    @EventHandler
    public void on(PermitIssuedEvent event) {
        this.permits.put(event.getPermit().getPermitId(), new PermitView(event.getPermit()));
    }

    @EventHandler
    public void on(PermitInvalidatedEvent event) {
        this.permits.remove(event.getPermit().getPermitId());
    }
}
