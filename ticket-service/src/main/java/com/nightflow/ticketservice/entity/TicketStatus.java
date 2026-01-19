package com.nightflow.ticketservice.entity;

/**
 * Bilet durumu
 */
public enum TicketStatus {
    AVAILABLE,   // Satışa hazır
    RESERVED,    // Sepette bekliyor (geçici kilit)
    SOLD,        // Satıldı
    USED,        // Kullanıldı (check-in yapıldı)
    CANCELLED    // İptal edildi
}
