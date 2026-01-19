package com.nightflow.eventcatalogservice.controller;

import com.nightflow.eventcatalogservice.document.EventCategory;
import com.nightflow.eventcatalogservice.document.EventStatus;
import com.nightflow.eventcatalogservice.dto.EventRequest;
import com.nightflow.eventcatalogservice.dto.EventResponse;
import com.nightflow.eventcatalogservice.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody EventRequest request) {
        EventResponse response = eventService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<EventResponse>> findAll(Pageable pageable) {
        return ResponseEntity.ok(eventService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(eventService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<EventResponse> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(eventService.findBySlug(slug));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<EventResponse>> findFeatured() {
        return ResponseEntity.ok(eventService.findFeatured());
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<EventResponse>> findUpcoming() {
        return ResponseEntity.ok(eventService.findUpcoming());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<EventResponse>> findByCategory(@PathVariable EventCategory category) {
        return ResponseEntity.ok(eventService.findByCategory(category));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<EventResponse>> findByCity(@PathVariable String city) {
        return ResponseEntity.ok(eventService.findByCity(city));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponse> update(
            @PathVariable String id,
            @Valid @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<EventResponse> updateStatus(
            @PathVariable String id,
            @RequestParam EventStatus status) {
        return ResponseEntity.ok(eventService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        eventService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
