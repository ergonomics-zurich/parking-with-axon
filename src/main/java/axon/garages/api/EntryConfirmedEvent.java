package axon.garages.api;

import lombok.Value;

@Value
public class EntryConfirmedEvent {
    String garageId;
}
