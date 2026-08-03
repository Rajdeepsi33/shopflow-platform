package com.shopflow.producer.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long productId) {
        super("Product not found in catalogue: " + productId);
    }
}