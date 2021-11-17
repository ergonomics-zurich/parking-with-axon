package axon.web;

import axon.cards.api.InvalidateTicketCmd;
import axon.cards.api.IssueTicketCmd;
import axon.cards.api.PayTicketCmd;
import axon.garages.api.GarageIdsQuery;
import axon.garages.api.MostFreeGaragesQuery;
import axon.garages.api.EnsureCapacityCmd;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@RestController
public class GaragesApi {

    private final ReactorCommandGateway reactorCommandGateway;

    private final ReactorQueryGateway reactorQueryGateway;

    public GaragesApi(ReactorCommandGateway reactorCommandGateway, ReactorQueryGateway reactorQueryGateway) {
        this.reactorCommandGateway = reactorCommandGateway;
        this.reactorQueryGateway = reactorQueryGateway;
    }

    @GetMapping("/garages")
    public Mono<List<String>> garages() {
        return reactorQueryGateway.query(
                new GarageIdsQuery(),
                ResponseTypes.multipleInstancesOf(String.class)
        );
    }

    @GetMapping(path = "/garages/best")
    public Mono<List<String>> bestGarages() {
        return reactorQueryGateway.query(
                new MostFreeGaragesQuery(),
                ResponseTypes.multipleInstancesOf(String.class)
        );
    }

    @PostMapping(path = "/garages/{gid}/request-entry/{uid}")
    public Mono<Boolean> requestEntry(@PathVariable String gid, @PathVariable String uid) {
        return
            reactorCommandGateway
                .send(new EnsureCapacityCmd(gid, uid))
                .map(o -> true)
                .onErrorReturn(false);
    }

    @PostMapping(path = "/garages/{gid}/confirm-entry/{uid}")
    public Mono<Void> confirmEntry(@PathVariable String gid, @PathVariable String uid) {
        return reactorCommandGateway.send(new IssueTicketCmd(uid, gid, Instant.now()));
    }

    @PostMapping(path = "/garages/{gid}/request-exit/{uid}")
    public Mono<Boolean> requestExit(@PathVariable String gid, @PathVariable String uid) {
        return
            reactorCommandGateway
                .send(new PayTicketCmd(uid, gid, Instant.now()))
                .map(o -> true)
                .onErrorReturn(false);
    }

    @PostMapping(path = "/garages/{gid}/confirm-exit/{uid}")
    public Mono<Void> confirmExit(@PathVariable String gid, @PathVariable String uid) {
        return reactorCommandGateway.send(new InvalidateTicketCmd(uid, gid));
    }

}