package com.shopflow.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the order-producer-service (port 8081).
 *
 * The synchronous edge: validates orders, enriches them with catalogue
 * data, persists them, and publishes an event after the transaction commits.
 */
@SpringBootApplication
public class OrderProducerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderProducerApplication.class, args);
    }
}