package com.shopflow.producer.exception;

/**
 * Error codes from section 3.11. Each maps to one HTTP status,
 * surfaced in the ProblemDetail body as "code".
 */
public enum ErrorCode {
    VALIDATION_FAILED,
    ORDER_NOT_FOUND,
    PRODUCT_NOT_FOUND,
    INVALID_STATE_TRANSITION,
    INVALID_SHIPPING_DESTINATION,
    CATALOG_UNAVAILABLE,
    INTERNAL_ERROR
}