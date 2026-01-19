package com.nightflow.eventcatalogservice.dto;

import com.nightflow.eventcatalogservice.document.EventCategory;
import com.nightflow.eventcatalogservice.document.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String description;
    private String slug;

    private Long venueId;
    private String venueName;
    private String venueCity;

    private Long organizerId;
    private String organizerName;

    private LocalDateTime eventDate;
    private LocalDateTime doorsOpenAt;

    private EventCategory category;
    private List<String> tags;

    private String posterUrl;
    private List<String> galleryUrls;

    private Double minPrice;
    private Double maxPrice;

    private EventStatus status;
    private Boolean featured;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
