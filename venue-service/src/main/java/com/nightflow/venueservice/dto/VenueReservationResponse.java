package com.nightflow.venueservice.dto;

import com.nightflow.venueservice.entity.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueReservationResponse {

    private Long id;
    private Long venueId;
    private String venueName;
    private Long organizerId;
    private String organizerName;
    private LocalDateTime eventDate;
    private LocalDateTime reservedAt;
    private ReservationStatus status;
}
