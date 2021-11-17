package axon.garages.api;

import lombok.Getter;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Value
public class ConfirmEntryCmd {
    @Getter(onMethod_ = {@TargetAggregateIdentifier()})
    String gid;
    String uid;
}
