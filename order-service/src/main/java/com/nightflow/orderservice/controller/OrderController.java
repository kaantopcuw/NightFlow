package com.nightflow.orderservice.controller;

import com.nightflow.orderservice.dto.OrderRequest;
import com.nightflow.orderservice.entity.Order;
import com.nightflow.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @PostMapping("/{orderNumber}/pay")
    public ResponseEntity<Order> payOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.payOrder(orderNumber));
    }
    
    @GetMapping("/{orderNumber}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrder(orderNumber));
    }
}
