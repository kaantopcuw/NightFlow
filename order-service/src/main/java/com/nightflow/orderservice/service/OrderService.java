package com.nightflow.orderservice.service;

import com.nightflow.orderservice.client.TicketServiceClient;
import com.nightflow.orderservice.dto.OrderRequest;
import com.nightflow.orderservice.entity.Order;
import com.nightflow.orderservice.entity.OrderItem;
import com.nightflow.orderservice.entity.OrderStatus;
import com.nightflow.orderservice.event.OrderCreatedEvent;
import com.nightflow.orderservice.producer.OrderProducer;
import com.nightflow.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final TicketServiceClient ticketServiceClient;
    private final OrderProducer orderProducer;

    @Transactional
    public Order createOrder(OrderRequest request) {
        log.info("Creating order for user: {}", request.userId());

        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .userId(request.userId())
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

    @Transactional
    public Order payOrder(String orderNumber) {
        log.info("Processing payment for order: {}", orderNumber);
        
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order is not pending");
        }

        // 1. Ödeme işlemi (Simüle)
        // paymentService.processPayment(...)
        
        // 2. Ticket Service'de satışı onayla
        Long userIdLong;
        try {
            userIdLong = Long.parseLong(order.getUserId());
        } catch (NumberFormatException e) {
            userIdLong = 999L; // Anonymous / Guest user backup ID
        }

        for (OrderItem item : order.getItems()) {
            try {
                ticketServiceClient.confirmSale(
                        item.getReservationId(),
                        item.getCategoryId(),
                        userIdLong
                );
            } catch (Exception e) {
                log.error("Failed to confirm sale for item: {}, reservation: {}", item.getId(), item.getReservationId(), e);
                // Saga Compensation burada tetiklenmeli (Refund, Cancel Order)
                // Şimdilik basit tutuyoruz, logluyoruz.
            }
        }
        
        // 3. Siparişi güncelle
        order.setStatus(OrderStatus.COMPLETED);
        Order savedOrder = orderRepository.save(order);
        
        // 4. Kafka event gönder
        List<OrderCreatedEvent.OrderItemEvent> itemEvents = order.getItems().stream()
                .map(item -> new OrderCreatedEvent.OrderItemEvent(
                        item.getCategoryId(),
                        item.getCategoryName(),
                        item.getEventId(),
                        item.getEventName(),
                        item.getQuantity(),
                        item.getPrice()
                )).toList();
                
        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getOrderNumber(),
                order.getUserId(),
                order.getTotalAmount(),
                LocalDateTime.now(),
                itemEvents
        );
        
        orderProducer.sendOrderCreatedEvent(event);
        
        return savedOrder;
    }

    public Order getOrder(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}
