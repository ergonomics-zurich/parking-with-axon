/* Parking with Axon Demo App
 * This is part of Ergonomics AG's code example for the Axon Framework Workshop Nov 21.
 * Feel free to contact us to discuss your event-driven needs at axon.consulting@ergonomics.ch.
 */
package axon.util;

public final class PermitId {
    private PermitId() {
    }

    public static String generate() {
        return String.format("T%s", System.currentTimeMillis());
    }
}
