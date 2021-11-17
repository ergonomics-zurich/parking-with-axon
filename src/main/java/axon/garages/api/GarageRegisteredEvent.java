package axon.garages.api;

import lombok.Value;
import org.axonframework.serialization.Revision;

@Value
@Revision("1")
public class GarageRegisteredEvent {
    String garageId;
    int capacity;
    int used;
}
