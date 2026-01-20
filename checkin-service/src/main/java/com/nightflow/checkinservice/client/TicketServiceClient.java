package com.nightflow.checkinservice.client;

import com.nightflow.checkinservice.config.FeignClientConfig;
import com.nightflow.checkinservice.dto.TicketInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;

@FeignClient(
    name = "ticket-service-client", 
    url = "http://localhost:8093",
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
