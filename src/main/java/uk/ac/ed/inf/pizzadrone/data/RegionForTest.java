package uk.ac.ed.inf.pizzadrone.data;

import java.util.List;

public class RegionForTest {
    private String name;
    private List<PositionForTest> vertices;
    private boolean regionValid;

    public RegionForTest(String name, List<PositionForTest> vertices, boolean regionValid) {
        this.name = name;
        this.vertices = vertices;
        this.regionValid = regionValid;
    }

    public String getName() {
        return name;
    }

    public List<PositionForTest> getVertices() {
        return vertices;
    }

    public boolean isRegionValid() {
        if (vertices == null || vertices.size() < 3) {
            return false;
        }
        for (PositionForTest vertex : vertices) {
            if (vertex == null || !vertex.isValid()) {
                return false;
            }
        }
        return !allVerticesCollinear();
    }

    private boolean allVerticesCollinear() {
        int n = vertices.size();
        PositionForTest p1 = vertices.get(0);
        PositionForTest p2 = vertices.get(1);

        double dx = p2.getLng() - p1.getLng();
        double dy = p2.getLat() - p1.getLat();

        for (int i = 2; i < n; i++) {
            PositionForTest pi = vertices.get(i);
            double currentDx = pi.getLng() - p1.getLng();
            double currentDy = pi.getLat() - p1.getLat();
            if ((dx * currentDy) != (dy * currentDx)) {
                return false;
            }
        }
        return true;
    }
}
