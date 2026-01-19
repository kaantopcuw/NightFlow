package com.nightflow.ticketservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCategoryRequest {

    @NotBlank(message = "Event ID zorunludur")
    private String eventId;

    @NotBlank(message = "Kategori adı zorunludur")
    private String name;

    private String description;

    @NotNull(message = "Fiyat zorunludur")
    @Min(value = 0, message = "Fiyat negatif olamaz")
    private BigDecimal price;

    @NotNull(message = "Toplam adet zorunludur")
    @Min(value = 1, message = "En az 1 bilet olmalıdır")
    private Integer totalQuantity;

    private LocalDateTime salesStartAt;
    private LocalDateTime salesEndAt;
}
