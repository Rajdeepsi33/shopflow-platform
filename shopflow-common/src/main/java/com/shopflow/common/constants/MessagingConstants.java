package com.shopflow.common.constants;

/**
 * Single source of truth for the AMQP topology.
 *
 * Producer and consumer both reference these, so a rename cannot
 * leave the two sides disagreeing about a queue or routing key.
 */
public final class MessagingConstants {

    private MessagingConstants() {
        // holder class, never instantiated
    }

    // ── exchanges ────────────────────────────────────────────
    public static final String ORDER_EXCHANGE = "x.order.direct";
    public static final String ORDER_DLX = "x.order.dlx";

    // ── routing keys ─────────────────────────────────────────
    public static final String ROUTING_KEY_STANDARD = "order.standard";
    public static final String ROUTING_KEY_EXPRESS = "order.express";
    public static final String ROUTING_KEY_INTERNATIONAL = "order.international";
    public static final String ROUTING_KEY_DEAD = "order.dead";

    // ── queues ───────────────────────────────────────────────
    public static final String QUEUE_STANDARD = "q.order.standard";
    public static final String QUEUE_EXPRESS = "q.order.express";
    public static final String QUEUE_INTERNATIONAL = "q.order.international";
    public static final String QUEUE_DLQ = "q.order.dlq";

    // ── queue declaration arguments ──────────────────────────
    public static final String ARG_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    public static final String ARG_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    // ── AMQP headers ─────────────────────────────────────────
    public static final String HEADER_ORDER_ID = "x-order-id";
    public static final String HEADER_SHIPPING_TYPE = "x-shipping-type";
    public static final String HEADER_CORRELATION_ID = "x-correlation-id";
    public static final String HEADER_SCHEMA_VERSION = "x-schema-version";

    // ── event metadata ───────────────────────────────────────
    public static final String EVENT_TYPE_ORDER_CREATED = "ORDER_CREATED";
    public static final int SCHEMA_VERSION = 1;
}