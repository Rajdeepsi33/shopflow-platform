package com.shopflow.producer.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "A line item on an order response")
public record OrderItemResponseDto(
        Long productId,
        String productTitle,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal lineTotal
) {
}