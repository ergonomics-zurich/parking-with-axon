package axon.cards.api;

import axon.cards.command.Permit;
import lombok.Value;

import java.time.Instant;

@Value
public class PermitInvalidatedEvent {
    Permit permit;
    Instant invalidatedAt;
}
