package com.nightflow.eventcatalogservice.dto;

import com.nightflow.eventcatalogservice.document.EventCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Arama kriterleri
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventSearchCriteria {

    private String city;
    private EventCategory category;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String keyword;
    private Double minPrice;
    private Double maxPrice;
}
