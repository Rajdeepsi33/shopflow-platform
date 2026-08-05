package com.shopflow.consumer.service;

import com.shopflow.common.enums.ShippingType;
import com.shopflow.common.event.OrderCreatedEvent;
import com.shopflow.consumer.config.ShopflowProperties;
import com.shopflow.consumer.entity.Fulfilment;
import com.shopflow.consumer.entity.ProcessingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Cross-border shipping. Applies customs duty outside the EU and
 * rejects embargoed destinations outright.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public final class InternationalOrderProcessor implements OrderProcessor {

    private static final String WAREHOUSE = "WH-INT-01";
    private static final int SLA_DAYS = 10;
    private static final int MONEY_SCALE = 2;

    private final ShopflowProperties properties;

    @Override
    public ShippingType supports() {
        return ShippingType.INTERNATIONAL;
    }

    @Override
    public Fulfilment process(OrderCreatedEvent event) {

        BigDecimal base = event.orderTotal().setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        String country = event.destinationCountry();

        if (isEmbargoed(country)) {
            log.warn("Rejecting order {} - embargoed destination {}", event.orderRef(), country);
            return rejected(event, base, country);
        }

        BigDecimal duty = isEuropeanUnion(country)
                ? BigDecimal.ZERO.setScale(MONEY_SCALE)
                : base.multiply(properties.getCustoms().getDutyRate())
                      .setScale(MONEY_SCALE, RoundingMode.HALF_UP);

        return Fulfilment.builder()
                .orderRef(event.orderRef())
                .shippingType(ShippingType.INTERNATIONAL)
                .warehouseCode(WAREHOUSE)
                .slaDate(LocalDate.now().plusDays(SLA_DAYS))
                .baseTotal(base)
                .surcharge(BigDecimal.ZERO.setScale(MONEY_SCALE))
                .customsDuty(duty)
                .finalTotal(base.add(duty).setScale(MONEY_SCALE, RoundingMode.HALF_UP))
                .processingStatus(ProcessingStatus.PROCESSED)
                .correlationId(event.orderRef())
                .build();
    }

    private Fulfilment rejected(OrderCreatedEvent event, BigDecimal base, String country) {
        return Fulfilment.builder()
                .orderRef(event.orderRef())
                .shippingType(ShippingType.INTERNATIONAL)
                .warehouseCode(WAREHOUSE)
                .slaDate(LocalDate.now().plusDays(SLA_DAYS))
                .baseTotal(base)
                .surcharge(BigDecimal.ZERO.setScale(MONEY_SCALE))
                .customsDuty(BigDecimal.ZERO.setScale(MONEY_SCALE))
                .finalTotal(base)
                .processingStatus(ProcessingStatus.REJECTED)
                .rejectionReason("Embargoed destination: " + country)
                .correlationId(event.orderRef())
                .build();
    }

    private boolean isEmbargoed(String country) {
        return properties.getCustoms().getEmbargoed().contains(country);
    }

    private boolean isEuropeanUnion(String country) {
        return properties.getCustoms().getEuCountries().contains(country);
    }
}