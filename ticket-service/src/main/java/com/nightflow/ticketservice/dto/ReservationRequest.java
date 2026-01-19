package com.nightflow.ticketservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bilet rezervasyon isteği
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRequest {

    @NotNull(message = "Kategori ID zorunludur")
    private Long categoryId;

    @NotNull(message = "Adet zorunludur")
    @Min(value = 1, message = "En az 1 bilet seçmelisiniz")
    private Integer quantity;

    @NotBlank(message = "Session ID zorunludur")
    private String sessionId;
}
