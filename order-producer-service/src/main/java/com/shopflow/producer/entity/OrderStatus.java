package com.shopflow.producer.entity;

/** 
 * Order lifecycle. Transitions are enforced in the service layer;
 * an illegal one throws InvalidOrderStateException and maps to 409.
 */

public enum OrderStatus {
    CREATED,
    PUBLISHED,
    PUBLISH_FAILED,
    CANCELLED
}