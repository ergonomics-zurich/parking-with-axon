package ch.ergonomics.demo.garages.api;

import lombok.Value;

import java.time.Instant;

@Value
public class ExitRequestedEvent {
    String ticketId;
    Instant stop;
}
