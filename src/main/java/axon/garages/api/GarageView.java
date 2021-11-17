package axon.garages.api;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GarageView implements Comparable<GarageView> {
    String garageId;
    Integer capacity;
    Integer used;

    private double calcFill() {
        return 1.0 * used / capacity;
    }

    @Override
    public int compareTo(final GarageView other) {
        return Double.compare(this.calcFill(), other.calcFill());
    }
}
