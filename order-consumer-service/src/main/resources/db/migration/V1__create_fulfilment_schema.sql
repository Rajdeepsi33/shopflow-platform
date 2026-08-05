CREATE TABLE fulfilments (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_ref         VARCHAR(36)   NOT NULL UNIQUE,   -- idempotency backstop
    shipping_type     VARCHAR(20)   NOT NULL,
    warehouse_code    VARCHAR(20)   NOT NULL,
    sla_date          DATE          NOT NULL,
    base_total        DECIMAL(12,2) NOT NULL,
    surcharge         DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    customs_duty      DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    final_total       DECIMAL(12,2) NOT NULL,
    processing_status VARCHAR(20)   NOT NULL,          -- PROCESSED | REJECTED
    rejection_reason  VARCHAR(255),
    correlation_id    VARCHAR(36),
    processed_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_fulfil_warehouse (warehouse_code),
    INDEX idx_fulfil_sla (sla_date)
) ENGINE=InnoDB;