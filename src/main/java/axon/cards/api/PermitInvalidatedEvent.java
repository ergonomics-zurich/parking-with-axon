package axon.cards.api;

import axon.cards.command.Permit;
import lombok.Value;
import org.axonframework.serialization.Revision;

import java.time.Instant;

@Value
@Revision("1")
public class PermitInvalidatedEvent {
    Permit permit;
    Instant invalidatedAt;
}
