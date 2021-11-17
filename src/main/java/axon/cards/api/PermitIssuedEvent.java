package axon.cards.api;

import axon.cards.command.Permit;
import lombok.Value;
import org.axonframework.serialization.Revision;

@Value
@Revision("1")
public class PermitIssuedEvent {
    Permit permit;
}
