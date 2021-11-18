package axon.web;

import axon.backoffice.api.ActivePermitsQuery;
import axon.backoffice.api.PermitView;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class BackofficeApi {

    final QueryGateway queryGateway;

    public BackofficeApi(QueryGateway queryGateway) {
        this.queryGateway = queryGateway;
    }

    @GetMapping("/backoffice/active-permits")
    public CompletableFuture<List<PermitView>> activePermits() {
        return queryGateway.query(new ActivePermitsQuery(), ResponseTypes.multipleInstancesOf(PermitView.class));
    }
}
