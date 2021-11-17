package axon.cards.api;

import axon.cards.command.Permit;
import lombok.Value;

@Value
public class PermitIssuedEvent {
    Permit permit;
}
