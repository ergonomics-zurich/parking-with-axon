package ch.ergonomics.demo.cards.api;

import lombok.Value;

import java.time.Instant;

@Value
public class TicketIssuedEvent {
    String gid;
    Instant start;
}
