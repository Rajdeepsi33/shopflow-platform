package com.shopflow.producer.exception;

import com.shopflow.producer.entity.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {

    public InvalidOrderStateException(String orderRef, OrderStatus from, OrderStatus to) {
        super("Cannot transition order " + orderRef + " from " + from + " to " + to);
    }
}