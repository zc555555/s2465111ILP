package uk.ac.ed.inf.pizzadrone.data;

public class IsInRegionRequestForTest {
    private PositionForTest position;
    private RegionForTest region;

    public IsInRegionRequestForTest(PositionForTest position, RegionForTest region) {
        this.position = position;
        this.region = region;
    }

    public PositionForTest getPosition() {
        return position;
    }

    public RegionForTest getRegion() {
        return region;
    }
}

