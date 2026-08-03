package com.shopflow.producer.repository;

import com.shopflow.common.enums.ShippingType;
import com.shopflow.producer.entity.Order;
import com.shopflow.producer.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderRef(String orderRef);

    /**
     * Loads the order with its items in one query. Without the fetch join
     * the lazy item list triggers a second query, or fails outside the
     * transaction.
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderRef = :orderRef")
    Optional<Order> findByOrderRefWithItems(String orderRef);

    /**
     * Both filters are optional: a null argument disables that condition.
     */
    @Query("""
            SELECT o FROM Order o
            WHERE (:status IS NULL OR o.status = :status)
              AND (:shippingType IS NULL OR o.shippingType = :shippingType)
            """)
    Page<Order> search(OrderStatus status, ShippingType shippingType, Pageable pageable);

    boolean existsByOrderRef(String orderRef);
}