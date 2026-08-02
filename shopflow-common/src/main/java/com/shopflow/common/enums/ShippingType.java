package com.shopflow.common.enums;

import com.shopflow.common.constants.MessagingConstants;

/**
 * Shipping type, and the routing key each one publishes under.
 *
 * Holding the key on the enum means routing is resolved by asking the
 * value itself. Adding a fourth shipping type forces you to supply its
 * routing key here rather than remembering to update a switch elsewhere.
 */
public enum ShippingType {

    STANDARD(MessagingConstants.ROUTING_KEY_STANDARD),
    EXPRESS(MessagingConstants.ROUTING_KEY_EXPRESS),
    INTERNATIONAL(MessagingConstants.ROUTING_KEY_INTERNATIONAL);

    private final String routingKey;

    ShippingType(String routingKey) {
        this.routingKey = routingKey;
    }

    public String routingKey() {
        return routingKey;
    }
}