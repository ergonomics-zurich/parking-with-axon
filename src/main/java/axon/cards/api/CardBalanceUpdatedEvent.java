package axon.cards.api;

import lombok.Value;

@Value
public class CardBalanceUpdatedEvent {
    String uid;
    double balance;
}
