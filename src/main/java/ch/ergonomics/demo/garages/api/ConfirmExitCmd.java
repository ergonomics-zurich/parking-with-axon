package ch.ergonomics.demo.garages.api;

import lombok.Getter;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class ConfirmExitCmd {
    @Getter(onMethod_ = {@TargetAggregateIdentifier()}) String gId;
    String uId;
}
