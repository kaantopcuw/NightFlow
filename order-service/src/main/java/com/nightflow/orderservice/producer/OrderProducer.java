package com.nightflow.orderservice.producer;

import com.nightflow.orderservice.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "order-created";

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("Sending order created event to Kafka: {}", event);
        kafkaTemplate.send(TOPIC, event.orderNumber(), event);
    }
}
