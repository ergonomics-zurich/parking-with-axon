package ch.ergonomics.demo.cards.api;

import lombok.Getter;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class CreditCmd {
    @Getter(onMethod_ = {@TargetAggregateIdentifier()})
    String uid;
    double credit;
}
