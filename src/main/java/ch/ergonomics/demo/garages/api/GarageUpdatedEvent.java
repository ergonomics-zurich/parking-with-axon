package ch.ergonomics.demo.garages.api;

import lombok.Value;

@Value
public class GarageUpdatedEvent {
    String garageId;
    Integer capacity;
}
