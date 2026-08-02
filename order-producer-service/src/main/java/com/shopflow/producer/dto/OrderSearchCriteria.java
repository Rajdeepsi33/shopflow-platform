package com.shopflow.producer.dto;

import com.shopflow.common.enums.ShippingType;
import com.shopflow.producer.entity.OrderStatus;

public record OrderSearchCriteria(
        OrderStatus status,
        ShippingType shippingType
) {
}