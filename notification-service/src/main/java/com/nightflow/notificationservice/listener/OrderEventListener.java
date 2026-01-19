package com.nightflow.notificationservice.listener;

import com.nightflow.notificationservice.event.OrderCreatedEvent;
import com.nightflow.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final EmailService emailService;

    @KafkaListener(topics = "order-created", groupId = "notification-service")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Received order created event: {}", event.orderNumber());
        
        try {
            emailService.sendOrderConfirmation(event);
            log.info("Email sent successfully for order: {}", event.orderNumber());
        } catch (Exception e) {
            log.error("Failed to send email for order: {}", event.orderNumber(), e);
        }
    }
}
