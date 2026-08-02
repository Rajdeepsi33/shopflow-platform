package com.shopflow.producer.dto;

import com.shopflow.common.enums.ShippingType;
import com.shopflow.producer.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(description = "Order as returned by the API")
public record OrderResponseDto(

        @Schema(example = "8a7c2f10-3b1e-4c2a-9f5d-1e2b3c4d5e6f")
        String orderRef,

        String customerEmail,
        ShippingType shippingType,
        String destinationCountry,
        OrderStatus status,
        BigDecimal orderTotal,
        String correlationId,
        Instant createdAt,
        List<OrderItemResponseDto> items
) {
    
}