package com.nightflow.eventcatalogservice.service;

import com.nightflow.eventcatalogservice.document.Event;
import com.nightflow.eventcatalogservice.document.EventCategory;
import com.nightflow.eventcatalogservice.document.EventStatus;
import com.nightflow.eventcatalogservice.dto.EventRequest;
import com.nightflow.eventcatalogservice.dto.EventResponse;
import com.nightflow.eventcatalogservice.exception.ResourceAlreadyExistsException;
import com.nightflow.eventcatalogservice.exception.ResourceNotFoundException;
import com.nightflow.eventcatalogservice.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public EventResponse create(EventRequest request) {
        if (eventRepository.existsBySlug(request.getSlug())) {
            throw new ResourceAlreadyExistsException("Bu slug zaten kullanılıyor: " + request.getSlug());
        }

        Event event = Event.builder()
                .name(request.getName())
                .description(request.getDescription())
                .slug(request.getSlug())
                .venueId(request.getVenueId())
                .venueName(request.getVenueName())
                .venueCity(request.getVenueCity())
                .organizerId(request.getOrganizerId())
                .organizerName(request.getOrganizerName())
                .eventDate(request.getEventDate())
                .doorsOpenAt(request.getDoorsOpenAt())
                .category(request.getCategory())
                .tags(request.getTags())
                .posterUrl(request.getPosterUrl())
                .galleryUrls(request.getGalleryUrls())
                .minPrice(request.getMinPrice())
                .maxPrice(request.getMaxPrice())
                .featured(request.getFeatured() != null ? request.getFeatured() : false)
                .status(EventStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Event saved = eventRepository.save(event);
        return toResponse(saved);
    }

    public Page<EventResponse> findAll(Pageable pageable) {
        return eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED, pageable)
                .map(this::toResponse);
    }

    @Cacheable(value = "events", key = "#id")
    public EventResponse findById(String id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etkinlik", id));
        return toResponse(event);
    }

    @Cacheable(value = "events", key = "'slug:' + #slug")
    public EventResponse findBySlug(String slug) {
        Event event = eventRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Etkinlik", slug));
        return toResponse(event);
    }

    @Cacheable(value = "featured-events")
    public List<EventResponse> findFeatured() {
        return eventRepository.findByFeatured(true).stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLISHED)
                .map(this::toResponse)
                .toList();
    }

    @Cacheable(value = "upcoming-events")
    public List<EventResponse> findUpcoming() {
        return eventRepository.findUpcomingEvents(LocalDateTime.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EventResponse> findByCategory(EventCategory category) {
        return eventRepository.findByCategoryAndUpcoming(category, LocalDateTime.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<EventResponse> findByCity(String city) {
        return eventRepository.findByCity(city).stream()
                .map(this::toResponse)
                .toList();
    }

    @CacheEvict(value = {"events", "featured-events", "upcoming-events"}, allEntries = true)
    public EventResponse update(String id, EventRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etkinlik", id));

        if (!event.getSlug().equals(request.getSlug()) && eventRepository.existsBySlug(request.getSlug())) {
            throw new ResourceAlreadyExistsException("Bu slug zaten kullanılıyor: " + request.getSlug());
        }

        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setSlug(request.getSlug());
        event.setVenueId(request.getVenueId());
        event.setVenueName(request.getVenueName());
        event.setVenueCity(request.getVenueCity());
        event.setOrganizerId(request.getOrganizerId());
        event.setOrganizerName(request.getOrganizerName());
        event.setEventDate(request.getEventDate());
        event.setDoorsOpenAt(request.getDoorsOpenAt());
        event.setCategory(request.getCategory());
        event.setTags(request.getTags());
        event.setPosterUrl(request.getPosterUrl());
        event.setGalleryUrls(request.getGalleryUrls());
        event.setMinPrice(request.getMinPrice());
        event.setMaxPrice(request.getMaxPrice());
        event.setFeatured(request.getFeatured());
        event.setUpdatedAt(LocalDateTime.now());

        Event updated = eventRepository.save(event);
        return toResponse(updated);
    }

    @CacheEvict(value = {"events", "featured-events", "upcoming-events"}, allEntries = true)
    public EventResponse updateStatus(String id, EventStatus status) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Etkinlik", id));

        event.setStatus(status);
        event.setUpdatedAt(LocalDateTime.now());

        Event updated = eventRepository.save(event);
        return toResponse(updated);
    }

    @CacheEvict(value = {"events", "featured-events", "upcoming-events"}, allEntries = true)
    public void delete(String id) {
        if (!eventRepository.existsById(id)) {
            throw new ResourceNotFoundException("Etkinlik", id);
        }
        eventRepository.deleteById(id);
    }

    private EventResponse toResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .slug(event.getSlug())
                .venueId(event.getVenueId())
                .venueName(event.getVenueName())
                .venueCity(event.getVenueCity())
                .organizerId(event.getOrganizerId())
                .organizerName(event.getOrganizerName())
                .eventDate(event.getEventDate())
                .doorsOpenAt(event.getDoorsOpenAt())
                .category(event.getCategory())
                .tags(event.getTags())
                .posterUrl(event.getPosterUrl())
                .galleryUrls(event.getGalleryUrls())
                .minPrice(event.getMinPrice())
                .maxPrice(event.getMaxPrice())
                .status(event.getStatus())
                .featured(event.getFeatured())
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
