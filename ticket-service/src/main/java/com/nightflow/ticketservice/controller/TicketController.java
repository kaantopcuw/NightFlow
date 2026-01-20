package com.nightflow.ticketservice.controller;

import com.nightflow.ticketservice.dto.ReservationRequest;
import com.nightflow.ticketservice.dto.ReservationResponse;
import com.nightflow.ticketservice.dto.TicketResponse;
import com.nightflow.ticketservice.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResponse> reserveTickets(@Valid @RequestBody ReservationRequest request) {
        return new ResponseEntity<>(ticketService.reserveTickets(request), HttpStatus.CREATED);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<com.nightflow.ticketservice.entity.TicketCategory>> getCategoriesByEvent(@PathVariable String eventId) {
        return ResponseEntity.ok(ticketService.getCategoriesByEvent(eventId));
    }

    /**
     * CheckInService için: Bir event'in tüm biletlerini döndür
     */
    @GetMapping("/event/{eventId}/all")
    public ResponseEntity<List<TicketResponse>> getAllTicketsByEvent(@PathVariable String eventId) {
        return ResponseEntity.ok(ticketService.getAllTicketsByEvent(eventId));
    }

    /**
     * CheckInService için: Bilet koduna göre bilet bul
     */
    @GetMapping("/{ticketCode}")
    public ResponseEntity<TicketResponse> getTicketByCode(@PathVariable String ticketCode) {
        return ResponseEntity.ok(ticketService.getTicketByCode(ticketCode));
    }

    /**
     * CheckInService için: Bileti check-in yap (USED olarak işaretle)
     */
    @PatchMapping("/{ticketCode}/checkin")
    public ResponseEntity<Void> markAsCheckedIn(@PathVariable String ticketCode) {
        ticketService.markAsCheckedIn(ticketCode);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm-sale")
    public ResponseEntity<List<TicketResponse>> confirmSale(
            @RequestParam String sessionId,
            @RequestParam Long orderId,
            @RequestParam Long userId) {
        // Internal service communication (should be secured via network or secret, but skipping for now)
        return ResponseEntity.ok(ticketService.confirmSale(sessionId, orderId, userId));
    }

    @DeleteMapping("/reserve/{sessionId}")
    public ResponseEntity<Void> cancelReservation(@PathVariable String sessionId) {
        ticketService.cancelReservation(sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<List<TicketResponse>> getMyTickets(org.springframework.security.core.Authentication authentication) {
        Long userId = Long.parseLong((String) authentication.getPrincipal());
        return ResponseEntity.ok(ticketService.getMyTickets(userId));
    }
}
