package ch.ergonomics.demo.garages.api;

import lombok.Value;

import java.time.Instant;

@Value
public class EntryConfirmedEvent {
    String ticketId;
    String garageId;
    String cardId;
    Double ratePerMinute;
    Instant start;
}
