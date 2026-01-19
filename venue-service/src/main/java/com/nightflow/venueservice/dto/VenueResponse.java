package com.nightflow.venueservice.dto;

import com.nightflow.venueservice.entity.VenueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueResponse {

    private Long id;
    private String name;
    private String address;
    private String city;
    private String district;
    private Integer capacity;
    private String mapUrl;
    private String imageUrl;
    private VenueType type;
    private LocalDateTime createdAt;
}
