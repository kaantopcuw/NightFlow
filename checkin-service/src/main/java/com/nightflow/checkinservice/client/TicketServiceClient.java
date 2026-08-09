package com.nightflow.checkinservice.client;

import com.nightflow.checkinservice.config.FeignClientConfig;
import com.nightflow.checkinservice.dto.TicketInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;

/**
 * Calls ticket-service through Eureka.
 *
 * `name` is the Eureka service id and `url` is deliberately absent: a hard-coded
 * `url` makes spring-cloud-openfeign bypass load balancing, which is how this
 * client used to point at http://localhost:8093 - the checkin container's own
 * loopback. Without `url` the target is lb://ticket-service.
 */
@FeignClient(
    name = "ticket-service",
    path = "/tickets",
    configuration = FeignClientConfig.class
)
public interface TicketServiceClient {

    @GetMapping("/event/{eventId}/all")
    List<TicketInfo> getEventTickets(@PathVariable String eventId);
    
    @GetMapping("/{ticketCode}")
    TicketInfo getTicketByCode(@PathVariable String ticketCode);
    
    @PatchMapping("/{ticketCode}/checkin")
    void markAsUsed(@PathVariable String ticketCode);
}
