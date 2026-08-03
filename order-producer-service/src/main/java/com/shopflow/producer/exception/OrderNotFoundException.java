package com.shopflow.producer.exception;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String orderRef) {
        super("Order not found: " + orderRef);
    }
}