package com.shopflow.consumer.service;

import com.shopflow.common.enums.ShippingType;
import com.shopflow.common.event.OrderCreatedEvent;
import com.shopflow.consumer.config.ShopflowProperties;
import com.shopflow.consumer.entity.Fulfilment;
import com.shopflow.consumer.entity.ProcessingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;

/**
 * Next-day shipping. Adds a surcharge and flags the order in Redis for
 * the express picking dashboard.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public final class ExpressOrderProcessor implements OrderProcessor {

    private static final String WAREHOUSE = "WH-EXP-01";
    private static final String EXPRESS_KEY_PREFIX = "express::";
    private static final int SLA_DAYS = 1;
    private static final int MONEY_SCALE = 2;
    private static final Duration FLAG_TTL = Duration.ofSeconds(86400);

    private final ShopflowProperties properties;
    private final StringRedisTemplate redisTemplate;

    @Override
    public ShippingType supports() {
        return ShippingType.EXPRESS;
    }

    @Override
    public Fulfilment process(OrderCreatedEvent event) {

        BigDecimal base = event.orderTotal().setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal surcharge = base
                .multiply(properties.getExpress().getSurchargeRate())
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        flagForPriorityPicking(event.orderRef());

        return Fulfilment.builder()
                .orderRef(event.orderRef())
                .shippingType(ShippingType.EXPRESS)
                .warehouseCode(WAREHOUSE)
                .slaDate(LocalDate.now().plusDays(SLA_DAYS))
                .baseTotal(base)
                .surcharge(surcharge)
                .customsDuty(BigDecimal.ZERO.setScale(MONEY_SCALE))
                .finalTotal(base.add(surcharge).setScale(MONEY_SCALE, RoundingMode.HALF_UP))
                .processingStatus(ProcessingStatus.PROCESSED)
                .correlationId(event.orderRef())
                .build();
    }

    /**
     * Best effort. A missing dashboard flag must not fail the fulfilment.
     */
    private void flagForPriorityPicking(String orderRef) {
        try {
            redisTemplate.opsForValue().set(EXPRESS_KEY_PREFIX + orderRef, "1", FLAG_TTL);
        } catch (Exception e) {
            log.warn("Could not set express flag for {}: {}", orderRef, e.getMessage());
        }
    }
}