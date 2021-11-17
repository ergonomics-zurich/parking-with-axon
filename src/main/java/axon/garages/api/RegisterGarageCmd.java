package axon.garages.api;

import lombok.Value;

@Value
public class RegisterGarageCmd {
    int capacity;
    int used;
}
