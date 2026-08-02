package com.shopflow.common.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.shopflow.common.enums.ShippingType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * The ORDER_CREATED message contract.
 *
 * Adding a field is backwards compatible; renaming or removing one is not.
 * ignoreUnknown lets an older consumer accept a newer producer's payload.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(
        String eventId,
        String eventType,
        Instant occurredAt,
        int schemaVersion,
        String orderRef,
        String customerEmail,
        ShippingType shippingType,
        String destinationCountry,
        BigDecimal orderTotal,
        List<Item> items
) {

    /**
     * Records are only shallowly immutable - the list reference is final
     * but the list itself would still be mutable. Copying on construction
     * closes that hole.
     */
    public OrderCreatedEvent {
        items = (items == null) ? List.of() : List.copyOf(items);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Item(
            Long productId,
            String productTitle,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal lineTotal
    ) {
    }
}