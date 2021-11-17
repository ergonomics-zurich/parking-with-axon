package axon.util;

import java.util.List;

public final class GarageId {
    private static final List<String> words = List.of(
        "alfa", "bravo", "charlie", "delta", "echo", "foxtrot", "golf", "hotel", "india", "juliet"
    );
    private static int next = -1;
    private GarageId() {
    }

    public static String generate() {
        if (next < words.size() - 1) {
            next += 1;
            return words.get(next);
        } else {
            next += 1;
            return String.format("G-%05d", next);
        }
    }
}
