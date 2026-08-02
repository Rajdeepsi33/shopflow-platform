package com.shopflow.common;

import com.shopflow.common.enums.ShippingType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShippingTypeTest {

    @ParameterizedTest
    @CsvSource({
            "STANDARD,      order.standard",
            "EXPRESS,       order.express",
            "INTERNATIONAL, order.international"
    })
    void eachTypeResolvesToItsOwnRoutingKey(ShippingType type, String expectedKey) {
        assertEquals(expectedKey, type.routingKey());
    }

    @Test
    void hasExactlyThreeShippingTypes() {
        assertEquals(3, ShippingType.values().length);
    }
}