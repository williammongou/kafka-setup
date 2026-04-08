package com.demo.kafka.controller;

import com.demo.kafka.model.OrderEvent;
import com.demo.kafka.producer.OrderEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class EventController {

    private final OrderEventProducer producer;

    // POST /orders
    // Body: { "customerId": "cust-123", "status": "CREATED", "amount": 49.99 }
    // Publishes an OrderEvent to Kafka and returns the generated orderId
    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody OrderRequest request) {
        String orderId = UUID.randomUUID().toString();

        OrderEvent event = OrderEvent.of(
                orderId,
                request.customerId(),
                request.status(),
                request.amount()
        );

        producer.sendOrderEvent(event);

        log.info("Accepted order request → orderId={}", orderId);
        return ResponseEntity.accepted().body("Order accepted: " + orderId);
    }

    // Simple request record — Jackson deserializes the JSON body into this automatically
    // Using a Java record keeps it concise (no need for a separate DTO class file)
    public record OrderRequest(String customerId, String status, double amount) {}
}
