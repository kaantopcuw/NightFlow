package com.nightflow.orderservice.service;

import com.nightflow.orderservice.client.TicketServiceClient;
import com.nightflow.orderservice.dto.OrderRequest;
import com.nightflow.orderservice.entity.Order;
import com.nightflow.orderservice.entity.OrderItem;
import com.nightflow.orderservice.entity.OrderStatus;
import com.nightflow.orderservice.event.OrderCreatedEvent;
import com.nightflow.orderservice.exception.OrderNotFoundException;
import com.nightflow.orderservice.exception.OrderSagaFailedException;
import com.nightflow.orderservice.producer.OrderProducer;
import com.nightflow.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    /** Fallback ticket-service user id for orders placed without a numeric user id. */
    private static final long GUEST_USER_ID = 999L;

    private final OrderRepository orderRepository;
    private final TicketServiceClient ticketServiceClient;
    private final OrderProducer orderProducer;
    private final OrderStateWriter orderStateWriter;

    @Transactional
    public Order createOrder(OrderRequest request, String userId) {
        log.info("Creating order for user: {}", userId);

        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .userId(userId)
                .totalAmount(request.totalAmount())
                .build();

        List<OrderItem> items = request.items().stream()
                .map(itemRequest -> OrderItem.builder()
                        .order(order)
                        .categoryId(itemRequest.categoryId())
                        .categoryName(itemRequest.categoryName())
                        .eventId(itemRequest.eventId())
                        .eventName(itemRequest.eventName())
                        .quantity(itemRequest.quantity())
                        .price(itemRequest.price())
                        .reservationId(itemRequest.reservationId())
                        .build())
                .collect(Collectors.toList());

        order.setItems(items);
        return orderRepository.save(order);
    }

    public List<Order> getMyOrders(String userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * Pays an order as a saga: take the money, then turn every reservation into
     * a sold ticket. Both halves live in different services, so there is no
     * transaction that spans them - if the second half fails, the first half is
     * undone by compensating calls rather than by a rollback.
     *
     * Deliberately *not* @Transactional: the remote calls must not run inside a
     * database transaction, and the compensation has to commit the failure it
     * is about to report. State changes go through {@link OrderStateWriter}.
     *
     * @throws OrderSagaFailedException when the sale could not be confirmed.
     *         The order is left in FAILED (everything undone) or
     *         COMPENSATION_FAILED (undo itself failed) - never COMPLETED.
     */
    public Order payOrder(String orderNumber) {
        log.info("Processing payment for order: {}", orderNumber);

        Order order = orderStateWriter.loadWithItems(orderNumber);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException(
                    "Order " + orderNumber + " is not payable, current status: " + order.getStatus());
        }

        long ticketUserId = resolveTicketUserId(order);

        // Step 1 - take the money.
        String paymentReference = capturePayment(order);

        // Step 2 - confirm the sale of every reserved item. The first failure
        // aborts the loop; items confirmed before it are undone by compensate().
        try {
            for (OrderItem item : order.getItems()) {
                ticketServiceClient.confirmSale(item.getReservationId(), order.getId(), ticketUserId);
            }
        } catch (Exception failure) {
            throw compensate(order, paymentReference, failure);
        }

        // Step 3 - only now is the order complete.
        Order completed = orderStateWriter.markCompleted(order.getId(), paymentReference);

        // Step 4 - tell the rest of the system. Published after the sale is
        // confirmed, so a failed order never triggers a confirmation e-mail.
        // Not part of the saga: the money is taken and the tickets are sold, so
        // a broker problem must not be reported to the buyer as a failed
        // payment. It costs a confirmation e-mail, not a ticket.
        try {
            orderProducer.sendOrderCreatedEvent(toEvent(order));
        } catch (Exception e) {
            log.error("Order {} is COMPLETED but the order-created event could not be published",
                    order.getOrderNumber(), e);
        }

        return completed;
    }

    public Order getOrder(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException(orderNumber));
    }

    /**
     * Undoes everything step 1 and 2 managed to do, then records the outcome.
     *
     * Two compensating calls, because there are two things to undo and they are
     * different: tickets this order already had confirmed are SOLD and are
     * released by order id, while reservations that were never confirmed are
     * still held under their reservation id. cancelReservation is a no-op for
     * an item that was confirmed (confirm-sale clears the session id), so both
     * can simply be attempted for the whole order.
     *
     * Both are attempted unconditionally rather than only for the items the
     * loop got through. Which items are confirmed is not something this side
     * knows for certain - a confirm-sale whose response was lost committed in
     * ticket-service but looks like a failure here - and asking to release
     * nothing is answered with 0.
     */
    private OrderSagaFailedException compensate(Order order, String paymentReference, Exception cause) {
        log.error("confirm-sale failed for order {} - starting compensation", order.getOrderNumber(), cause);

        List<String> compensationErrors = new ArrayList<>();

        try {
            Integer released = ticketServiceClient.releaseSale(order.getId());
            log.warn("Compensation: released {} already-sold ticket(s) for order {}",
                    released, order.getOrderNumber());
        } catch (Exception e) {
            compensationErrors.add("release-sale failed: " + describe(e));
        }

        for (OrderItem item : order.getItems()) {
            try {
                ticketServiceClient.cancelReservation(item.getReservationId());
            } catch (Exception e) {
                compensationErrors.add("cancel-reservation " + item.getReservationId() + " failed: " + describe(e));
            }
        }

        refundPayment(order, paymentReference);

        OrderStatus status = compensationErrors.isEmpty()
                ? OrderStatus.FAILED
                : OrderStatus.COMPENSATION_FAILED;

        StringBuilder reason = new StringBuilder("confirm-sale failed: ").append(describe(cause));
        if (compensationErrors.isEmpty()) {
            reason.append(" | compensated: tickets released, payment ")
                    .append(paymentReference)
                    .append(" refunded (simulated)");
        } else {
            reason.append(" | payment ").append(paymentReference)
                    .append(" refunded (simulated), but compensation left ticket-service inconsistent: ")
                    .append(String.join("; ", compensationErrors));
        }

        Order failed = orderStateWriter.markFailed(order.getId(), status, paymentReference, reason.toString());
        log.error("Order {} ended as {} - {}", failed.getOrderNumber(), failed.getStatus(), failed.getFailureReason());

        return new OrderSagaFailedException(failed);
    }

    /**
     * Payment capture is still a stand-in - NightFlow has no PSP integration.
     * It exists as its own step so the saga has the shape it would have with a
     * real gateway, and so the compensating refund has a reference to quote;
     * no money moves in either direction.
     */
    private String capturePayment(Order order) {
        String reference = "SIM-" + order.getOrderNumber();
        log.info("Payment captured (simulated) for order {}: amount={} reference={}",
                order.getOrderNumber(), order.getTotalAmount(), reference);
        return reference;
    }

    /** Compensation for {@link #capturePayment} - equally simulated. */
    private void refundPayment(Order order, String paymentReference) {
        log.warn("Refunding payment (simulated) for order {}: amount={} reference={}",
                order.getOrderNumber(), order.getTotalAmount(), paymentReference);
    }

    private long resolveTicketUserId(Order order) {
        try {
            return Long.parseLong(order.getUserId());
        } catch (NumberFormatException e) {
            return GUEST_USER_ID; // Anonymous / guest checkout
        }
    }

    private OrderCreatedEvent toEvent(Order order) {
        List<OrderCreatedEvent.OrderItemEvent> itemEvents = order.getItems().stream()
                .map(item -> new OrderCreatedEvent.OrderItemEvent(
                        item.getCategoryId(),
                        item.getCategoryName(),
                        item.getEventId(),
                        item.getEventName(),
                        item.getQuantity(),
                        item.getPrice()
                )).toList();

        return new OrderCreatedEvent(
                order.getOrderNumber(),
                order.getUserId(),
                order.getTotalAmount(),
                LocalDateTime.now(),
                itemEvents
        );
    }

    private static String describe(Exception e) {
        String message = e.getMessage();
        return e.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }
}
