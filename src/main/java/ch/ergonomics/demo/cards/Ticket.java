package ch.ergonomics.demo.cards;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Ticket {
    final String ticketId;
    final Instant start;
    @EqualsAndHashCode.Exclude double amountPaid = 0.0;
    @EqualsAndHashCode.Exclude Instant stop = null;

    public static Ticket create(Instant start) {
        return new Ticket(TicketId.create().toString(), start);
    }

    static class TicketId {
        private final String id;

        private TicketId(String id) {
            this.id = id;
        }

        public static TicketId create() {
            return new TicketId(UUID.randomUUID().toString());
        }

        @Override
        public String toString() {
            return id;
        }
    }
}
