package com.shopflow.consumer.entity;

import com.shopflow.common.enums.ShippingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "fulfilments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Fulfilment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique. This is the idempotency backstop: if Redis is flushed and a
     * duplicate message gets past the guard, the database refuses it.
     */
    @Column(name = "order_ref", nullable = false, unique = true, length = 36)
    private String orderRef;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_type", nullable = false, length = 20)
    private ShippingType shippingType;

    @Column(name = "warehouse_code", nullable = false, length = 20)
    private String warehouseCode;

    @Column(name = "sla_date", nullable = false)
    private LocalDate slaDate;

    @Column(name = "base_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseTotal;

    @Column(name = "surcharge", nullable = false, precision = 12, scale = 2)
    private BigDecimal surcharge;

    @Column(name = "customs_duty", nullable = false, precision = 12, scale = 2)
    private BigDecimal customsDuty;

    @Column(name = "final_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal finalTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false, length = 20)
    private ProcessingStatus processingStatus;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "correlation_id", length = 36)
    private String correlationId;

    @Column(name = "processed_at", nullable = false, insertable = false, updatable = false)
    private Instant processedAt;
}