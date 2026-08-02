package com.shopflow.producer.dto;

import com.shopflow.common.enums.ShippingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

@Schema(description = "Request payload for creating an order")
public record OrderRequestDto(

        @NotBlank(message = "customerEmail is required")
        @Email(message = "customerEmail must be a valid email address")
        @Schema(example = "jane.doe@example.com")
        String customerEmail,

        @NotNull(message = "shippingType is required")
        @Schema(example = "EXPRESS")
        ShippingType shippingType,

        @NotBlank(message = "destinationCountry is required")
        @Pattern(regexp = "^[A-Z]{2}$",
                 message = "destinationCountry must be an uppercase ISO-3166 alpha-2 code")
        @Schema(example = "GB")
        String destinationCountry,

        @NotEmpty(message = "items must not be empty")
        @Size(max = 20, message = "items must not exceed 20 entries")
        @Valid
        List<OrderItemRequestDto> items
) {
}