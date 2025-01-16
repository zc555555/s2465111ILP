package uk.ac.ed.inf.pizzadrone.controller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.ac.ed.inf.pizzadrone.constant.OrderStatus;
import uk.ac.ed.inf.pizzadrone.data.*;
import uk.ac.ed.inf.pizzadrone.interfaces.OrderValidation;


import java.util.List;



@RestController
//@RequestMapping("/api")
public class RestControllerClass {
    private final OrderValidation orderValidator;
    private final DataService dataService;
    private final DeliveryPathCalculator deliveryPathCalculator;

    @Autowired
    public RestControllerClass(OrderValidation orderValidator, DataService dataService,DeliveryPathCalculator deliveryPathCalculator) {
        this.orderValidator = orderValidator;
        this.dataService = dataService;
        this.deliveryPathCalculator = deliveryPathCalculator;
    }

    @GetMapping("/uuid")
    public String  getUuid(){
        return "s2465111";
    }

    @PostMapping("/distanceTo")
    public ResponseEntity<Double> calculateDistance(@RequestBody LngLatPairRequest request){
        Position position1 = request.getPosition1();
        Position position2 = request.getPosition2();
        if (position1 == null || position2 == null) {
            return ResponseEntity.badRequest().build();  // Return 400 Bad Request
        }

        if (!position1.isValid() || !position2.isValid()) {
            return ResponseEntity.badRequest().build();  // Lng and Lat is over range
        }
        double lngDiff = position1.getLng() - position2.getLng();
        double latDiff = position1.getLat() - position2.getLat();
        double distance = Math.sqrt(Math.pow(lngDiff, 2) + Math.pow(latDiff, 2));//Euclidean distance
        return ResponseEntity.ok(distance); // Return 200 ok and result
    }

    @PostMapping("/isCloseTo")
    public ResponseEntity<Boolean> isCloseTo(@RequestBody LngLatPairRequest request) {
        Position position1 = request.getPosition1();
        Position position2 = request.getPosition2();
        if (position1 == null || position2 == null) {
            return ResponseEntity.badRequest().build();
        }

        if (!position1.isValid() || !position2.isValid()) {
            return ResponseEntity.badRequest().build();
        }
        double lngDiff = position1.getLng() - position2.getLng();
        double latDiff = position1.getLat() - position2.getLat();
        double distance = Math.sqrt(Math.pow(lngDiff, 2) + Math.pow(latDiff, 2));
        return ResponseEntity.ok(distance < 0.00015);// If the Euclidean distance is less than 0.00015
    }
    @PostMapping("/nextPosition")
    public ResponseEntity<Position> nextPosition(@RequestBody NextPositionRequest request) {
        Position start = request.getStart();
        double angle = request.getAngle();

        if (start == null) {
            return ResponseEntity.badRequest().build();  //Return 400 Bad Request
        }

        if (!start.isValid() || angle < 0 || angle > 360) {
            return ResponseEntity.badRequest().build();  // Start has invalid lng and lat
            // Angle is only in the range between 0 and 360, if not then return 400 Bad Request
        }


        double angleInRadians = Math.toRadians(angle);
        double distance = 0.00015;

        double deltaLng = distance * Math.cos(angleInRadians); //Convert distance to lng and lat respectively
        double deltaLat = distance * Math.sin(angleInRadians);

        Position newPosition = new Position();
        newPosition.setLng(start.getLng() + deltaLng);
        newPosition.setLat(start.getLat() + deltaLat);


        return ResponseEntity.ok(newPosition);
    }
    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@RequestBody IsInRegionRequest request) {
        Position point = request.getPosition();
        Region region = request.getRegion();
        if (point == null || !point.isValid()) {
            return ResponseEntity.badRequest().build();  // Return 400 Bad Request
        }
        if (region == null || !region.isRegionValid()) {
            return ResponseEntity.badRequest().build();  // If region is invalid or null then return 400
        }
        List<Position> vertices = region.getVertices();

        boolean isInside = isPointInPolygon(point, vertices);
        return ResponseEntity.ok(isInside);

    }
    @PostMapping("/validateOrder")
    public ResponseEntity<OrderValidationResult> validateOrder(@RequestBody Order order) {
        List<Restaurant> restaurants = dataService.fetchRestaurants();
        OrderValidationResult validationResult = orderValidator.validateOrder(order, restaurants.toArray(new Restaurant[0]));
        if (validationResult.getOrderStatus() == OrderStatus.INVALID) {
            return ResponseEntity.badRequest().body(validationResult);
        }
        return ResponseEntity.ok(validationResult);
    }

    @PostMapping("/calcDeliveryPath")
    public ResponseEntity<List<LngLat>> calcDeliveryPath(@RequestBody Order order) {
        OrderValidationResult validationResult = orderValidator.validateOrder(order, dataService.fetchRestaurants().toArray(new Restaurant[0]));
        if (!validationResult.getOrderStatus().equals(OrderStatus.VALID)) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            Restaurant restaurant = dataService.findRestaurantByPizza(order.getPizzasInOrder()[0].getName());
            if (restaurant == null) {
                return ResponseEntity.badRequest().body(null);
            }

            List<LngLat> deliveryPath = deliveryPathCalculator.calculateDeliveryPath(restaurant.getLocation());
            return ResponseEntity.ok(deliveryPath);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/calcDeliveryPathAsGeoJson")
    public ResponseEntity<String> calculateDeliveryPathAsGeoJson(@RequestBody Order order) {
        OrderValidationResult validationResult = orderValidator.validateOrder(order, dataService.fetchRestaurants().toArray(new Restaurant[0]));
        if (!validationResult.getOrderStatus().equals(OrderStatus.VALID)) {
            return ResponseEntity.badRequest().body(null);
        }
        Restaurant restaurant = dataService.findRestaurantByPizza(order.getPizzasInOrder()[0].getName());
        if (restaurant == null) {
            return ResponseEntity.badRequest().body(null);
        }
        List<LngLat> path = deliveryPathCalculator.calculateDeliveryPath(restaurant.getLocation());
        //path = dataService.removeHoverSteps(path);
        String geoJson = deliveryPathCalculator.convertToGeoJson(path, order.getOrderNo());
        return ResponseEntity.ok(geoJson);
    }



    private boolean isPointInPolygon(Position point, List<Position> vertices) {
        // Determine if the point is on the board and in the region, or outside
        boolean isInside = false;
        int n = vertices.size();
        int j = n - 1;

        for (int i = 0; i < n; i++) {
            Position vertex1 = vertices.get(i);
            Position vertex2 = vertices.get(j);
            if (isPointOnLineSegment(point, vertex1, vertex2)) {
                return true;
            }
            
            if ((vertex1.getLat() > point.getLat()) != (vertex2.getLat() > point.getLat()) &&
                    (point.getLng() < (vertex2.getLng() - vertex1.getLng()) * (point.getLat() - vertex1.getLat()) / (vertex2.getLat() - vertex1.getLat()) + vertex1.getLng())) {
                isInside = !isInside;
            }
            j = i;
            // Ray-casting algorithm
        }

        return isInside;
    }

    private boolean isPointOnLineSegment(Position point, Position vertex1, Position vertex2) {
        // Determine if the point is on board
        double px = point.getLng();
        double py = point.getLat();
        double x1 = vertex1.getLng();
        double y1 = vertex1.getLat();
        double x2 = vertex2.getLng();
        double y2 = vertex2.getLat();

        if (px < Math.min(x1, x2) || px > Math.max(x1, x2) || py < Math.min(y1, y2) || py > Math.max(y1, y2)) {
            return false; // Firstly determine if it is in the rectangle made of two diagonal points(vertices)
        }

        double crossProduct = (py - y1) * (x2 - x1) - (px - x1) * (y2 - y1);// Similarity of two triangles
        return !(Math.abs(crossProduct) > 1e-10);  // Not on the line

    }


}
class Position {
    private Double lng;
    private Double lat;


