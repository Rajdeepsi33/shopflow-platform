package com.shopflow.producer.exception;

import com.shopflow.common.enums.ShippingType;

public class InvalidShippingDestinationException extends RuntimeException {

    public InvalidShippingDestinationException(ShippingType shippingType, String country) {
        super("Shipping type " + shippingType + " is not valid for destination " + country);
    }
}