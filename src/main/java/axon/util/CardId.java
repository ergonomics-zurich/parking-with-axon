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
