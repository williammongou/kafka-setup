package com.demo.kafka.config;

import com.demo.kafka.model.OrderEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    // Pulled from application.properties — makes it easy to swap localhost for your EC2 address
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ProducerFactory builds the underlying Kafka producer client.
    // Think of it as the "connection pool" for producers.
    @Bean
    public ProducerFactory<String, OrderEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Where to find the Kafka broker(s)
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // The message KEY is a plain String (we'll use orderId as the key).
        // Kafka uses the key for partitioning — same key always goes to same partition,
        // which guarantees ordering for a given order.
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // The message VALUE is our OrderEvent object serialized to JSON.
        // JsonSerializer (from spring-kafka) handles this automatically.
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // By default JsonSerializer adds a type header to the message so the consumer
        // knows what class to deserialize into. We keep this on for now.
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, true);

        return new DefaultKafkaProducerFactory<>(config);
    }

    // KafkaTemplate is the main Spring API for sending messages.
    // It wraps ProducerFactory and gives us a clean send() method.
    @Bean
    public KafkaTemplate<String, OrderEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
