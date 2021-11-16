package ch.ergonomics.demo.tickets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

import static com.google.common.hash.Hashing.sha256;
import static java.nio.charset.StandardCharsets.UTF_8;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Ticket {
    final String ticketId;
    final Instant start;
    @EqualsAndHashCode.Exclude double amountPaid = 0.0;
    @EqualsAndHashCode.Exclude Instant stop = null;

    public static Ticket create(String gId, String uId, Instant start) {
        return new Ticket(TicketId.create(gId, uId, start.getEpochSecond()).toString(), start);
    }

    static class TicketId {
        private final String id;

        private TicketId(String id) {
            this.id = id;
        }

        @SuppressWarnings("UnstableApiUsage")
        public static TicketId create(String gId, String uId, Long ts) {
            return new TicketId(String.format("T%s", sha256().newHasher().putString(gId, UTF_8).putString(uId, UTF_8).putLong(ts).hash()));
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
