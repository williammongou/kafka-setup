package com.demo.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

// @Data generates: getters, setters, equals(), hashCode(), toString()
// @Builder gives us: OrderEvent.builder().orderId(...).build()
// @NoArgsConstructor / @AllArgsConstructor are needed for Jackson deserialization
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {

    // A unique ID for this event — each Kafka message should be uniquely identifiable
    private String eventId;

    // The business ID of the order this event is about
    private String orderId;

    // Who placed the order
    private String customerId;

    // What happened: CREATED, UPDATED, CANCELLED, SHIPPED, etc.
    private String status;

    // Dollar amount of the order
    private double amount;

    // When the event was created (ISO-8601 string when serialized to JSON)
    private Instant timestamp;
}
