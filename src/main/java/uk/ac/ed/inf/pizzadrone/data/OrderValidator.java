package uk.ac.ed.inf.pizzadrone.data;

import org.springframework.stereotype.Service;
import uk.ac.ed.inf.pizzadrone.constant.OrderStatus;
import uk.ac.ed.inf.pizzadrone.constant.OrderValidationCode;
import uk.ac.ed.inf.pizzadrone.constant.SystemConstants;
import uk.ac.ed.inf.pizzadrone.interfaces.OrderValidation;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Service
public class OrderValidator implements OrderValidation {

    @Override
    public OrderValidationResult validateOrder(Order orderToValidate, Restaurant[] definedRestaurants) {

        //Check Order Number
        if (orderToValidate.getOrderNo() == null || orderToValidate.getOrderNo().isEmpty()) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.UNDEFINED);
        }


        //Check Creditcard information
        if (!isValidCreditCardNumber(orderToValidate.getCreditCardInformation().getCreditCardNumber())) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.CARD_NUMBER_INVALID);
        }
        if (!isValidExpiryDate(orderToValidate.getCreditCardInformation().getCreditCardExpiry())) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.EXPIRY_DATE_INVALID);
        }
        if (!isValidCVV(orderToValidate.getCreditCardInformation().getCvv())) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.CVV_INVALID);
        }

        //Check pizza number
        if (orderToValidate.getPizzasInOrder().length > SystemConstants.MAX_PIZZAS_PER_ORDER) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED);
        }
        if (orderToValidate.getPizzasInOrder().length == 0) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.EMPTY_ORDER);
        }

        //Check Money
        int calculatedTotal = Arrays.stream(orderToValidate.getPizzasInOrder())
                .mapToInt(Pizza::getPriceInPence)
                .sum() + SystemConstants.ORDER_CHARGE_IN_PENCE;

        if (calculatedTotal != orderToValidate.getPriceTotalInPence()) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.TOTAL_INCORRECT);
        }
        //Check pizza property
        Set<String> restaurantNames = new HashSet<>();
        for (Pizza pizza : orderToValidate.getPizzasInOrder()) {
            Restaurant restaurant = Arrays.stream(definedRestaurants)
                    .filter(r -> r.getMenu().stream()
                            .anyMatch(menuPizza -> pizzaNameMatches(menuPizza.getName(), pizza.getName())))
                    .findFirst()
                    .orElse(null);

            if (restaurant == null) {
                return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.PIZZA_NOT_DEFINED);
            }

            restaurantNames.add(restaurant.name());

            if (!restaurant.isOpenOnDay(orderToValidate.getOrderDate().getDayOfWeek())) {
                return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.RESTAURANT_CLOSED);
            }

            //Check individual price of pizza
            boolean priceMatch = restaurant.getMenu().stream()
                    .anyMatch(menuPizza -> pizzaNameMatches(menuPizza.getName(), pizza.getName()) &&
                            menuPizza.getPriceInPence() == pizza.getPriceInPence());

            if (!priceMatch) {
                return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.PRICE_FOR_PIZZA_INVALID);
            }
        }

        if (restaurantNames.size() > 1) {
            return new OrderValidationResult(OrderStatus.INVALID, OrderValidationCode.PIZZA_FROM_MULTIPLE_RESTAURANTS);
        }



        return new OrderValidationResult(OrderStatus.VALID, OrderValidationCode.NO_ERROR);
}

    private boolean pizzaNameMatches(String menuPizzaName, String orderPizzaName) {

        String cleanedMenuPizzaName = menuPizzaName.replaceAll("^R\\d+:\\s*", "").trim();
        String cleanedOrderPizzaName = orderPizzaName.replaceAll("^R\\d+:\\s*", "").trim();
        return cleanedMenuPizzaName.equalsIgnoreCase(cleanedOrderPizzaName);
    }

    private boolean isValidCreditCardNumber(String creditCardNumber) {
        return creditCardNumber != null && creditCardNumber.matches("\\d{16}");
    }


    private boolean isValidExpiryDate(String expiryDate) {
        return expiryDate != null && expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}");
    }


    private boolean isValidCVV(String cvv) {
        return cvv != null && cvv.matches("\\d{3}");
    }
}
