package axon.cards.api;

import lombok.Value;

import java.time.Instant;

@Value
public class PaymentEvent {
    String cardId;
    String permitId;
    Instant paidAt;
    double amount;
}
