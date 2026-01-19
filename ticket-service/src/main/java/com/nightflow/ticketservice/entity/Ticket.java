package com.nightflow.ticketservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bireysel bilet
 */
@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String ticketCode;  // UUID - QR için

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private TicketCategory category;

    private Long orderId;   // Order Service referansı
    private Long userId;    // Auth Service referansı

    private String seatInfo;  // "A-15" veya null (unnumbered)

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TicketStatus status = TicketStatus.AVAILABLE;

    private String sessionId;      // Rezervasyon session ID
    private LocalDateTime reservedAt;
    private LocalDateTime soldAt;
    private LocalDateTime usedAt;  // Check-in zamanı

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (ticketCode == null) {
            ticketCode = UUID.randomUUID().toString();
        }
    }
}
