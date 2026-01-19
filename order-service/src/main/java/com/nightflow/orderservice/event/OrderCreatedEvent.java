package com.nightflow.orderservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(
    String orderNumber,
    String userId,
    BigDecimal totalAmount,
    LocalDateTime createdAt,
    List<OrderItemEvent> items
) {
    public record OrderItemEvent(
        Long categoryId,
        String categoryName,
        String eventId,
        String eventName,
        Integer quantity,
        BigDecimal price
    ) {}
}
