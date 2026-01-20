package com.nightflow.checkinservice.controller;

import com.nightflow.checkinservice.dto.CheckInRequest;
import com.nightflow.checkinservice.dto.CheckInResponse;
import com.nightflow.checkinservice.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/checkin")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping("/validate")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('GATEKEEPER')")
    public ResponseEntity<CheckInResponse> validateAndCheckIn(@Valid @RequestBody CheckInRequest request) {
        return ResponseEntity.ok(checkInService.validateAndCheckIn(request.ticketCode()));
    }
    
    @GetMapping("/ticket/{ticketCode}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('GATEKEEPER')")
    public ResponseEntity<CheckInResponse> getTicketStatus(@PathVariable String ticketCode) {
        return ResponseEntity.ok(checkInService.validateAndCheckIn(ticketCode));
    }
    
    @PostMapping("/event/{eventId}/preload")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Map<String, Object>> preloadEventTickets(@PathVariable String eventId) {
        int count = checkInService.preloadEventTickets(eventId);
        return ResponseEntity.ok(Map.of(
            "eventId", eventId,
            "ticketsPreloaded", count,
            "message", count > 0 ? "Biletler cache'e yüklendi" : "Bilet bulunamadı veya hata oluştu"
        ));
    }
    
    @GetMapping("/event/{eventId}/stats")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Map<String, Object>> getEventStats(@PathVariable String eventId) {
        return ResponseEntity.ok(checkInService.getEventStats(eventId));
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "checkin-service"
        ));
    }
}
