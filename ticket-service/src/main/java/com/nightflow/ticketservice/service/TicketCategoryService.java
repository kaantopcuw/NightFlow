package com.nightflow.ticketservice.service;

import com.nightflow.ticketservice.dto.TicketCategoryRequest;
import com.nightflow.ticketservice.dto.TicketCategoryResponse;
import com.nightflow.ticketservice.entity.TicketCategory;
import com.nightflow.ticketservice.exception.ResourceNotFoundException;
import com.nightflow.ticketservice.repository.TicketCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketCategoryService {

    private final TicketCategoryRepository ticketCategoryRepository;

    public TicketCategoryResponse create(TicketCategoryRequest request) {
        TicketCategory category = TicketCategory.builder()
                .eventId(request.getEventId())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .totalQuantity(request.getTotalQuantity())
                .salesStartAt(request.getSalesStartAt())
                .salesEndAt(request.getSalesEndAt())
                .build();

        return toResponse(ticketCategoryRepository.save(category));
    }

    public List<TicketCategoryResponse> findByEventId(String eventId) {
        return ticketCategoryRepository.findByEventId(eventId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TicketCategoryResponse findById(Long id) {
        TicketCategory category = ticketCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TicketCategory", "id", id));
        return toResponse(category);
    }

    @Transactional
    public TicketCategoryResponse update(Long id, TicketCategoryRequest request) {
        TicketCategory category = ticketCategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TicketCategory", "id", id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setPrice(request.getPrice());
        
        // Stok artırılabilir ama azaltılamaz (satılanlardan az olamaz)
        if (request.getTotalQuantity() < category.getSoldQuantity() + category.getReservedQuantity()) {
            throw new IllegalArgumentException("Toplam miktar satılan ve rezerve edilen miktardan az olamaz");
        }
        category.setTotalQuantity(request.getTotalQuantity());
        
        category.setSalesStartAt(request.getSalesStartAt());
        category.setSalesEndAt(request.getSalesEndAt());
        category.setUpdatedAt(LocalDateTime.now());

        return toResponse(ticketCategoryRepository.save(category));
    }

    private TicketCategoryResponse toResponse(TicketCategory category) {
        return TicketCategoryResponse.builder()
                .id(category.getId())
                .eventId(category.getEventId())
                .name(category.getName())
                .description(category.getDescription())
                .price(category.getPrice())
                .totalQuantity(category.getTotalQuantity())
                .soldQuantity(category.getSoldQuantity())
                .reservedQuantity(category.getReservedQuantity())
                .availableQuantity(category.getAvailableQuantity())
                .status(category.getStatus())
                .salesStartAt(category.getSalesStartAt())
                .salesEndAt(category.getSalesEndAt())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
