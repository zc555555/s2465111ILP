package uk.ac.ed.inf.pizzadrone.data;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class LngLat {
    private final double lng;
    private final double lat;
    private static final double CLOSE_DISTANCE = 0.00015;


    @JsonCreator
    public LngLat(@JsonProperty("lng") double lng,
                  @JsonProperty("lat") double lat) {

        this.lng = lng;
        this.lat = lat;
    }
    @JsonProperty("lng")
    public double getLng() { return lng; }
    @JsonProperty("lat")
    public double getLat() { return lat; }

    public double distanceTo(LngLat other) {
        double lngDiff = this.lng - other.lng;
        double latDiff = this.lat - other.lat;
        return Math.sqrt(lngDiff * lngDiff + latDiff * latDiff);
    }

    public boolean isCloseTo(LngLat other) {
        return distanceTo(other) < CLOSE_DISTANCE;
    }

    public LngLat add(LngLat other) {
        return new LngLat(this.lng + other.lng, this.lat + other.lat);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LngLat)) return false;
        LngLat lngLat = (LngLat) o;
        return Double.compare(lngLat.lng, lng) == 0 &&
                Double.compare(lngLat.lat, lat) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lng, lat);
    }

}
