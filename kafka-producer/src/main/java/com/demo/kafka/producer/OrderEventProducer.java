package com.demo.kafka.producer;

import com.demo.kafka.model.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

// @Slf4j generates a `log` field — no need to write Logger log = LoggerFactory.getLogger(...)
// @RequiredArgsConstructor generates a constructor for all `final` fields (used by Spring for injection)
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventProducer {

    // The topic name comes from application.properties so we can change it without recompiling
    @Value("${kafka.topic.orders}")
    private String ordersTopic;

    // Spring injects the KafkaTemplate bean we defined in KafkaProducerConfig
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void sendOrderEvent(OrderEvent event) {
        // send(topic, key, value)
        //
        // KEY  = orderId  → all events for the same order land on the same partition
        //                    this guarantees they're consumed in the order they were produced
        //
        // VALUE = the OrderEvent object → JsonSerializer turns it into a JSON byte array
        CompletableFuture<SendResult<String, OrderEvent>> future =
                kafkaTemplate.send(ordersTopic, event.getOrderId(), event);

        // The send is async. We attach callbacks to log success or failure.
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to send event [{}] for order [{}]: {}",
                        event.getEventId(), event.getOrderId(), ex.getMessage());
            } else {
                // RecordMetadata tells us exactly where in Kafka the message landed
                log.info("Sent event [{}] for order [{}] → topic={} partition={} offset={}",
                        event.getEventId(),
                        event.getOrderId(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
