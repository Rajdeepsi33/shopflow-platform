CREATE TABLE orders (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_ref           VARCHAR(36)   NOT NULL UNIQUE,
    customer_email      VARCHAR(255)  NOT NULL,
    shipping_type       VARCHAR(20)   NOT NULL,
    destination_country CHAR(2)       NOT NULL,
    status              VARCHAR(20)   NOT NULL,
    order_total         DECIMAL(12,2) NOT NULL,
    correlation_id      VARCHAR(36),
    created_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version             BIGINT        NOT NULL DEFAULT 0,
    INDEX idx_orders_status_type (status, shipping_type),
    INDEX idx_orders_email (customer_email)
) ENGINE=InnoDB;

CREATE TABLE order_items (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT        NOT NULL,
    product_id    BIGINT        NOT NULL,
    product_title VARCHAR(500)  NOT NULL,
    unit_price    DECIMAL(10,2) NOT NULL,
    quantity      INT           NOT NULL,
    line_total    DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_items_order (order_id)
) ENGINE=InnoDB;