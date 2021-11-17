package axon.backoffice.api;

import axon.cards.command.Permit;
import lombok.Value;

@Value
public class PermitView {
    Permit permit;

    public double getCurrentPrice() {
        return permit.calcPrice();
    }
}
