package uk.ac.ed.inf.pizzadrone.data;

import uk.ac.ed.inf.pizzadrone.data.PositionForTest;

public class LngLatPairRequestForTest {
    private uk.ac.ed.inf.pizzadrone.data.PositionForTest position1;
    private uk.ac.ed.inf.pizzadrone.data.PositionForTest position2;

    public LngLatPairRequestForTest(PositionForTest position1, PositionForTest position2) {
        this.position1 = position1;
        this.position2 = position2;
    }
    // Getters and setters
    public uk.ac.ed.inf.pizzadrone.data.PositionForTest getPosition1() {
        return position1;
    }

    public PositionForTest getPosition2() {
        return position2;
    }
}

