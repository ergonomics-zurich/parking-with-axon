/*
 * Parking with Axon Demo App
 *
 * This is part of Ergonomics's code example for the Axon Framework Workshop Nov 21.
 * Ergonomics AG can be found at: https://ergonomics.ch/
 * Feel free to contact us to discuss your event-driven needs at axon.consulting@ergonomics.ch.
 */
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
