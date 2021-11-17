package axon.garages.api;

import lombok.Value;

@Value
public class GarageRegisteredEvent {
    String gid;
    int capacity;
    int used;
}
