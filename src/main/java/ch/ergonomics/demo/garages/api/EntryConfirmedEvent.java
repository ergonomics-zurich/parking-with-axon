package ch.ergonomics.demo.garages.api;

import lombok.Value;

@Value
public class EntryConfirmedEvent {
    String gId;
    String uId;
}
