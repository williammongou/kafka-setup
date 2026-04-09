package com.demo.kafka.consumer;

import com.demo.kafka.model.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

// @Slf4j generates a `log` field — no need to write Logger log = LoggerFactory.getLogger(...)
@Slf4j
@Service
public class OrderEventConsumer {

    // @KafkaListener tells Spring to call this method when messages arrive
    // It polls the topic at the rate configured in KafkaConsumerConfig
    //
    // How it works:
    // 1. Spring creates a background thread that continuously polls Kafka
    // 2. The poll interval is set to 3000ms (3 seconds) in application.properties
    // 3. Each poll fetches up to 10 records (max-poll-records=10)
    // 4. This method is called ONCE for EACH message in the batch
    // 5. After all messages are processed, offsets are committed (AckMode.BATCH)
    //
    // Parameters:
    // - topics: which Kafka topic(s) to consume from
    // - groupId: consumer group (defaults to application.properties value)
    // - containerFactory: which factory to use (our custom configured one)
    @KafkaListener(
            topics = "${kafka.topic.orders}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderEvent(
            @Payload OrderEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_KEY) String key
    ) {
        log.info("════════════════════════════════════════════════════════════");
        log.info("📦 Consumed OrderEvent:");
        log.info("   Topic: {}", topic);
        log.info("   Partition: {}", partition);
        log.info("   Offset: {}", offset);
        log.info("   Key: {}", key);
        log.info("   ---");
        log.info("   Event ID: {}", event.getEventId());
        log.info("   Order ID: {}", event.getOrderId());
        log.info("   Customer ID: {}", event.getCustomerId());
        log.info("   Status: {}", event.getStatus());
        log.info("   Amount: ${}", event.getAmount());
        log.info("   Timestamp: {}", event.getTimestamp());
        log.info("════════════════════════════════════════════════════════════");

        // TODO: Add your business logic here
        // Examples:
        // - Update order status in database
        // - Send email notification
        // - Trigger downstream workflows
        // - Calculate analytics/metrics
        // - Call external APIs

        // Simulate processing time (remove this in production)
        try {
            Thread.sleep(500); // Simulate 500ms of processing work
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Processing interrupted", e);
        }
    }

    // Alternative method: consume the entire ConsumerRecord for full metadata access
    // Uncomment this if you need access to headers, timestamps, or other metadata
    /*
    @KafkaListener(
            topics = "${kafka.topic.orders}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeOrderEventWithFullRecord(ConsumerRecord<String, OrderEvent> record) {
        OrderEvent event = record.value();

        log.info("Consumed message:");
        log.info("  Key: {}", record.key());
        log.info("  Partition: {}", record.partition());
        log.info("  Offset: {}", record.offset());
        log.info("  Timestamp: {}", record.timestamp());
        log.info("  Event: {}", event);

        // Process event...
    }
    */
}
