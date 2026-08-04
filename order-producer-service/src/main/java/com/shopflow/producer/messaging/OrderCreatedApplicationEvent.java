package com.shopflow.producer.messaging;

public record OrderCreatedApplicationEvent(String orderRef) {
}