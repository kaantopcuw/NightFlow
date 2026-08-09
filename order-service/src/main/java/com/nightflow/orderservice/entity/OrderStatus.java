package com.nightflow.orderservice.entity;

public enum OrderStatus {
    /** Created, not paid yet. */
    PENDING,
    /** Paid and every reservation was turned into a sold ticket. */
    COMPLETED,
    /** Abandoned before payment. */
    CANCELLED,
    /** Was COMPLETED, money given back afterwards. */
    REFUNDED,
    /**
     * The payment saga failed and was fully compensated: no ticket of this
     * order is sold, the stock is back and the (simulated) payment was
     * reversed. Terminal - the buyer has to start a new order.
     */
    FAILED,
    /**
     * The payment saga failed and at least one compensating call failed too,
     * so ticket-service may still be holding stock for this order. Terminal
     * for the buyer, but needs reconciliation: ticket-service's expired
     * reservation sweeper releases untouched reservations after 15 minutes,
     * anything already confirmed has to be released by hand.
     */
    COMPENSATION_FAILED
}
