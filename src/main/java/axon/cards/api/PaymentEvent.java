/* Parking with Axon Demo App
 * This is part of Ergonomics AG's code example for the Axon Framework Workshop Nov 21.
 * Feel free to contact us to discuss your event-driven needs at axon.consulting@ergonomics.ch.
 */
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
