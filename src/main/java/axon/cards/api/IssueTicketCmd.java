package ch.ergonomics.demo.cards.api;

import lombok.Getter;
import lombok.Value;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.time.Instant;

@Value
public class IssueTicketCmd {
    @Getter(onMethod_ = {@TargetAggregateIdentifier()})
    String uid;
    String gid;
    Instant start;
}
