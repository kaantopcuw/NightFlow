package com.nightflow.shoppingcartservice.client;

import com.nightflow.shoppingcartservice.config.FeignClientConfig;
import com.nightflow.shoppingcartservice.dto.ReservationRequest;
import com.nightflow.shoppingcartservice.dto.ReservationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "ticket-service-client", 
    url = "http://localhost:8093",
    path = "/tickets",
    configuration = FeignClientConfig.class
)
public interface TicketServiceClient {

    @PostMapping("/reserve")
    ReservationResponse reserveTickets(@RequestBody ReservationRequest request);

    @DeleteMapping("/reserve/{sessionId}")
    void cancelReservation(@PathVariable String sessionId);
}
