package uk.ac.ed.inf.pizzadrone.unitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ed.inf.pizzadrone.data.*;
import uk.ac.ed.inf.pizzadrone.constant.SystemConstants;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeliveryPathCalculatorTest {

    private DeliveryPathCalculator deliveryPathCalculator;
    private DataService dataServiceMock;

    @BeforeEach
    void setUp() {
        // Mock the DataService dependency
        dataServiceMock = mock(DataService.class);

        // Setup mock central area
        NamedRegion centralArea = new NamedRegion("central", List.of(
                new LngLat(-3.1878, 55.9449), // Top-left
                new LngLat(-3.1843, 55.9449), // Top-right
                new LngLat(-3.1843, 55.9422), // Bottom-right
                new LngLat(-3.1878, 55.9422)  // Bottom-left
        ));

        // Setup mock no-fly zones
        NamedRegion noFlyZone = new NamedRegion("noFlyZone", List.of(
                new LngLat(-3.1860, 55.9440),
                new LngLat(-3.1855, 55.9440),
                new LngLat(-3.1855, 55.9435),
                new LngLat(-3.1860, 55.9435)
        ));

        when(dataServiceMock.fetchCentralArea()).thenReturn(centralArea);
        when(dataServiceMock.fetchNoFlyZones()).thenReturn(List.of(noFlyZone));

        deliveryPathCalculator = new DeliveryPathCalculator(dataServiceMock);
    }

    @Test
    void testCalculateDeliveryPath_AvoidNoFlyZones() {

        LngLat startPosition = new LngLat(-3.1875, 55.9444); // Near top-left
        LngLat restaurantPosition = new LngLat(-3.1850, 55.9430); // Bottom-right of the central area


        List<LngLat> path = deliveryPathCalculator.calculateDeliveryPath(startPosition);


        assertNotNull(path);
        assertFalse(path.isEmpty());
        assertEquals(startPosition, path.get(0)); // Path starts at the given position
        assertTrue(path.get(path.size() - 1).isCloseTo(SystemConstants.APPLETON_TOWER)); // Ends near Appleton Tower


        NamedRegion noFlyZone = dataServiceMock.fetchNoFlyZones().get(0);
        assertTrue(path.stream().noneMatch(noFlyZone::containsPoint));
    }

    @Test
    void testCalculateDeliveryPath_CentralAreaBoundary() {

        LngLat startPosition = new LngLat(-3.1875, 55.9444);
        LngLat outsidePosition = new LngLat(-3.1830, 55.9450);


        List<LngLat> path = deliveryPathCalculator.calculateDeliveryPath(startPosition);


        assertNotNull(path);
        assertFalse(path.isEmpty());
        NamedRegion centralArea = dataServiceMock.fetchCentralArea();


        boolean hasExitedCentral = false;
        for (LngLat point : path) {
            if (centralArea.containsPoint(point)) {
                hasExitedCentral = true;
            } else {
                if (hasExitedCentral) {
                    fail("Path exits the central area after entering.");
                }
            }
        }
    }

    @Test
    void testHeuristic() {

        LngLat pointA = new LngLat(-3.1870, 55.9440);
        LngLat pointB = new LngLat(-3.1850, 55.9430);


        double heuristicValue = deliveryPathCalculator.heuristic(pointA, pointB);


        assertTrue(heuristicValue > 0);
    }

    @Test
    void testValidMoves() {

        LngLat currentPosition = new LngLat(-3.1860, 55.9440);


        List<LngLat> validMoves = deliveryPathCalculator.getValidMoves(currentPosition);


        assertNotNull(validMoves);
        assertEquals(16, validMoves.size()); // 16 possible moves (22.5-degree increments)
        for (LngLat move : validMoves) {
            double distance = currentPosition.distanceTo(move);
            assertTrue(Math.abs(distance - SystemConstants.DRONE_MOVE_DISTANCE) < 1e-6);
        }
    }

    @Test
    void testIsWithinNoFlyZone() {

        LngLat positionInside = new LngLat(-3.1858, 55.9438);
        LngLat positionOutside = new LngLat(-3.1870, 55.9445);


        boolean insideResult = deliveryPathCalculator.isWithinNoFlyZone(positionInside);
        boolean outsideResult = deliveryPathCalculator.isWithinNoFlyZone(positionOutside);


        assertTrue(insideResult);
        assertFalse(outsideResult);
    }
}
