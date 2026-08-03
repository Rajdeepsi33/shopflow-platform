package com.shopflow.producer.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.producer.dto.ProductDto;
import com.shopflow.producer.exception.CatalogUnavailableException;
import com.shopflow.producer.exception.ProductNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Slf4j
@Service
public class ProductCatalogClientImpl implements ProductCatalogClient {

    private static final String CACHE_KEY_PREFIX = "product::";
    private static final String INSTANCE = "productCatalog";

    private final RestClient catalogRestClient;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration productTtl;

    public ProductCatalogClientImpl(RestClient catalogRestClient,
                                    StringRedisTemplate redisTemplate,
                                    ObjectMapper objectMapper,
                                    @Value("${shopflow.cache.product-ttl}") Duration productTtl) {
        this.catalogRestClient = catalogRestClient;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.productTtl = productTtl;
    }

    /**
     * Read-through cache, then HTTP.
     *
     * The resilience annotations wrap the whole method, so a cache hit
     * is not recorded as a circuit breaker call - only real HTTP work is.
     */
    @Override
    @CircuitBreaker(name = INSTANCE, fallbackMethod = "fetchProductFallback")
    @Retry(name = INSTANCE)
    public ProductDto fetchProduct(Long productId) {

        ProductDto cached = readFromCache(productId);
        if (cached != null) {
            log.debug("Cache hit for product {}", productId);
            return cached.withDataSource(ProductDto.DataSource.CACHE);
        }

        ProductDto fetched = callCatalog(productId);
        writeToCache(productId, fetched);
        return fetched.withDataSource(ProductDto.DataSource.CATALOG);
    }

    /**
     * Invoked when the breaker is open, or when retries are exhausted.
     *
     * Reads Redis directly rather than through @Cacheable: once the
     * breaker short-circuits the call, a caching proxy is never
     * consulted, so the stale entry has to be fetched by hand.
     */
    @SuppressWarnings("unused")
    private ProductDto fetchProductFallback(Long productId, Throwable t) {

        if (t instanceof ProductNotFoundException pnfe) {
            throw pnfe;   // a 404 is a real answer, not something to degrade around
        }

        log.warn("Catalogue call failed for product {} ({}), attempting stale cache",
                productId, t.getClass().getSimpleName());

        ProductDto stale = readFromCache(productId);
        if (stale != null) {
            log.info("Serving stale cached data for product {}", productId);
            return stale.withDataSource(ProductDto.DataSource.CACHE_FALLBACK);
        }

        throw new CatalogUnavailableException(productId, t);
    }

    // ── helpers ──────────────────────────────────────────────

    private ProductDto callCatalog(Long productId) {
        try {
            ProductDto product = catalogRestClient.get()
                    .uri("/products/{id}", productId)
                    .retrieve()
                    .body(ProductDto.class);

            if (product == null || product.id() == null) {
                throw new ProductNotFoundException(productId);
            }
            return product;

        } catch (HttpClientErrorException.NotFound e) {
            throw new ProductNotFoundException(productId);
        }
    }

    /**
     * Cache reads and writes are best effort. Redis being down must
     * degrade to a direct HTTP call, never fail the request.
     */
    private ProductDto readFromCache(Long productId) {
        try {
            String json = redisTemplate.opsForValue().get(CACHE_KEY_PREFIX + productId);
            return json == null ? null : objectMapper.readValue(json, ProductDto.class);
        } catch (Exception e) {
            log.warn("Redis read failed for product {}: {}", productId, e.getMessage());
            return null;
        }
    }

    private void writeToCache(Long productId, ProductDto product) {
        try {
            String json = objectMapper.writeValueAsString(product);
            redisTemplate.opsForValue().set(CACHE_KEY_PREFIX + productId, json, productTtl);
        } catch (Exception e) {
            log.warn("Redis write failed for product {}: {}", productId, e.getMessage());
        }
    }
}