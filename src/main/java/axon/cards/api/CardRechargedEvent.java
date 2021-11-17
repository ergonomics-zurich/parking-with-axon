package axon.cards.api;

import lombok.Value;
import org.axonframework.serialization.Revision;

@Value
@Revision("1")
public class CardRechargedEvent {
    String cardId;
    double amount;
}
