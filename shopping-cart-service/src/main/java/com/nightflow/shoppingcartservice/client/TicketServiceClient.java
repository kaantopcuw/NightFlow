package com.nightflow.shoppingcartservice.client;

import com.nightflow.shoppingcartservice.config.FeignClientConfig;
import com.nightflow.shoppingcartservice.dto.ReservationRequest;
import com.nightflow.shoppingcartservice.dto.ReservationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Calls ticket-service through Eureka.
 *
 * `name` is the Eureka service id and `url` is deliberately absent: a hard-coded
 * `url` makes spring-cloud-openfeign bypass load balancing, which is how this
 * client used to point at http://localhost:8093 - the shopping-cart container's
 * own loopback. Without `url` the target is lb://ticket-service.
 */
@FeignClient(
    name = "ticket-service",
    path = "/tickets",
    configuration = FeignClientConfig.class
)
public interface TicketServiceClient {

    @PostMapping("/reserve")
    ReservationResponse reserveTickets(@RequestBody ReservationRequest request);

    @DeleteMapping("/reserve/{sessionId}")
    void cancelReservation(@PathVariable String sessionId);
}
