package ch.ergonomics.demo.cards.api;

import lombok.Value;

import java.time.Instant;

@Value
public class TicketPaidEvent {
    String gId;
    Instant stop;
    double price;
}
