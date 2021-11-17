package axon.cards.api;

import lombok.Value;

@Value
public class CardRechargedEvent {
    String cardId;
    double amount;
}
