package axon.cards.api;

import lombok.Getter;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class IssuePermitCmd {
    @Getter(onMethod_ = {@TargetAggregateIdentifier()})
    String cardId;
    String garageId;
}
