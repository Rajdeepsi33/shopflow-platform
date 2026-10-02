package com.shopflow.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.shopflow.common.enums.ShippingType;
import com.shopflow.common.event.OrderCreatedEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderCreatedEventTest {

    private OrderCreatedEvent eventWith(List<OrderCreatedEvent.Item> items) {
        return new OrderCreatedEvent(
                "evt-1",
                "ORDER_CREATED",
                Instant.parse("2026-07-27T10:15:30Z"),
                1,
                "order-1",
                "jane.doe@example.com",
                ShippingType.EXPRESS,
                "DE",
                new BigDecimal("129.98"),
                items
        );
    }

    @Test
    void nullItemsBecomeAnEmptyList() {
        assertTrue(eventWith(null).items().isEmpty());
    }

    @Test
    void itemsAreCopiedSoLaterMutationDoesNotLeakIn() {
        List<OrderCreatedEvent.Item> mutable = new ArrayList<>();
        mutable.add(new OrderCreatedEvent.Item(
                1L, "Backpack", new BigDecimal("109.95"), 1, new BigDecimal("109.95")));

        OrderCreatedEvent event = eventWith(mutable);
        mutable.add(new OrderCreatedEvent.Item(
                2L, "Sneakers", new BigDecimal("19.99"), 1, new BigDecimal("19.99")));

        assertEquals(1, event.items().size());
    }

    @Test
    void theItemListItselfCannotBeModified() {
        OrderCreatedEvent event = eventWith(List.of(new OrderCreatedEvent.Item(
                1L, "Backpack", new BigDecimal("109.95"), 1, new BigDecimal("109.95"))));

        assertThrows(UnsupportedOperationException.class,
                () -> event.items().add(null));
    }

    @Test
    void carriesTheFieldsItWasBuiltWith() {
        OrderCreatedEvent event = eventWith(List.of());

        assertEquals("order-1", event.orderRef());
        assertEquals(ShippingType.EXPRESS, event.shippingType());
        assertEquals(new BigDecimal("129.98"), event.orderTotal());
        assertEquals(1, event.schemaVersion());
    }

    @Test
    void unknownFieldsAreIgnoredSoNewerProducersDoNotBreakOlderConsumers() throws Exception {
        String jsonFromNewerProducer = """
                {
                  "eventId": "evt-1",
                  "eventType": "ORDER_CREATED",
                  "occurredAt": "2026-07-27T10:15:30Z",
                  "schemaVersion": 1,
                  "orderRef": "order-1",
                  "customerEmail": "jane.doe@example.com",
                  "shippingType": "EXPRESS",
                  "destinationCountry": "DE",
                  "orderTotal": 129.98,
                  "items": [],
                  "promoCode": "SUMMER25"
                }
                """;

        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        OrderCreatedEvent event = mapper.readValue(jsonFromNewerProducer, OrderCreatedEvent.class);

        assertEquals("order-1", event.orderRef());
        assertEquals(ShippingType.EXPRESS, event.shippingType());
    }
}