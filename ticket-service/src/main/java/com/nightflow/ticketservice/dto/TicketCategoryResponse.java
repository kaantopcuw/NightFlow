package com.nightflow.ticketservice.dto;

import com.nightflow.ticketservice.entity.CategoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCategoryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String eventId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer totalQuantity;
    private Integer soldQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private CategoryStatus status;
    private LocalDateTime salesStartAt;
    private LocalDateTime salesEndAt;
    private LocalDateTime createdAt;
}
