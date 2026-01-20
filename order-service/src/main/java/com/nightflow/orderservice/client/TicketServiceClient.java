package com.nightflow.orderservice.client;

import com.nightflow.orderservice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
    name = "ticket-service-client", 
    url = "http://localhost:8093",
    path = "/tickets",
    configuration = FeignClientConfig.class
)
public interface TicketServiceClient {

    @PostMapping("/confirm-sale")
    void confirmSale(@RequestParam String sessionId,
                     @RequestParam Long categoryId,
                     @RequestParam Long userId);
}
