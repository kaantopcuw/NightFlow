package com.nightflow.checkinservice.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckInRequest(
    @NotBlank(message = "Ticket code is required")
    String ticketCode
) {}
