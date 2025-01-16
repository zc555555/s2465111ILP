package uk.ac.ed.inf.pizzadrone.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.List;

public class NamedRegion {
    private final String name;
    private final List<LngLat> vertices;

    @JsonCreator
    public NamedRegion(String name, List<LngLat> vertices) {
        this.name = name;
        this.vertices = vertices;
    }

    public String getName() {
        return this.name;
    }

    public boolean containsPoint(LngLat point) {
        int crossings = 0;
        for (int i = 0; i < vertices.size(); i++) {
            LngLat p1 = vertices.get(i);
            LngLat p2 = vertices.get((i + 1) % vertices.size());

            if ((p1.getLat() <= point.getLat() && p2.getLat() > point.getLat()) ||
                    (p1.getLat() > point.getLat() && p2.getLat() <= point.getLat())) {
                double slope = (p2.getLng() - p1.getLng()) / (p2.getLat() - p1.getLat());
                double intersectionLng = p1.getLng() + slope * (point.getLat() - p1.getLat());
                if (intersectionLng > point.getLng()) {
                    crossings++;
                }
            }
        }
        return crossings % 2 == 1;
    }
}

