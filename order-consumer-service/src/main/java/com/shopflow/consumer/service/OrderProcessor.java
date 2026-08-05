package com.shopflow.consumer.service;

import com.shopflow.common.enums.ShippingType;
import com.shopflow.common.event.OrderCreatedEvent;
import com.shopflow.consumer.entity.Fulfilment;

/**
 * Sealed so the set of processors is closed and known at compile time.
 * Adding a shipping type forces a matching processor rather than
 * silently falling through to a default.
 */
public sealed interface OrderProcessor
        permits StandardOrderProcessor, ExpressOrderProcessor, InternationalOrderProcessor {

    ShippingType supports();

    Fulfilment process(OrderCreatedEvent event);
}