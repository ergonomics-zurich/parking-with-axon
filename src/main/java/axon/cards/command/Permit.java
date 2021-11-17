package axon.cards.command;

import lombok.Value;

import java.time.Duration;
import java.time.Instant;

@Value
public class Permit {
    String permitId;
    String cardId;
    String garageId;
    Instant issuedAt;

    public double calcPrice() {
        return 0.015 * Duration.between(this.issuedAt, Instant.now()).toSeconds();
    }
}
