package uk.ac.ed.inf.pizzadrone.data;


public class Pizza {
    private String name;
    private int priceInPence;

    public Pizza(String name, int priceInPence) {
        this.name = name;
        this.priceInPence = priceInPence;
    }

    public String getName() {
        return name;
    }

    public int getPriceInPence() {
        return priceInPence;
    }

}