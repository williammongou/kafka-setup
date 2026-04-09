package com.demo.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication is a shortcut for three annotations:
//   @Configuration      - this class can define Spring beans
//   @EnableAutoConfiguration - Spring Boot auto-wires Kafka, Jackson, etc.
//   @ComponentScan      - scans this package and sub-packages for @Component, @Service, etc.
@SpringBootApplication
public class KafkaConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaConsumerApplication.class, args);
        // Once started, the @KafkaListener in OrderEventConsumer automatically begins polling
    }
}
