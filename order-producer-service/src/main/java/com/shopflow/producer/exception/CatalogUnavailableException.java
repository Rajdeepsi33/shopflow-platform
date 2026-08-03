package com.shopflow.producer.exception;

/**
 * Thrown when the breaker is open and there is no cached copy to fall
 * back to. Maps to 503 CATALOG_UNAVAILABLE.
 */
public class CatalogUnavailableException extends RuntimeException {

    public CatalogUnavailableException(Long productId, Throwable cause) {
        super("Catalogue unavailable and no cached data for product " + productId, cause);
    }
}