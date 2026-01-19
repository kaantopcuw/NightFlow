package com.nightflow.orderservice.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderRequest(
    String userId,
    List<OrderItemRequest> items,
    BigDecimal totalAmount
) {}
