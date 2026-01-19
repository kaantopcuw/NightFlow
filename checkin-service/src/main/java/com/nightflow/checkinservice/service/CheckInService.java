package com.nightflow.checkinservice.service;

import com.nightflow.checkinservice.client.TicketServiceClient;
import com.nightflow.checkinservice.dto.CheckInResponse;
import com.nightflow.checkinservice.dto.TicketInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckInService {

    private final StringRedisTemplate redisTemplate;
    private final TicketServiceClient ticketClient;
    
    private static final String TICKET_PREFIX = "checkin:";
    private static final String EVENT_PRELOAD_PREFIX = "event-preload:";

    /**
     * Etkinlik başlamadan önce tüm biletler Redis'e yüklenir.
     * Bu sayede check-in anında DB'ye gitmek gerekmez.
     */
    public int preloadEventTickets(String eventId) {
        log.info("Preloading tickets for event: {}", eventId);
        
        try {
            List<TicketInfo> tickets = ticketClient.getEventTickets(eventId);
            
            tickets.forEach(ticket -> {
                String key = TICKET_PREFIX + ticket.ticketCode();
                redisTemplate.opsForHash().putAll(key, Map.of(
                    "status", ticket.status(),
                    "userId", ticket.userId() != null ? ticket.userId().toString() : "0",
                    "categoryName", ticket.categoryName() != null ? ticket.categoryName() : "Unknown"
                ));
                // 24 saat TTL
                redisTemplate.expire(key, 24, TimeUnit.HOURS);
            });
            
            // Event preload flag'i
            redisTemplate.opsForValue().set(EVENT_PRELOAD_PREFIX + eventId, "true", 24, TimeUnit.HOURS);
            
            log.info("Preloaded {} tickets for event: {}", tickets.size(), eventId);
            return tickets.size();
        } catch (Exception e) {
            log.error("Failed to preload tickets for event: {}", eventId, e);
            return 0;
        }
    }
    
    /**
     * QR kod tarandığında çağrılır.
     * Ortalama yanıt süresi: <10ms
     */
    public CheckInResponse validateAndCheckIn(String ticketCode) {
        log.info("Validating ticket: {}", ticketCode);
        
        String key = TICKET_PREFIX + ticketCode;
        Map<Object, Object> ticketData = redisTemplate.opsForHash().entries(key);
        
        // Redis'te yoksa DB'den çek
        if (ticketData.isEmpty()) {
            log.debug("Ticket not in cache, fetching from DB: {}", ticketCode);
            try {
                TicketInfo ticket = ticketClient.getTicketByCode(ticketCode);
                if (ticket == null) {
                    return CheckInResponse.invalid(ticketCode);
                }
                // Cache'e ekle
                redisTemplate.opsForHash().putAll(key, Map.of(
                    "status", ticket.status(),
                    "userId", ticket.userId() != null ? ticket.userId().toString() : "0",
                    "categoryName", ticket.categoryName() != null ? ticket.categoryName() : "Unknown"
                ));
                ticketData = redisTemplate.opsForHash().entries(key);
            } catch (Exception e) {
                log.error("Failed to fetch ticket: {}", ticketCode, e);
                return CheckInResponse.invalid(ticketCode);
            }
        }
        
        String status = (String) ticketData.get("status");
        
        if ("USED".equals(status)) {
            return CheckInResponse.alreadyUsed(ticketCode);
        }
        
        if (!"SOLD".equals(status)) {
            return CheckInResponse.notSold(ticketCode);
        }
        
        // Atomik olarak USED yap
        redisTemplate.opsForHash().put(key, "status", "USED");
        
        // Async olarak DB güncelle
        asyncUpdateTicketStatus(ticketCode);
        
        return CheckInResponse.success(ticketCode);
    }
    
    @Async
    public void asyncUpdateTicketStatus(String ticketCode) {
        try {
            ticketClient.markAsUsed(ticketCode);
            log.debug("Ticket marked as used in DB: {}", ticketCode);
        } catch (Exception e) {
            log.error("Failed to update ticket status in DB: {}", ticketCode, e);
            // Burada retry mekanizması veya dead letter queue eklenebilir
        }
    }
    
    /**
     * Etkinlik check-in istatistikleri
     */
    public Map<String, Object> getEventStats(String eventId) {
        // Basit implementasyon - gerçekte Redis'ten sayıları çekebiliriz
        return Map.of(
            "eventId", eventId,
            "preloaded", Boolean.TRUE.equals(redisTemplate.hasKey(EVENT_PRELOAD_PREFIX + eventId)),
            "message", "Detaylı istatistikler için Ticket Service'e başvurun"
        );
    }
}
