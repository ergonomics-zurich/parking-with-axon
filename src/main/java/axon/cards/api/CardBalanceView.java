package axon.cards.api;

import lombok.Value;

@Value
public class CardBalanceView {
    String cardId;
    double balance;
}
