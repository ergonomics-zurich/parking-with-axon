package axon.web;

import axon.backoffice.api.ActivePermitsQuery;
import axon.backoffice.api.PermitView;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BackofficeApi {
    @Autowired
    QueryGateway queryGateway;

    @GetMapping("/backoffice/active-permits")
    public List<PermitView> activePermits() {
        return queryGateway
            .query(new ActivePermitsQuery(), ResponseTypes.multipleInstancesOf(PermitView.class))
            .join();
    }
}
