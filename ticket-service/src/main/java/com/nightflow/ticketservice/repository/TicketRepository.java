package com.nightflow.ticketservice.repository;

import com.nightflow.ticketservice.entity.Ticket;
import com.nightflow.ticketservice.entity.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    List<Ticket> findByCategoryId(Long categoryId);

    List<Ticket> findByCategoryIdAndStatus(Long categoryId, TicketStatus status);

    /**
     * CheckInService için: Birden fazla kategori ID'sine göre bilet getir
     */
    List<Ticket> findByCategoryIdInAndStatus(List<Long> categoryIds, TicketStatus status);

    List<Ticket> findByOrderId(Long orderId);

    List<Ticket> findByUserId(Long userId);

    /**
     * Belirli sayıda available bilet getir (pessimistic lock ile)
     */
    @Query(value = "SELECT * FROM tickets WHERE category_id = :categoryId AND status = 'AVAILABLE' ORDER BY id LIMIT :count FOR UPDATE", nativeQuery = true)
    List<Ticket> findAvailableTicketsForReservation(@Param("categoryId") Long categoryId, @Param("count") int count);

    /**
     * Session ID ile rezerve edilmiş biletleri getir
     */
    List<Ticket> findBySessionIdAndStatus(String sessionId, TicketStatus status);

    /**
     * Süresi dolmuş rezervasyonları temizle
     */
    /**
     * Süresi dolmuş rezervasyonları getir
     */
    @Query("SELECT t FROM Ticket t WHERE t.status = 'RESERVED' AND t.reservedAt < :expireTime")
    List<Ticket> findExpiredReservations(@Param("expireTime") LocalDateTime expireTime);

    /**
     * Session için bilet sayısı
     */
    long countBySessionIdAndStatus(String sessionId, TicketStatus status);
}
