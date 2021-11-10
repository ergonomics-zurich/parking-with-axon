package ch.ergonomics.demo.cards.api;

import lombok.Value;

@Value
public class CardBalanceUpdatedEvent {
    String uid;
    double balance;
}
