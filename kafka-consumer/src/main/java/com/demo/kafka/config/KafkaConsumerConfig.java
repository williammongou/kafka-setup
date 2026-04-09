package com.demo.kafka.config;

import com.demo.kafka.model.OrderEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

// @EnableKafka turns on Spring's Kafka listener infrastructure
// @Configuration tells Spring this class contains bean definitions
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${kafka.consumer.poll-interval-ms}")
    private Long pollIntervalMs;

    @Value("${spring.kafka.consumer.max-poll-records}")
    private Integer maxPollRecords;

    // ConsumerFactory creates Kafka consumer instances
    // It's configured with all the connection and deserialization settings
    @Bean
    public ConsumerFactory<String, OrderEvent> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Kafka broker address
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Consumer group ID - consumers in the same group share the workload
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Where to start reading if no offset exists: earliest or latest
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        // Deserializer for message keys (String in our case)
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Deserializer for message values (OrderEvent JSON)
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Maximum records returned in a single poll() call
        // Lower value = more frequent processing with smaller batches
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);

        // Trust all packages for JSON deserialization (be careful in production)
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        // Tell Jackson to deserialize into OrderEvent.class
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, OrderEvent.class.getName());

        return new DefaultKafkaConsumerFactory<>(
                config,
                new StringDeserializer(),
                new JsonDeserializer<>(OrderEvent.class, false)
        );
    }

    // KafkaListenerContainerFactory manages the lifecycle of @KafkaListener methods
    // This is where we configure the custom polling behavior
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // Get container properties to configure polling behavior
        ContainerProperties containerProps = factory.getContainerProperties();

        // AckMode.BATCH: commits offsets after the entire batch is processed
        // Other options: MANUAL, MANUAL_IMMEDIATE, RECORD, TIME, COUNT, COUNT_TIME
        containerProps.setAckMode(ContainerProperties.AckMode.BATCH);

        // Poll timeout: how long to wait for new records before returning
        // This controls the polling rate - consumer checks every <pollIntervalMs> milliseconds
        containerProps.setPollTimeout(pollIntervalMs);

        return factory;
    }
}
