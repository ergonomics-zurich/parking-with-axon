package ch.ergonomics.demo.garages.api;

import lombok.Value;

@Value
public class EntryAllowedEvent {
    String garageId;
    String cardId;
}
