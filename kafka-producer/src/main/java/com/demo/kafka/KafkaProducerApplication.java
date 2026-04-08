package com.demo.kafka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication is a shortcut for three annotations:
//   @Configuration      - this class can define Spring beans
//   @EnableAutoConfiguration - Spring Boot auto-wires Kafka, web server, Jackson, etc.
//   @ComponentScan      - scans this package and sub-packages for @Component, @Service, etc.
@SpringBootApplication
public class KafkaProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaProducerApplication.class, args);
    }
}
