package com.nightflow.orderservice.dto;

import java.math.BigDecimal;

public record OrderItemRequest(
    Long categoryId,
    String categoryName,
    String eventId,
    String eventName,
    Integer quantity,
    BigDecimal price,
    String reservationId
) {}
