/* Parking with Axon Demo App
 * This is part of Ergonomics AG's code example for the Axon Framework Workshop Nov 21.
 * Feel free to contact us to discuss your event-driven needs at axon.consulting@ergonomics.ch.
 */
package axon.backoffice.query;

import axon.backoffice.api.ActivePermitsQuery;
import axon.backoffice.api.PermitView;
import axon.cards.api.PermitInvalidatedEvent;
import axon.cards.api.PermitIssuedEvent;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ActivePermitsProjection {
    private final Map<String, PermitView> permits = new HashMap<>();

    @QueryHandler
    public List<PermitView> activePermits(ActivePermitsQuery query) {
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
