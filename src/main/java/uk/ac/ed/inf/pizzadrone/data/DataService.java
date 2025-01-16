package uk.ac.ed.inf.pizzadrone.data;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.inf.pizzadrone.constant.OrderStatus;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DataService {

    private final String noFlyZonesUrl = "https://ilp-rest-2024.azurewebsites.net/noFlyZones";
    private final String restaurantUrl = "https://ilp-rest-2024.azurewebsites.net/restaurants";
    private final RestTemplate restTemplate = new RestTemplate();


    public List<NamedRegion> fetchNoFlyZones() {
        try {
            return Arrays.asList(restTemplate.getForObject(noFlyZonesUrl, NamedRegion[].class));
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public NamedRegion fetchCentralArea() {
        String url = "https://ilp-rest-2024.azurewebsites.net/centralArea";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode jsonNode = mapper.readTree(response.getBody());
            JsonNode verticesNode = jsonNode.get("vertices");
            LngLat[] centralAreaPoints = mapper.treeToValue(verticesNode, LngLat[].class);
            return new NamedRegion(jsonNode.get("name").asText(), List.of(centralAreaPoints));
        } catch (Exception e) {
            throw new RuntimeException("Error parsing central area data: " + e.getMessage());
        }
    }


    public List<Restaurant> fetchRestaurants() {
        return Arrays.asList(restTemplate.getForObject(restaurantUrl, Restaurant[].class));
    }

    public Restaurant findRestaurantByPizza(String pizzaName) {
        return fetchRestaurants().stream()
                .filter(r -> r.getMenu().stream()
                        .anyMatch(p -> p.getName().equalsIgnoreCase(pizzaName)))
                .findFirst().orElse(null);
    }

    public List<LngLat> removeHoverSteps(List<LngLat> path) {
        if (path == null || path.isEmpty()) {
            return new ArrayList<>();
        }

        List<LngLat> filteredPath = new ArrayList<>();
        LngLat previousPoint = null;

        for (LngLat point : path) {
            // if current position is the same as the last one then jump over
            if (previousPoint != null && previousPoint.equals(point)) {
                continue;
            }
            filteredPath.add(point);
            previousPoint = point;
        }
        return filteredPath;
    }

    public boolean validateOrder(Order order) {

        return order.getOrderStatus() == OrderStatus.VALID;
    }


}
