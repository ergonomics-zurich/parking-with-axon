/* Parking with Axon Demo App
 * This is part of Ergonomics AG's code example for the Axon Framework Workshop Nov 21.
 * Feel free to contact us to discuss your event-driven needs at axon.consulting@ergonomics.ch.
 */
package axon.backoffice.api;

import axon.cards.command.Permit;
import lombok.Value;

@Value
public class PermitView {
    Permit permit;

    public double getCurrentPrice() {
        return permit.calcPrice();
    }
}
