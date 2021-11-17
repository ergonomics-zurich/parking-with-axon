package axon.cards.api;

import lombok.Value;
import org.axonframework.serialization.Revision;

import java.time.Instant;

@Value
@Revision("1")
public class PaymentEvent {
    String cardId;
    String permitId;
    Instant paidAt;
    double amount;
}
