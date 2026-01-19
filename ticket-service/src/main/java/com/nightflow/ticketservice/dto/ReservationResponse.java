package com.nightflow.ticketservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Rezervasyon yanıtı
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {

    private String sessionId;
    private Long categoryId;
    private Integer quantity;
    private List<String> ticketCodes;
    private LocalDateTime reservedAt;
    private LocalDateTime expiresAt;
    private String message;
}
