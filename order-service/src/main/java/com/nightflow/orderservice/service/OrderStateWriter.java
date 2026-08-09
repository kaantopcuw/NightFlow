package com.nightflow.orderservice.service;

import com.nightflow.orderservice.entity.Order;
import com.nightflow.orderservice.entity.OrderStatus;
import com.nightflow.orderservice.exception.OrderNotFoundException;
import com.nightflow.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The transactional half of the payment saga.
 *
 * payOrder cannot be one big @Transactional method: it calls ticket-service
 * over HTTP, and when a call fails it has to *record* that failure. A single
 * surrounding transaction would either stay open across the remote calls or -
 * worse - roll the FAILED status back together with the exception that reports
 * it, leaving the order PENDING and the failure invisible.
 *
 * So each state change is its own short transaction, in its own bean (a
 * self-invoked @Transactional method is not proxied and would silently do
 * nothing). REQUIRES_NEW makes the compensation record survive even if a
 * caller ever wraps payOrder in a transaction of its own.
 */
@Component
@RequiredArgsConstructor
public class OrderStateWriter {

    /** `failureReason` column width. */
    private static final int REASON_LIMIT = 1024;

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public Order loadWithItems(String orderNumber) {
        return orderRepository.findByOrderNumberWithItems(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order markCompleted(Long orderId, String paymentReference) {
        Order order = require(orderId);
        order.setStatus(OrderStatus.COMPLETED);
        order.setPaymentReference(paymentReference);
        order.setFailureReason(null);
        return orderRepository.save(order);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Order markFailed(Long orderId, OrderStatus status, String paymentReference, String failureReason) {
        Order order = require(orderId);
        order.setStatus(status);
        order.setPaymentReference(paymentReference);
        order.setFailureReason(truncate(failureReason));
        return orderRepository.save(order);
    }

    private Order require(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("id=" + orderId));
    }

    private static String truncate(String reason) {
        if (reason == null || reason.length() <= REASON_LIMIT) {
            return reason;
        }
        return reason.substring(0, REASON_LIMIT - 3) + "...";
    }
}
