package axon.cards.api;

import lombok.Getter;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.time.Instant;

@Value
public class PayTicketCmd {
    @Getter(onMethod_ = {@TargetAggregateIdentifier()})
    String uid;
    String gid;
    Instant stop;
}
