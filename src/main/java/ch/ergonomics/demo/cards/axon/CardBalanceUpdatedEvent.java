package ch.ergonomics.demo.cards.axon;

import lombok.Value;

@Value
public class CardBalanceUpdatedEvent {
    String uid;
    double balance;
}
