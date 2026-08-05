package com.shopflow.consumer.dto;

import com.shopflow.common.enums.ShippingType;
import com.shopflow.consumer.entity.ProcessingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "Fulfilment record for an order")
public record FulfilmentResponseDto(

        @Schema(example = "8a7c2f10-3b1e-4c2a-9f5d-1e2b3c4d5e6f")
        String orderRef,

        ShippingType shippingType,

        @Schema(example = "WH-EXP-01")
        String warehouseCode,

        LocalDate slaDate,
        BigDecimal baseTotal,
        BigDecimal surcharge,
        BigDecimal customsDuty,
        BigDecimal finalTotal,
        ProcessingStatus processingStatus,
        String rejectionReason,
        String correlationId,
        Instant processedAt
) {
}