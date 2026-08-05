package com.shopflow.consumer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Guards against duplicate processing.
 *
 * AMQP delivery is at-least-once: a consumer that crashes before acking
 * will see the message again. SETNX gives a cheap distributed check -
 * the first caller sets the key and proceeds, later callers find it
 * present and drop the message.
 *
 * This is the fast path only. The unique constraint on fulfilments.order_ref
 * is the correctness backstop if Redis is flushed or unavailable.
 */
@Slf4j
@Component
public class IdempotencyGuard {

    private static final String KEY_PREFIX = "processed::";

    private final StringRedisTemplate redisTemplate;
    private final Duration ttl;

    public IdempotencyGuard(StringRedisTemplate redisTemplate,
                            @Value("${shopflow.idempotency.ttl}") Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.ttl = ttl;
    }

    /**
     * @return true if this caller acquired the right to process, false if
     *         the order has already been handled.
     */
    public boolean tryAcquire(String orderRef) {
        try {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(KEY_PREFIX + orderRef, "1", ttl);
            return Boolean.TRUE.equals(acquired);

        } catch (Exception e) {
            // Redis down: fall through and let the database unique
            // constraint catch duplicates rather than blocking all work.
            log.warn("Idempotency check failed for {}, proceeding on DB constraint: {}",
                    orderRef, e.getMessage());
            return true;
        }
    }

    /**
     * Released on processing failure so a redelivery can retry. Without
     * this a transient error would permanently block the order.
     */
    public void release(String orderRef) {
        try {
            redisTemplate.delete(KEY_PREFIX + orderRef);
        } catch (Exception e) {
            log.warn("Failed to release idempotency key for {}: {}", orderRef, e.getMessage());
        }
    }
}