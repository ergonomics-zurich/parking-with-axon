package axon.web;

import axon.cards.api.InvalidatePermitCmd;
import axon.cards.api.IssuePermitCmd;
import axon.cards.api.PayOutstandingCmd;
import axon.garages.api.*;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.springframework.web.bind.annotation.*;
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
    public Mono<List<GarageView>> garages() {
        return reactorQueryGateway.query(
            new AllGaragesQuery(),
            ResponseTypes.multipleInstancesOf(GarageView.class)
        );
    }

    @PostMapping("/garages")
    public Mono<String> garages(@RequestParam(name = "capacity", defaultValue = "25") int capacity,
                                @RequestParam(name = "used", defaultValue = "0") int used) {
        return reactorCommandGateway.send(
            new RegisterGarageCmd(capacity, used)
        );
    }

    @GetMapping(path = "/garages/best")
    public Mono<GarageView> bestGarages() {
        return reactorQueryGateway.query(
            new BestGarageQuery(),
            ResponseTypes.instanceOf(GarageView.class)
        );
    }

    @PostMapping(path = "/garages/{gid}/request-entry/{uid}")
    public Mono<Boolean> requestEntry(@PathVariable String gid, @PathVariable String uid) {
        return reactorCommandGateway.send(new EnsureCapacityCmd(gid, uid));
    }

    @PostMapping(path = "/garages/{gid}/confirm-entry/{uid}")
    public Mono<Void> confirmEntry(@PathVariable String gid, @PathVariable String uid) {
        return reactorCommandGateway.send(new IssuePermitCmd(uid, gid));
    }

    @PostMapping(path = "/garages/{gid}/request-exit/{uid}")
    public Mono<Boolean> requestExit(@PathVariable String gid, @PathVariable String uid) {
        return reactorCommandGateway.send(new PayOutstandingCmd(uid, gid));
    }

    @PostMapping(path = "/garages/{gid}/confirm-exit/{uid}")
    public Mono<Void> confirmExit(@PathVariable String gid, @PathVariable String uid) {
        return reactorCommandGateway.send(new InvalidatePermitCmd(uid, gid));
    }

}