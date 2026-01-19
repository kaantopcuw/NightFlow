package com.nightflow.ticketservice.dto;

import com.nightflow.ticketservice.entity.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String ticketCode;
    private Long categoryId;
    private String categoryName;
    private String eventId;
    private Long orderId;
    private Long userId;
    private String seatInfo;
    private TicketStatus status;
    private LocalDateTime reservedAt;
    private LocalDateTime soldAt;
    private LocalDateTime usedAt;
}
