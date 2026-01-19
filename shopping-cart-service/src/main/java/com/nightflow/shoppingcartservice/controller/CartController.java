package com.nightflow.shoppingcartservice.controller;

import com.nightflow.shoppingcartservice.dto.AddToCartRequest;
import com.nightflow.shoppingcartservice.dto.Cart;
import com.nightflow.shoppingcartservice.dto.CartItem;
import com.nightflow.shoppingcartservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/{sessionId}")
    public ResponseEntity<Cart> getCart(@PathVariable String sessionId) {
        return ResponseEntity.ok(cartService.getCart(sessionId));
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@Valid @RequestBody AddToCartRequest request) {
        // Her item için unique reservation ID (sub-session) oluşturuyoruz Ticket Service için
        // Kullanıcının ana session ID'si cart key'i oluyor.
        // Ticket service'e giden session ID ise unique oluyor.
        // Böylece item bazlı iptal yapabiliriz.
        // Ancak AddToCartRequest içinde sessionId geliyor.
        
        // CartService içinde bu mantığı kuralım, request'i manipüle etmek yerine.
        // CartService.addToCart metodunda request.sessionId'yi override edelim (reserveration için).
        // Ama Cart içinde userSessionId tutulmalı.
        
        // Gelin CartService'i düzeltelim, burası sadece pass-through olsun.
        return ResponseEntity.ok(cartService.addToCart(request));
    }

    @DeleteMapping("/{sessionId}/item/{index}")
    public ResponseEntity<Cart> removeFromCart(@PathVariable String sessionId, @PathVariable int index) {
        return ResponseEntity.ok(cartService.removeFromCart(sessionId, index));
    }
    
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> clearCart(@PathVariable String sessionId) {
        cartService.clearCart(sessionId);
        return ResponseEntity.noContent().build();
    }
}
