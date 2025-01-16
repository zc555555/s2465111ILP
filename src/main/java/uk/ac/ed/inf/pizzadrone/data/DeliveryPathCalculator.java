package uk.ac.ed.inf.pizzadrone.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.ArrayList;
import java.util.List;

@Component
public class DeliveryPathCalculator{

    private NamedRegion centralArea;
    private List<NamedRegion> noFlyZones;
    private final LngLat APPLETON_TOWER = new LngLat(-3.186874, 55.944494);
    private final double MOVE_DISTANCE = 0.00015;


    @Autowired
    public DeliveryPathCalculator(DataService dataService) {
        this.centralArea = dataService.fetchCentralArea();
        this.noFlyZones = dataService.fetchNoFlyZones();
    }
    public List<LngLat> calculateDeliveryPath(LngLat startPosition) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(node -> node.fCost));
        Map<LngLat, Node> allNodes = new HashMap<>();
        Set<LngLat> closedSet = new HashSet<>();

        Node startNode = new Node(startPosition, null, 0, heuristic(startPosition, APPLETON_TOWER));
        openSet.add(startNode);
        allNodes.put(startPosition, startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            /*if (current.position.isCloseTo(restaurantPosition)) {
            List<LngLat> path = reconstructPath(current);
            path.add(restaurantPosition); // Hovering at restaurant
        }*/

            /*if (current.position.isCloseTo(APPLETON_TOWER)) {
                List<LngLat> path = reconstructPath(current);
                path.add(APPLETON_TOWER); // Hovering at appleton using appleton tower constant
                return path;
            }*/


            if (current.position.isCloseTo(APPLETON_TOWER)) {
                List<LngLat> path = reconstructPath(current);
                //LngLat lastPosition = path.get(path.size() - 1);
                //path.add(lastPosition);//Hovering at appleton using last position
                return path;
            }

            closedSet.add(current.position);

            for (LngLat nextPosition : getValidMoves(current.position)) {
                if (closedSet.contains(nextPosition) || isWithinNoFlyZone(nextPosition)) continue;

                if (!centralArea.containsPoint(nextPosition) && centralArea.containsPoint(current.position)) {
                    continue;
                }

                double tentativeGCost = current.gCost + current.position.distanceTo(nextPosition);
                Node nextNode = allNodes.getOrDefault(nextPosition, new Node(nextPosition, null, Double.MAX_VALUE, 0));

                if (tentativeGCost < nextNode.gCost) {
                    nextNode.gCost = tentativeGCost;
                    nextNode.hCost = heuristic(nextPosition, APPLETON_TOWER);
                    nextNode.fCost = nextNode.gCost + nextNode.hCost;
                    nextNode.parent = current;
                    openSet.add(nextNode);
                    allNodes.put(nextPosition, nextNode);
                }
            }
        }
        throw new RuntimeException("No effective trail");
    }

    public double heuristic(LngLat current, LngLat target) {
        return Math.abs(current.getLng() - target.getLng()) + Math.abs(current.getLat() - target.getLat());
    }

    public List<LngLat> getValidMoves(LngLat current) {
        double[] angles = {0, 22.5, 45, 67.5, 90, 112.5, 135, 157.5,
                180, 202.5, 225, 247.5, 270, 292.5, 315, 337.5};
        List<LngLat> moves = new ArrayList<>();
        for (double angle : angles) {
            double radian = Math.toRadians(angle);
            moves.add(new LngLat(
                    current.getLng() + MOVE_DISTANCE * Math.cos(radian),
                    current.getLat() + MOVE_DISTANCE * Math.sin(radian)
            ));
        }
        return moves;
    }

    public boolean isWithinNoFlyZone(LngLat position) {
        return noFlyZones.parallelStream().anyMatch(zone -> zone.containsPoint(position));
    }


    private List<LngLat> reconstructPath(Node node) {
        List<LngLat> path = new ArrayList<>();
        while (node != null) {
            path.add(node.position);
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    public String convertToGeoJson(List<LngLat> path, String orderNo) {
        StringBuilder geoJson = new StringBuilder();
        geoJson.append("{\"type\":\"FeatureCollection\",\"features\":[");
        geoJson.append("{\"type\":\"Feature\",\"geometry\":{\"type\":\"LineString\",\"coordinates\":[");

        for (int i = 0; i < path.size(); i++) {
            LngLat point = path.get(i);
            geoJson.append("[").append(point.getLng()).append(",").append(point.getLat()).append("]");
            if (i < path.size() - 1) geoJson.append(",");
        }

        geoJson.append("]},\"properties\":{\"orderNo\":\"").append(orderNo).append("\"}}]}");
        return geoJson.toString();
    }

    public PositionForTest calculateNextPosition(PositionForTest start, double angle) {
        double radian = Math.toRadians(angle);
        double newLng = start.getLng() + MOVE_DISTANCE * Math.cos(radian);
        double newLat = start.getLat() + MOVE_DISTANCE * Math.sin(radian);
        return new PositionForTest(newLat, newLng);
    }




    private static class Node {
        LngLat position;
        Node parent;
        double gCost;
        double hCost;
        double fCost;

        public Node(LngLat position, Node parent, double gCost, double hCost) {
            this.position = position;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }
    }
}


