package com.nightflow.orderservice.controller;

import com.nightflow.orderservice.dto.OrderRequest;
import com.nightflow.orderservice.entity.Order;
import com.nightflow.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request, org.springframework.security.core.Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.createOrder(request, userId));
    }

    @PostMapping("/{orderNumber}/pay")
    public ResponseEntity<Order> payOrder(@PathVariable String orderNumber, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        Order order = orderService.getOrder(orderNumber);
        
        if (!order.getUserId().equals(userId)) {
            throw new AccessDeniedException("Bu siparişi ödeme yetkiniz yok");
        }
        return ResponseEntity.ok(orderService.payOrder(orderNumber));
    }
    
    @GetMapping("/{orderNumber}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderNumber, Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        Order order = orderService.getOrder(orderNumber);
        
        if (!order.getUserId().equals(userId)) {
            throw new AccessDeniedException("Bu siparişi görüntüleme yetkiniz yok");
        }
        return ResponseEntity.ok(order);
    }

    @GetMapping("/my-orders")
    public ResponseEntity<java.util.List<Order>> getMyOrders(org.springframework.security.core.Authentication authentication) {
        String userId = (String) authentication.getPrincipal();
        return ResponseEntity.ok(orderService.getMyOrders(userId));
    }
}
