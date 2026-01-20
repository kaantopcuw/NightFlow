package com.nightflow.ticketservice.client;

import com.nightflow.ticketservice.config.FeignClientConfig;
import com.nightflow.ticketservice.dto.EventResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "event-catalog-client", 
    url = "http://localhost:8092",
    configuration = FeignClientConfig.class
)
public interface EventServiceClient {

    @GetMapping("/events/{id}")
    EventResponse getEvent(@PathVariable("id") String id);
}




