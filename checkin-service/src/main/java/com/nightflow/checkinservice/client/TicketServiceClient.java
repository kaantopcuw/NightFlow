package com.nightflow.checkinservice.client;

import com.nightflow.checkinservice.dto.TicketInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;

import java.util.List;

@FeignClient(name = "ticket-service", path = "/tickets")
public interface TicketServiceClient {

    @GetMapping("/event/{eventId}/all")
    List<TicketInfo> getEventTickets(@PathVariable String eventId);
    
    @GetMapping("/{ticketCode}")
    TicketInfo getTicketByCode(@PathVariable String ticketCode);
    
    @PatchMapping("/{ticketCode}/checkin")
    void markAsUsed(@PathVariable String ticketCode);
}
