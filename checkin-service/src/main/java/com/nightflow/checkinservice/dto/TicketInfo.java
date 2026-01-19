package com.nightflow.checkinservice.dto;

public record TicketInfo(
    String ticketCode,
    Long categoryId,
    String categoryName,
    Long userId,
    String status  // AVAILABLE, RESERVED, SOLD, USED, CANCELLED
) {}
