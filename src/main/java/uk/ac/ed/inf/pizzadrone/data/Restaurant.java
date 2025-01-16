package uk.ac.ed.inf.pizzadrone.data;


import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;


public record Restaurant(String name, LngLat location, DayOfWeek[] openingDays, Pizza[] menu) {
    public boolean isOpenOnDay(DayOfWeek day) {
        return Arrays.asList(openingDays).contains(day);
    }

    public List<Pizza> getMenu() {
        return Arrays.asList(menu);
    }

    public LngLat getLocation() {
        return location;
    }

}


