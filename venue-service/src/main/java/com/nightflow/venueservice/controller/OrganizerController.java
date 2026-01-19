package com.nightflow.venueservice.controller;

import com.nightflow.venueservice.dto.OrganizerRequest;
import com.nightflow.venueservice.dto.OrganizerResponse;
import com.nightflow.venueservice.entity.OrganizerStatus;
import com.nightflow.venueservice.service.OrganizerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organizers")
@RequiredArgsConstructor
public class OrganizerController {

    private final OrganizerService organizerService;

    @PostMapping
    public ResponseEntity<OrganizerResponse> create(@Valid @RequestBody OrganizerRequest request) {
        OrganizerResponse response = organizerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrganizerResponse>> findAll() {
        return ResponseEntity.ok(organizerService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizerResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(organizerService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<OrganizerResponse> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(organizerService.findBySlug(slug));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody OrganizerRequest request) {
        return ResponseEntity.ok(organizerService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrganizerResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam OrganizerStatus status) {
        return ResponseEntity.ok(organizerService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        organizerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
