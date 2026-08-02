package com.shopflow.producer.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "A single line item on an order request")
public record OrderItemRequestDto(

        @NotNull(message = "productId is required")
        @Schema(description = "Catalogue product id", example = "1")
        Long productId,

        @NotNull(message = "quantity is required")
        @Min(value = 1, message = "quantity must be at least 1")
        @Max(value = 100, message = "quantity must not exceed 100")
        @Schema(description = "Units ordered", example = "2")
        Integer quantity
) {
}