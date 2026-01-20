package com.nightflow.ticketservice.service;

import com.nightflow.ticketservice.dto.*;
import com.nightflow.ticketservice.entity.Ticket;
import com.nightflow.ticketservice.entity.TicketCategory;
import com.nightflow.ticketservice.entity.CategoryStatus;
import com.nightflow.ticketservice.entity.TicketStatus;
import com.nightflow.ticketservice.exception.InsufficientStockException;
import com.nightflow.ticketservice.exception.ResourceNotFoundException;
import com.nightflow.ticketservice.repository.TicketCategoryRepository;
import com.nightflow.ticketservice.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketCategoryRepository ticketCategoryRepository;

    @Transactional
    public ReservationResponse reserveTickets(ReservationRequest request) {
        log.info("Rezervasyon isteği: {}", request);
        
        TicketCategory category = ticketCategoryRepository.findByIdWithLock(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (category.getAvailableQuantity() < request.getQuantity()) {
            throw new InsufficientStockException("Yetersiz stok. Mevcut: " + category.getAvailableQuantity());
        }

        category.setReservedQuantity(category.getReservedQuantity() + request.getQuantity());
        category.setUpdatedAt(LocalDateTime.now());
        ticketCategoryRepository.save(category);

        List<String> ticketCodes = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < request.getQuantity(); i++) {
            Ticket ticket = Ticket.builder()
                    .category(category)
                    .status(TicketStatus.RESERVED)
                    .sessionId(request.getSessionId())
                    .reservedAt(now)
                    .createdAt(now)
                    .build();
            
            Ticket savedTicket = ticketRepository.save(ticket);
            ticketCodes.add(savedTicket.getTicketCode());
        }

        return ReservationResponse.builder()
                .sessionId(request.getSessionId())
                .categoryId(category.getId())
                .quantity(request.getQuantity())
                .ticketCodes(ticketCodes)
                .reservedAt(now)
                .expiresAt(now.plusMinutes(15))
                .message("Biletler geçici olarak rezerve edildi.")
                .build();
    }

    @Transactional
    public List<TicketResponse> confirmSale(String sessionId, Long orderId, Long userId) {
        List<Ticket> reservedTickets = ticketRepository.findBySessionIdAndStatus(sessionId, TicketStatus.RESERVED);
        
        if (reservedTickets.isEmpty()) {
            throw new ResourceNotFoundException("Rezervasyon bulunamadı veya süresi dolmuş.");
        }

        List<TicketResponse> responses = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        reservedTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getCategory))
                .forEach((category, tickets) -> {
                    TicketCategory lockedCategory = ticketCategoryRepository.findByIdWithLock(category.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Category", "id", category.getId()));
                    
                    int quantity = tickets.size();
                    lockedCategory.setReservedQuantity(lockedCategory.getReservedQuantity() - quantity);
                    lockedCategory.setSoldQuantity(lockedCategory.getSoldQuantity() + quantity);
                    lockedCategory.setUpdatedAt(now);
                    ticketCategoryRepository.save(lockedCategory);
                });

        for (Ticket ticket : reservedTickets) {
            ticket.setStatus(TicketStatus.SOLD);
            ticket.setOrderId(orderId);
            ticket.setUserId(userId);
            ticket.setSoldAt(now);
            ticket.setSessionId(null);
            
            Ticket saved = ticketRepository.save(ticket);
            responses.add(toResponse(saved));
        }

        return responses;
    }
    
    /**
     * Süresi Dolmuş Rezervasyonları Temizle (Scheduled Job)
     * Her dakika çalışır
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void releaseExpiredReservations() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(15);
        
        List<Ticket> expiredTickets = ticketRepository.findExpiredReservations(expirationTime);
        
        if (expiredTickets.isEmpty()) {
            return;
        }

        log.info("{} adet süresi dolmuş rezervasyon temizleniyor...", expiredTickets.size());
        
        expiredTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getCategory))
                .forEach((category, tickets) -> {
                    // Kategori stokunu güncelle
                    try {
                        TicketCategory lockedCategory = ticketCategoryRepository.findByIdWithLock(category.getId())
                                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", category.getId()));
                        
                        // Negatif stok kontrolü (güvenlik için)
                        int quantityToRelease = tickets.size();
                        if (lockedCategory.getReservedQuantity() >= quantityToRelease) {
                            lockedCategory.setReservedQuantity(lockedCategory.getReservedQuantity() - quantityToRelease);
                        } else {
                            lockedCategory.setReservedQuantity(0);
                            log.warn("Kategori ID {} için reserved stok tutarsızlığı tespit edildi!", category.getId());
                        }
                        
                        lockedCategory.setUpdatedAt(LocalDateTime.now());
                        ticketCategoryRepository.save(lockedCategory);
                    } catch (Exception e) {
                        log.error("Expired rezervasyon temizlenirken hata: {}", e.getMessage());
                    }
                });

        // Biletleri veritabanından sil (Stok geri döndü)
        ticketRepository.deleteAll(expiredTickets);
        log.info("Temizleme tamamlandı.");
    }
    
    /**
     * Rezervasyon İptali (Kullanıcı sepeti boşalttı veya vazgeçti)
     */
    @Transactional
    public void cancelReservation(String sessionId) {
         List<Ticket> reservedTickets = ticketRepository.findBySessionIdAndStatus(sessionId, TicketStatus.RESERVED);
         
         if (reservedTickets.isEmpty()) {
             return;
         }
         
         reservedTickets.stream()
                .collect(Collectors.groupingBy(Ticket::getCategory))
                .forEach((category, tickets) -> {
                    TicketCategory lockedCategory = ticketCategoryRepository.findByIdWithLock(category.getId())
                            .orElseThrow();
                    
                    lockedCategory.setReservedQuantity(lockedCategory.getReservedQuantity() - tickets.size());
                    ticketCategoryRepository.save(lockedCategory);
                });
         
         ticketRepository.deleteAll(reservedTickets);
    }
    
    public List<TicketCategory> getCategoriesByEvent(String eventId) {
        return ticketCategoryRepository.findByEventIdAndStatus(eventId, CategoryStatus.AVAILABLE);
    }

    /**
     * CheckInService için: Bir event'in tüm biletlerini döndür (SOLD olanlar)
     */
    public List<TicketResponse> getAllTicketsByEvent(String eventId) {
        List<TicketCategory> categories = ticketCategoryRepository.findByEventId(eventId);
        List<Long> categoryIds = categories.stream().map(TicketCategory::getId).toList();
        
        if (categoryIds.isEmpty()) {
            return List.of();
        }
        
        return ticketRepository.findByCategoryIdInAndStatus(categoryIds, TicketStatus.SOLD)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * CheckInService için: Bilet koduna göre bilet bul
     */
    public TicketResponse getTicketByCode(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "code", ticketCode));
        return toResponse(ticket);
    }

    /**
     * CheckInService için: Bileti check-in yap (USED olarak işaretle)
     */
    @Transactional
    public void markAsCheckedIn(String ticketCode) {
        Ticket ticket = ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "code", ticketCode));
        
        ticket.setStatus(TicketStatus.USED);
        ticket.setUsedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        
        log.info("Ticket checked in: {}", ticketCode);
    }

    public List<TicketResponse> getMyTickets(Long userId) {
        return ticketRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private TicketResponse toResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketCode(ticket.getTicketCode())
                .categoryId(ticket.getCategory().getId())
                .categoryName(ticket.getCategory().getName())
                .eventId(ticket.getCategory().getEventId())
                .orderId(ticket.getOrderId())
                .userId(ticket.getUserId())
                .seatInfo(ticket.getSeatInfo())
                .status(ticket.getStatus())
                .reservedAt(ticket.getReservedAt())
                .soldAt(ticket.getSoldAt())
                .usedAt(ticket.getUsedAt())
                .build();
    }
}
