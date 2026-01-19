package com.nightflow.eventcatalogservice.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Etkinlik MongoDB Document
 */
@Document(collection = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    private String id;

    @Indexed
    private String name;

    private String description;

    @Indexed(unique = true)
    private String slug;

    // Venue referansı (Venue Service'den)
    private Long venueId;
    private String venueName;       // Denormalize
    private String venueCity;       // Denormalize

    // Organizatör referansı
    private Long organizerId;
    private String organizerName;   // Denormalize

    private LocalDateTime eventDate;
    private LocalDateTime doorsOpenAt;

    @Indexed
    private EventCategory category;

    @Builder.Default
    private List<String> tags = new ArrayList<>();

    private String posterUrl;

    @Builder.Default
    private List<String> galleryUrls = new ArrayList<>();

    // Fiyat aralığı (kesin fiyat Ticket Service'de)
    private Double minPrice;
    private Double maxPrice;

    @Indexed
    @Builder.Default
    private EventStatus status = EventStatus.DRAFT;

    @Builder.Default
    private Boolean featured = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
