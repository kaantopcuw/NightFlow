package com.nightflow.ticketservice.controller;

import com.nightflow.ticketservice.dto.TicketCategoryRequest;
import com.nightflow.ticketservice.dto.TicketCategoryResponse;
import com.nightflow.ticketservice.service.TicketCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ticket-categories")
@RequiredArgsConstructor
public class TicketCategoryController {

    private final TicketCategoryService ticketCategoryService;

    @PostMapping
    public ResponseEntity<TicketCategoryResponse> create(@Valid @RequestBody TicketCategoryRequest request) {
        return new ResponseEntity<>(ticketCategoryService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<TicketCategoryResponse>> findByEventId(@PathVariable String eventId) {
        return ResponseEntity.ok(ticketCategoryService.findByEventId(eventId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketCategoryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketCategoryService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketCategoryResponse> update(@PathVariable Long id, @Valid @RequestBody TicketCategoryRequest request) {
        return ResponseEntity.ok(ticketCategoryService.update(id, request));
    }
}
