package uk.ac.ed.inf.pizzadrone.unitTest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ed.inf.pizzadrone.constant.*;
import uk.ac.ed.inf.pizzadrone.data.*;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OrderValidatorTest {

    private OrderValidator orderValidator;
    private Restaurant[] mockRestaurants;

    @BeforeEach
    void setUp() {
        orderValidator = new OrderValidator();

        mockRestaurants = new Restaurant[]{
                new Restaurant(
                        "PizzaHut",
                        new LngLat(-3.186874, 55.944494),
                        new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, LocalDate.now().getDayOfWeek()},
                        new Pizza[]{
                                new Pizza("Margarita", 900),
                                new Pizza("Pepperoni", 800)
                        }
                )
        };
    }

    @Test
    void testValidateOrder_ValidOrder() {
        Order validOrder = new Order(
                "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                900 + SystemConstants.ORDER_CHARGE_IN_PENCE,
                new Pizza[]{new Pizza("Margarita", 900)},
                new CreditCardInformation("1234567812345678", "12/25", "123")
        );

        OrderValidationResult result = orderValidator.validateOrder(validOrder, mockRestaurants);

        assertEquals(OrderStatus.VALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.NO_ERROR, result.getOrderValidationCode());
    }

    @Test
    void testValidateOrder_InvalidOrderNumber() {
        Order invalidOrder = new Order(
                "",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                900 + SystemConstants.ORDER_CHARGE_IN_PENCE,
                new Pizza[]{new Pizza("Margarita", 900)},
                new CreditCardInformation("1234567812345678", "12/25", "123")
        );

        OrderValidationResult result = orderValidator.validateOrder(invalidOrder, mockRestaurants);

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.UNDEFINED, result.getOrderValidationCode());
    }

    @Test
    void testValidateOrder_InvalidCreditCardNumber() {
        Order invalidOrder = new Order(
                "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                900 + SystemConstants.ORDER_CHARGE_IN_PENCE,
                new Pizza[]{new Pizza("Margarita", 900)},
                new CreditCardInformation("1234", "12/25", "123")
        );

        OrderValidationResult result = orderValidator.validateOrder(invalidOrder, mockRestaurants);

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.CARD_NUMBER_INVALID, result.getOrderValidationCode());
    }

    @Test
    void testValidateOrder_InvalidExpiryDate() {
        Order invalidOrder = new Order(
                "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                900 + SystemConstants.ORDER_CHARGE_IN_PENCE,
                new Pizza[]{new Pizza("Margarita", 900)},
                new CreditCardInformation("1234567812345678", "13/25", "123")
        );

        OrderValidationResult result = orderValidator.validateOrder(invalidOrder, mockRestaurants);

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.EXPIRY_DATE_INVALID, result.getOrderValidationCode());
    }

    @Test
    void testValidateOrder_InvalidCVV() {
        Order invalidOrder = new Order(
                "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                900 + SystemConstants.ORDER_CHARGE_IN_PENCE,
                new Pizza[]{new Pizza("Margarita", 900)},
                new CreditCardInformation("1234567812345678", "12/25", "12")
        );

        OrderValidationResult result = orderValidator.validateOrder(invalidOrder, mockRestaurants);

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.CVV_INVALID, result.getOrderValidationCode());
    }

    @Test
    void testValidateOrder_TooManyPizzas() {
        Order invalidOrder = new Order(
                "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                900 * 5 + SystemConstants.ORDER_CHARGE_IN_PENCE,
                new Pizza[]{
                        new Pizza("Margarita", 900),
                        new Pizza("Pepperoni", 900),
                        new Pizza("Margarita", 900),
                        new Pizza("Pepperoni", 900),
                        new Pizza("Veggie", 900)
                },
                new CreditCardInformation("1234567812345678", "12/25", "123")
        );

        OrderValidationResult result = orderValidator.validateOrder(invalidOrder, mockRestaurants);

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.MAX_PIZZA_COUNT_EXCEEDED, result.getOrderValidationCode());
    }

    @Test
    void testValidateOrder_InvalidPizzaPrice() {
        Order invalidOrder = new Order(
                "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                950 + SystemConstants.ORDER_CHARGE_IN_PENCE,
                new Pizza[]{new Pizza("Margarita", 950)},
                new CreditCardInformation("1234567812345678", "12/25", "123")
        );


        OrderValidationResult result = orderValidator.validateOrder(invalidOrder, mockRestaurants);

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.PRICE_FOR_PIZZA_INVALID, result.getOrderValidationCode());
    }

    @Test
    void testValidateOrder_EmptyOrder() {
        Order invalidOrder = new Order(
                "12345",
                LocalDate.now(),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                SystemConstants.ORDER_CHARGE_IN_PENCE,
                new Pizza[]{},
                new CreditCardInformation("1234567812345678", "12/25", "123")
        );

        OrderValidationResult result = orderValidator.validateOrder(invalidOrder, mockRestaurants);

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.EMPTY_ORDER, result.getOrderValidationCode());
    }

    @Test
    void testValidateOrder_RestaurantClosed() {
        Order invalidOrder = new Order(
                "12345",
                LocalDate.now().with(DayOfWeek.SUNDAY),
                OrderStatus.UNDEFINED,
                OrderValidationCode.UNDEFINED,
                900 + SystemConstants.ORDER_CHARGE_IN_PENCE,
                new Pizza[]{new Pizza("Margarita", 900)},
                new CreditCardInformation("1234567812345678", "12/25", "123")
        );

        OrderValidationResult result = orderValidator.validateOrder(invalidOrder, mockRestaurants);

        assertEquals(OrderStatus.INVALID, result.getOrderStatus());
        assertEquals(OrderValidationCode.RESTAURANT_CLOSED, result.getOrderValidationCode());
    }
}

