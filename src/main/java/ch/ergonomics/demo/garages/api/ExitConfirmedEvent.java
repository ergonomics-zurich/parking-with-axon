package ch.ergonomics.demo.garages.api;

import lombok.Value;

@Value
public class ExitConfirmedEvent {
    String garageId;
}
