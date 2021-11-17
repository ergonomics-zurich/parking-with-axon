package axon.util;

public final class PermitId {
    private PermitId() {
    }

    public static String generate() {
        return String.format("T%s", System.currentTimeMillis());
    }
}
