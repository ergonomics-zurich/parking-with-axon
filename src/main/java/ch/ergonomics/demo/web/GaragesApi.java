package ch.ergonomics.demo.web;

import ch.ergonomics.demo.garages.api.ConfirmEntryCmd;
import ch.ergonomics.demo.garages.api.ConfirmExitCmd;
import ch.ergonomics.demo.garages.api.GarageIdsQuery;
import ch.ergonomics.demo.garages.api.MostFreeGaragesQuery;
import ch.ergonomics.demo.garages.api.RequestEntryCmd;
import ch.ergonomics.demo.garages.api.RequestExitCmd;
import org.axonframework.extensions.reactor.commandhandling.gateway.ReactorCommandGateway;
import org.axonframework.extensions.reactor.queryhandling.gateway.ReactorQueryGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

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
    public void requestEntry(@PathVariable String gid, @PathVariable String uid) {
        reactorCommandGateway.send(new RequestEntryCmd(gid, uid));
    }

    @PostMapping(path = "/garages/{gid}/confirm-entry/{uid}")
    public void confirmEntry(@PathVariable String gid, @PathVariable String uid) {
        reactorCommandGateway.send(new ConfirmEntryCmd(gid, uid));
    }

    @PostMapping(path = "/garages/{gid}/request-exit/{uid}")
    public void requestExit(@PathVariable String gid, @PathVariable String uid) {
        reactorCommandGateway.send(new RequestExitCmd(gid, uid));
    }

    @PostMapping(path = "/garages/{gid}/confirm-exit/{uid}")
    public void confirmExit(@PathVariable String gid, @PathVariable String uid) {
        reactorCommandGateway.send(new ConfirmExitCmd(gid, uid));
    }

}