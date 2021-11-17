package axon.cards.api;

import lombok.Value;

import java.time.Instant;

@Value
public class TicketIssuedEvent {
    String uid;
    String gid;
    Instant start;
}