    public double getLng() {
        return normalizeLongitude(lng);
    }
    public void setLng(double lng) {
        this.lng = normalizeLongitude(lng);
    }
    public double getLat() {
        return lat;
    }
    public void setLat(double lat) {
        this.lat = lat;
    }

    @JsonIgnore
    public boolean isValid() {
        if (lat == null || lng == null) {
            return false; // If either latitude or longitude is null, the position is invalid
        }

        return (lat >= -90 && lat <= 90 );
        // Valid latitude is between -90 and 90
    }

    private double normalizeLongitude(double longitude) {
        // for lng that over the range -180 to 180, need to be rounded back in the range
        longitude = longitude % 360;
        if (longitude > 180) {
            longitude -= 360;
        } else if (longitude < -180) {
            longitude += 360;
        }
        return longitude;
    }

}

class LngLatPairRequest {
    private Position position1;
    private Position position2;

    // Getters and setters
    public Position getPosition1() {
        return position1;
    }

    public Position getPosition2() {
        return position2;
    }

}

class NextPositionRequest {
    private Position start;
    private double angle;

    // Getters and setters
    public Position getStart() {
        return start;
    }


    public double getAngle() {
        return angle;
    }

}


class IsInRegionRequest {
    private Position position;
    private Region region;

    public Position getPosition() {
        return position;
    }

    public Region getRegion() {
        return region;
    }
}

class Region {
    private String name;           // The name of the region
    private List<Position> vertices;  // The list of vertices of region

    public String getName() {
        return name;
    }

    public List<Position> getVertices() {
        return vertices;
    }

    public boolean isRegionValid(){

        for (Position vertex : vertices) {
            if (vertex == null || !vertex.isValid()) {
                return false;
            }
        }
        // Check if the region has at least three vertices
        if (vertices == null || vertices.size() < 3) {
            return false;
        }
        // Check if all vertices are collinear
        else return !allVerticesCollinear();

    }

    private boolean allVerticesCollinear() {
        int n = vertices.size();

        // Use the first two points to determine the base direction
        Position p1 = vertices.get(0);
        Position p2 = vertices.get(1);

        double dx = p2.getLng() - p1.getLng();
        double dy = p2.getLat() - p1.getLat();

        // Check if all other points are on the same line
        for (int i = 2; i < n; i++) {
            Position pi = vertices.get(i);
            double currentDx = pi.getLng() - p1.getLng();
            double currentDy = pi.getLat() - p1.getLat();

            // If the cross product is not zero, the points are not collinear
            if ((dx * currentDy) != (dy * currentDx)) {
                return false;
            }
        }

        return true;
    }
}

