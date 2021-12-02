/*
 * Parking with Axon Demo App
 *
 * This is part of Ergonomics's code example for the Axon Framework Workshop Nov 21.
 * Ergonomics AG can be found at: https://ergonomics.ch/
 * Feel free to contact us to discuss your event-driven needs at axon.consulting@ergonomics.ch.
 */
package axon.garages.api;

import lombok.Value;
import org.axonframework.serialization.Revision;

@Value
@Revision("1")
public class GarageRegisteredEvent {
    String garageId;
    int capacity;
    int used;
}
