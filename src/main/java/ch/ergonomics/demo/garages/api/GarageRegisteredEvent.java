package ch.ergonomics.demo.garages.api;

import lombok.Value;

@Value
public class GarageRegisteredEvent {
    String garageId;
    int capacity;
}
