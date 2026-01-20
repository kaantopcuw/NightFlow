package com.nightflow.ticketservice;

import com.nightflow.ticketservice.dto.*;
import com.nightflow.ticketservice.exception.InsufficientStockException;
import com.nightflow.ticketservice.service.TicketCategoryService;
import com.nightflow.ticketservice.service.TicketService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketConcurrencyTest {

    @Autowired
    private TicketCategoryService ticketCategoryService;

    @Autowired
    private TicketService ticketService;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private com.nightflow.ticketservice.client.EventServiceClient eventServiceClient;

    private static Long categoryId;
    private static final String EVENT_ID = "concurrency-test-event";
    private static final String ORGANIZER_ID = "test-organizer";

    @Test
    @Order(1)
    public void testCreateCategory() {
        System.out.println("1. Kategori Oluştur (Stok: 2)");
        
        // Mock Event ownership check
        com.nightflow.ticketservice.dto.EventResponse eventMock = new com.nightflow.ticketservice.dto.EventResponse();
        eventMock.setId(EVENT_ID);
        eventMock.setOrganizerId(ORGANIZER_ID);
        org.mockito.Mockito.when(eventServiceClient.getEvent(EVENT_ID)).thenReturn(eventMock);
        
        TicketCategoryRequest request = TicketCategoryRequest.builder()
                .eventId(EVENT_ID)
                .name("Concurrency Kategori")
                .description("Test Description")
                .price(BigDecimal.valueOf(100))
                .totalQuantity(2)
                .build();

        TicketCategoryResponse response = ticketCategoryService.create(request, ORGANIZER_ID);
        assertNotNull(response.getId());
        categoryId = response.getId();
        System.out.println("Kategori ID: " + categoryId);
    }

    @Test
    @Order(2)
    public void testUser1Reserve() {
        System.out.println("2. Kullanıcı 1 Rezerve Et (1 adet)");
        ReservationRequest request = ReservationRequest.builder()
                .categoryId(categoryId)
                .quantity(1)
                .sessionId("user1-session")
                .build();

        ReservationResponse response = ticketService.reserveTickets(request);
        assertNotNull(response.getSessionId());
        assertEquals(1, response.getTicketCodes().size());
    }

    @Test
    @Order(3)
    public void testUser2Reserve() {
        System.out.println("3. Kullanıcı 2 Rezerve Et (1 adet)");
        ReservationRequest request = ReservationRequest.builder()
                .categoryId(categoryId)
                .quantity(1)
                .sessionId("user2-session")
                .build();

        ReservationResponse response = ticketService.reserveTickets(request);
        assertNotNull(response.getSessionId());
        assertEquals(1, response.getTicketCodes().size());
    }

    @Test
    @Order(4)
    public void testUser3ReserveFail() {
        System.out.println("4. Kullanıcı 3 Rezerve Et (1 adet) -> HATA Bekleniyor");
        ReservationRequest request = ReservationRequest.builder()
                .categoryId(categoryId)
                .quantity(1)
                .sessionId("user3-session")
                .build();

        assertThrows(InsufficientStockException.class, () -> {
            ticketService.reserveTickets(request);
        });
    }

    @Test
    @Order(5)
    public void testUser1Cancel() {
        System.out.println("5. Kullanıcı 1 İptal Et (Session: user1-session)");
        ticketService.cancelReservation("user1-session");
        // Kontrol et
        TicketCategoryResponse category = ticketCategoryService.findById(categoryId);
        assertEquals(1, category.getAvailableQuantity()); // 1 tane boşa çıkmalı
    }

    @Test
    @Order(6)
    public void testUser2ConfirmSale() {
        System.out.println("6. Kullanıcı 2 Satın Al (Confirm)");
        var tickets = ticketService.confirmSale("user2-session", 1002L, 505L);
        assertEquals(1, tickets.size());
        
        TicketCategoryResponse category = ticketCategoryService.findById(categoryId);
        assertEquals(1, category.getSoldQuantity());
    }

    @Test
    @Order(7)
    public void testUser3ReserveSuccess() {
        System.out.println("7. Kullanıcı 3 Rezerve Et (1 adet) -> BAŞARILI Bekleniyor");
        ReservationRequest request = ReservationRequest.builder()
                .categoryId(categoryId)
                .quantity(1)
                .sessionId("user3-session")
                .build();

        ReservationResponse response = ticketService.reserveTickets(request);
        assertNotNull(response.getSessionId());
    }

    @Test
    @Order(8)
    public void testUser4ReserveFail() {
        System.out.println("8. Kullanıcı 4 Rezerve Et (1 adet) -> HATA Bekleniyor");
        ReservationRequest request = ReservationRequest.builder()
                .categoryId(categoryId)
                .quantity(1)
                .sessionId("user4-session")
                .build();

        assertThrows(InsufficientStockException.class, () -> {
            ticketService.reserveTickets(request);
        });
    }
}
