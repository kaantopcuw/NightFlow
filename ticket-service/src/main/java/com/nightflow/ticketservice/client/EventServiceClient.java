package com.nightflow.ticketservice.client;

import com.nightflow.ticketservice.config.FeignClientConfig;
import com.nightflow.ticketservice.dto.EventResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Calls event-catalog-service through Eureka.
 *
 * `name` is the Eureka service id and `url` is deliberately absent: in
 * spring-cloud-openfeign a hard-coded `url` bypasses load balancing entirely
 * (FeignClientFactoryBean unwraps FeignBlockingLoadBalancerClient), which is how
 * this client used to end up pointing at http://localhost:8092 - a loopback
 * address that resolves to the ticket-service container itself. With no `url`,
 * the target becomes lb://event-catalog-service and Spring Cloud LoadBalancer
 * resolves it from the registry.
 */
@FeignClient(
    name = "event-catalog-service",
    configuration = FeignClientConfig.class
)
public interface EventServiceClient {

    @GetMapping("/events/{id}")
    EventResponse getEvent(@PathVariable("id") String id);
}




