package ch.ergonomics.demo.garages.api;

import lombok.Value;

@Value
public class CapacityUpdatedEvent {
    String garageId;
    Integer capacity;
}