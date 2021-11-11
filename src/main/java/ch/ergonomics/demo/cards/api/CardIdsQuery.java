package ch.ergonomics.demo.cards.api;

import lombok.Value;

@Value
public class CardIdsQuery {
    String fromId;
    String toId;
}
