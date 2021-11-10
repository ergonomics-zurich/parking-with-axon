package ch.ergonomics.demo.cards.axon;

import lombok.Getter;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class CreditCmd {
    @Getter(onMethod_ = {@TargetAggregateIdentifier()})
    String uid;
    double credit;
}
