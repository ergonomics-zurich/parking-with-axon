package axon.cards.api;

import lombok.Getter;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class RechargeCardCmd {
    @Getter(onMethod_ = {@TargetAggregateIdentifier()})
    String cardId;
    double amount;
}
