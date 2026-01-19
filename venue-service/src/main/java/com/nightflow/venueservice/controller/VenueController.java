package com.nightflow.venueservice.controller;

import com.nightflow.venueservice.dto.VenueRequest;
import com.nightflow.venueservice.dto.VenueReservationRequest;
import com.nightflow.venueservice.dto.VenueReservationResponse;
import com.nightflow.venueservice.dto.VenueResponse;
import com.nightflow.venueservice.entity.VenueType;
import com.nightflow.venueservice.service.VenueReservationService;
import com.nightflow.venueservice.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;
    private final VenueReservationService reservationService;

    // ─────────────── VENUE ENDPOINTS ───────────────

    @PostMapping
    public ResponseEntity<VenueResponse> create(@Valid @RequestBody VenueRequest request) {
        VenueResponse response = venueService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<VenueResponse>> findAll() {
        return ResponseEntity.ok(venueService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.findById(id));
    }

    @GetMapping("/city/{city}")
    public ResponseEntity<List<VenueResponse>> findByCity(@PathVariable String city) {
        return ResponseEntity.ok(venueService.findByCity(city));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<VenueResponse>> findByType(@PathVariable VenueType type) {
        return ResponseEntity.ok(venueService.findByType(type));
    }

    @GetMapping("/search")
    public ResponseEntity<List<VenueResponse>> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) VenueType type,
            @RequestParam(required = false) Integer minCapacity) {
        
        if (city != null && type != null) {
            return ResponseEntity.ok(venueService.findByCityAndType(city, type));
        } else if (city != null) {
            return ResponseEntity.ok(venueService.findByCity(city));
        } else if (type != null) {
            return ResponseEntity.ok(venueService.findByType(type));
        } else if (minCapacity != null) {
            return ResponseEntity.ok(venueService.findByMinCapacity(minCapacity));
        }
        return ResponseEntity.ok(venueService.findAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<VenueResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody VenueRequest request) {
        return ResponseEntity.ok(venueService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        venueService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────── RESERVATION ENDPOINTS ───────────────

    @PostMapping("/{venueId}/reserve")
    public ResponseEntity<VenueReservationResponse> reserve(
            @PathVariable Long venueId,
            @Valid @RequestBody VenueReservationRequest request) {
        request.setVenueId(venueId);
        VenueReservationResponse response = reservationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{venueId}/reservations")
    public ResponseEntity<List<VenueReservationResponse>> getReservations(@PathVariable Long venueId) {
        return ResponseEntity.ok(reservationService.findByVenueId(venueId));
    }

    @PatchMapping("/reservations/{reservationId}/confirm")
    public ResponseEntity<VenueReservationResponse> confirmReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.confirm(reservationId));
    }

    @PatchMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<VenueReservationResponse> cancelReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.cancel(reservationId));
    }
}
