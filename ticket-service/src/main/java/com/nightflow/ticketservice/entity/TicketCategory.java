package com.nightflow.ticketservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Bilet kategorisi - VIP, Genel, Balkon vb.
 */
@Entity
@Table(name = "ticket_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventId;  // MongoDB Event ID

    @Column(nullable = false)
    private String name;  // VIP, Genel, Balkon

    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer totalQuantity;  // Toplam koltuk

    @Builder.Default
    private Integer soldQuantity = 0;  // Satılan

    @Builder.Default
    private Integer reservedQuantity = 0;  // Sepette bekleyen

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CategoryStatus status = CategoryStatus.AVAILABLE;

    private LocalDateTime salesStartAt;
    private LocalDateTime salesEndAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Mevcut stok sayısı
     */
    public Integer getAvailableQuantity() {
        return totalQuantity - soldQuantity - reservedQuantity;
    }
}
