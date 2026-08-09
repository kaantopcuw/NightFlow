package com.nightflow.orderservice.client;

import com.nightflow.orderservice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Calls ticket-service through Eureka.
 *
 * `name` is the Eureka service id and `url` is deliberately absent: a hard-coded
 * `url` makes spring-cloud-openfeign bypass load balancing, which is how this
 * client used to point at http://localhost:8093 - the order container's own
 * loopback. Without `url` the target is lb://ticket-service.
 */
@FeignClient(
    name = "ticket-service",
    path = "/tickets",
    configuration = FeignClientConfig.class
)
public interface TicketServiceClient {

    @PostMapping("/confirm-sale")
    void confirmSale(@RequestParam String sessionId,
                     @RequestParam Long orderId,
                     @RequestParam Long userId);

    /**
     * Compensation for {@link #confirmSale}: puts every ticket this order
     * already had confirmed back into free stock. Safe to call when nothing was
     * confirmed - it answers 0.
     */
    @PostMapping("/release-sale")
    Integer releaseSale(@RequestParam Long orderId);

    /**
     * Compensation for the reservation itself: drops the tickets still held
     * under a reservation id. A no-op once the reservation has been confirmed,
     * because confirm-sale clears the session id from the sold tickets.
     */
    @DeleteMapping("/reserve/{sessionId}")
    void cancelReservation(@PathVariable String sessionId);
}
