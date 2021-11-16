package ch.ergonomics.demo.garages.api;

import lombok.Value;

import java.time.Instant;

@Value
public class EntryConfirmedEvent {
    String gId;
    String cId;
    Instant start;
}
