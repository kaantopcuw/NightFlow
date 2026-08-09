package com.nightflow.orderservice.exception;

import com.nightflow.orderservice.entity.Order;
import lombok.Getter;

/**
 * Thrown when payOrder could not confirm the sale and had to compensate.
 *
 * It carries the order as it was persisted by the compensation, so the caller
 * is told the resulting status (FAILED or COMPENSATION_FAILED) instead of a
 * bare 500. The order state is already committed by the time this is thrown -
 * the compensation writes it in its own transaction.
 */
@Getter
public class OrderSagaFailedException extends RuntimeException {

    private final transient Order order;

    public OrderSagaFailedException(Order order) {
        super("Payment saga failed for order " + order.getOrderNumber()
                + " and was compensated (status " + order.getStatus() + ")");
        this.order = order;
    }
}
