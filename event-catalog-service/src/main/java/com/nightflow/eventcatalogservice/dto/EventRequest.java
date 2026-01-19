package com.nightflow.eventcatalogservice.dto;

import com.nightflow.eventcatalogservice.document.EventCategory;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {

    @NotBlank(message = "Etkinlik adı zorunludur")
    private String name;

    private String description;

    @NotBlank(message = "Slug zorunludur")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug sadece küçük harf, rakam ve tire içerebilir")
    private String slug;

    @NotNull(message = "Mekan ID zorunludur")
    private Long venueId;

    private String venueName;
    private String venueCity;

    @NotNull(message = "Organizatör ID zorunludur")
    private Long organizerId;

    private String organizerName;

    @NotNull(message = "Etkinlik tarihi zorunludur")
    @Future(message = "Etkinlik tarihi gelecekte olmalıdır")
    private LocalDateTime eventDate;

    private LocalDateTime doorsOpenAt;

    @NotNull(message = "Kategori zorunludur")
    private EventCategory category;

    private List<String> tags;

    private String posterUrl;

    private List<String> galleryUrls;

    private Double minPrice;
    private Double maxPrice;

    private Boolean featured;
}
