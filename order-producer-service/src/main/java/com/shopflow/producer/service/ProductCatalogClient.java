package com.shopflow.producer.service;

import com.shopflow.producer.dto.ProductDto;

public interface ProductCatalogClient {

    /**
     * Fetches a product, guarded by time limiter, retry and circuit breaker.
     */
    ProductDto fetchProduct(Long productId);
}