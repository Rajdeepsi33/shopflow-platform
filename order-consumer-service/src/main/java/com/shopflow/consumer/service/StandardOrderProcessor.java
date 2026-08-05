package com.shopflow.consumer.service;

import com.shopflow.common.enums.ShippingType;
import com.shopflow.common.event.OrderCreatedEvent;
import com.shopflow.consumer.entity.Fulfilment;
import com.shopflow.consumer.entity.ProcessingStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Domestic ground shipping. No surcharge, no duty, five day SLA.
 */
@Slf4j
@Service
public final class StandardOrderProcessor implements OrderProcessor {

    private static final String WAREHOUSE = "WH-DOM-01";
    private static final int SLA_DAYS = 5;
    private static final int MONEY_SCALE = 2;

    @Override
    public ShippingType supports() {
        return ShippingType.STANDARD;
    }

    @Override
    public Fulfilment process(OrderCreatedEvent event) {

        BigDecimal base = event.orderTotal().setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        log.debug("Processing standard order {}", event.orderRef());

        return Fulfilment.builder()
                .orderRef(event.orderRef())
                .shippingType(ShippingType.STANDARD)
                .warehouseCode(WAREHOUSE)
                .slaDate(LocalDate.now().plusDays(SLA_DAYS))
                .baseTotal(base)
                .surcharge(BigDecimal.ZERO.setScale(MONEY_SCALE))
                .customsDuty(BigDecimal.ZERO.setScale(MONEY_SCALE))
                .finalTotal(base)
                .processingStatus(ProcessingStatus.PROCESSED)
                .correlationId(event.orderRef())
                .build();
    }
}