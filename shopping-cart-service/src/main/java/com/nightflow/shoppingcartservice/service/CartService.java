package com.nightflow.shoppingcartservice.service;

import com.nightflow.shoppingcartservice.client.TicketServiceClient;
import com.nightflow.shoppingcartservice.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final TicketServiceClient ticketServiceClient;

    private static final String CART_PREFIX = "cart:";
    // Sepet süresi (ve Ticket Service'deki rezervasyon süresi ile uyumlu olmalı)
    // Ticket Service 15 dk veriyor, biz de 15 dk tutalım.
    private static final Duration CART_TTL = Duration.ofMinutes(15);

    public Cart getCart(String sessionId) {
        String key = CART_PREFIX + sessionId;
        Cart cart = (Cart) redisTemplate.opsForValue().get(key);
        
        if (cart == null) {
            cart = Cart.builder()
                    .userId(sessionId) // userId veya sessionId
                    .items(new ArrayList<>())
                    .build();
        }
        return cart;
    }

    public Cart addToCart(AddToCartRequest request) {
        log.info("Adding to cart: {}", request);
        
        // Her item için unique bir reservation ID oluştur
        String reservationId = UUID.randomUUID().toString();
        
        // 1. Ticket Service'den rezerve et
        ReservationRequest reservationRequest = ReservationRequest.builder()
                .categoryId(request.getCategoryId())
                .quantity(request.getQuantity())
                .sessionId(reservationId) // Unique ID
                .build();
        
        // Bu çağrı hata fırlatırsa (yetersiz stok vs.) işlem kesilir ve sepete eklenmez.
        ReservationResponse reservation = ticketServiceClient.reserveTickets(reservationRequest);
        
        // 2. Redis'teki sepeti güncelle
        Cart cart = getCart(request.getSessionId()); // Kullanıcının sepeti
        
        CartItem newItem = CartItem.builder()
                .categoryId(request.getCategoryId())
                .categoryName(request.getCategoryName())
                .eventId(request.getEventId())
                .eventName(request.getEventName())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .reservationId(reservationId) // Kaydedilen unique ID
                .remainingTime(15 * 60L) 
                .build();
        
        cart.getItems().add(newItem);
        
        String key = CART_PREFIX + request.getSessionId();
        redisTemplate.opsForValue().set(key, cart, CART_TTL);
        
        return cart;
    }

    public Cart removeFromCart(String sessionId, int itemIndex) {
        Cart cart = getCart(sessionId);
        
        if (itemIndex >= 0 && itemIndex < cart.getItems().size()) {
            CartItem item = cart.getItems().get(itemIndex);
            
            try {
                // Sadece o item'a ait rezervasyonu iptal et
                ticketServiceClient.cancelReservation(item.getReservationId());
            } catch (Exception e) {
                log.error("Rezervasyon iptal edilirken hata oluştu: {}", e.getMessage());
            }

            cart.getItems().remove(itemIndex);
            
            if (cart.getItems().isEmpty()) {
                redisTemplate.delete(CART_PREFIX + sessionId);
            } else {
                redisTemplate.opsForValue().set(CART_PREFIX + sessionId, cart, CART_TTL);
            }
        }
        
        return cart;
    }
    
    public void clearCart(String sessionId) {
        Cart cart = getCart(sessionId);
        for (CartItem item : cart.getItems()) {
             try {
                 ticketServiceClient.cancelReservation(item.getReservationId());
             } catch (Exception e) {
                 log.error("Clear cart hata: {}", e.getMessage());
             }
        }
        redisTemplate.delete(CART_PREFIX + sessionId);
    }
}
