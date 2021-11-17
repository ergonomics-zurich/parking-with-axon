package axon.garages.api;

import lombok.Value;

@Value
public class ExitConfirmedEvent {
    String gid;
    String uid;
}
