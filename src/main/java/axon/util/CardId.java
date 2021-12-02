/*
 * Parking with Axon Demo App
 *
 * This is part of Ergonomics's code example for the Axon Framework Workshop Nov 21.
 * Ergonomics AG can be found at: https://ergonomics.ch/
 * Feel free to contact us to discuss your event-driven needs at axon.consulting@ergonomics.ch.
 */
package axon.util;

import java.security.SecureRandom;

public final class CardId {
    private static final SecureRandom secureRandom = new SecureRandom();

    private CardId() {
    }

    public static String generate() {
        var sb = new StringBuilder();
        while (sb.length() < 14) {
            sb.append(Integer.toHexString(secureRandom.nextInt(16)));
        }
        return sb.toString();
    }
}
