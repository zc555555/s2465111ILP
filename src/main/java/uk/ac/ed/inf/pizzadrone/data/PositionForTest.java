package uk.ac.ed.inf.pizzadrone.data;

public class PositionForTest {
    private Double lng;
    private Double lat;

    public PositionForTest(double lng, double lat) {
        this.lng = lng;
        this.lat = lat;
    }


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
    public boolean isValid() {
        if (lat == null || lng == null) {
            return false; // If either latitude or longitude is null, the position is invalid
        }

        return (lat >= -90 && lat <= 90 );
        // Valid latitude is between -90 and 90
    }
}


