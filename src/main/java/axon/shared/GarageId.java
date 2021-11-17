package axon.shared;

import java.security.SecureRandom;

public class GarageId {
    private final String id;

    private GarageId(String id) {
        this.id = id;
    }

    public static GarageId create() {
        return new GarageId("G" + (1_000_000 + new SecureRandom().nextInt(8_000_000)));
    }

    @Override
    public String toString() {
        return id;
    }
}
