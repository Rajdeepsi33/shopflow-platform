package com.shopflow.consumer.exception;

public class FulfilmentNotFoundException extends RuntimeException {

    public FulfilmentNotFoundException(String orderRef) {
        super("Fulfilment not found for order: " + orderRef);
    }
}