package com.nightflow.venueservice.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueReservationRequest {

    @NotNull(message = "Mekan ID zorunludur")
    private Long venueId;

    @NotNull(message = "Organizatör ID zorunludur")
    private Long organizerId;

    @NotNull(message = "Etkinlik tarihi zorunludur")
    @Future(message = "Etkinlik tarihi gelecekte olmalıdır")
    private LocalDateTime eventDate;
}
