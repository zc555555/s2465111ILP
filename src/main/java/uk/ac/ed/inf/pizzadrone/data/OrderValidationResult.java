package uk.ac.ed.inf.pizzadrone.data;

import uk.ac.ed.inf.pizzadrone.constant.OrderStatus;
import uk.ac.ed.inf.pizzadrone.constant.OrderValidationCode;

public class OrderValidationResult {
    private final OrderStatus orderStatus;
    private final OrderValidationCode validationCode;

    public OrderValidationResult(OrderStatus orderStatus, OrderValidationCode validationCode) {
        this.orderStatus = orderStatus;
        this.validationCode = validationCode;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }


    public OrderValidationCode getOrderValidationCode() {
        return this.validationCode;
    }


}
