package com.nightflow.eventcatalogservice.document;

/**
 * Etkinlik durumu
 */
public enum EventStatus {
    DRAFT,      // Taslak
    PUBLISHED,  // Yayında
    SOLD_OUT,   // Tükendi
    CANCELLED   // İptal edildi
}
