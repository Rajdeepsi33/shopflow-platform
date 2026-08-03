package com.shopflow.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the order-consumer-service (port 8082).
 *
 * Holds three independent RabbitMQ listeners, one per shipping type,
 * each with its own concurrency profile and business rules.
 */
@SpringBootApplication
public class OrderConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderConsumerApplication.class, args);
    }
}