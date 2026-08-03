package com.shopflow.producer.entity;

import com.shopflow.common.enums.ShippingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_ref", nullable = false, unique = true, length = 36)
    private String orderRef;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_type", nullable = false, length = 20)
    private ShippingType shippingType;

    @Column(name = "destination_country", nullable = false, length = 2, columnDefinition = "CHAR(2)")
    private String destinationCountry;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "order_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal orderTotal;

    @Column(name = "correlation_id", length = 36)
    private String correlationId;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private Instant updatedAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Keeps both sides of the relationship consistent. Setting only one
     * side leaves the foreign key null on insert.
     */
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}