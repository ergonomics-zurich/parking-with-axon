package axon.cards.api;

import lombok.Value;
import org.axonframework.serialization.Revision;

@Value
@Revision("1")
public class CardIssuedEvent {
    String cardId;
}
