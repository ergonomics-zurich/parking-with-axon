package axon.cards.api;

import lombok.Value;

@Value
public class TicketInvalidatedEvent {
    String gId;
    String uId;
}
