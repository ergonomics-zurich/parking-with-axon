package ch.ergonomics.demo.garages.api;

import lombok.Value;

@Value
public class ExitNotAllowedEvent {
    String garageId;
}