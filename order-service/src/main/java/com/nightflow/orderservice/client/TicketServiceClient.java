package com.nightflow.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ticket-service", path = "/tickets")
public interface TicketServiceClient {

    @PostMapping("/confirm-sale")
    void confirmSale(@RequestParam String sessionId,
                     @RequestParam Long categoryId,
                     @RequestParam Long userId);
                     
    // Sepet iptali için de kullanılabilir ama burada sipariş iptalinde lazım olabilir
}
