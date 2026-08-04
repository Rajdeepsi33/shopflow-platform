package com.shopflow.producer.service;

import com.shopflow.producer.dto.OrderRequestDto;
import com.shopflow.producer.entity.Order;
import com.shopflow.producer.entity.OrderItem;
import com.shopflow.producer.entity.OrderStatus;
import com.shopflow.producer.messaging.OrderCreatedApplicationEvent;
import com.shopflow.producer.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Holds the transactional write for order creation.
 *
 * This is a separate bean on purpose. Spring applies @Transactional
 * through a proxy, and a method called from inside the same class
 * bypasses that proxy - the annotation would silently do nothing.
 * Keeping the transactional step in its own bean means the call goes
 * through the proxy and the transaction actually starts.
 *
 * The transaction therefore covers only steps 5-7 of the flow: map,
 * save, commit. No external HTTP happens inside it.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPersistenceService {

    private static final int MONEY_SCALE = 2;

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Order persist(OrderRequestDto request, List<OrderItem> items) {

        Order order = Order.builder()
                .orderRef(UUID.randomUUID().toString())
                .customerEmail(request.customerEmail())
                .shippingType(request.shippingType())
                .destinationCountry(request.destinationCountry())
                .status(OrderStatus.CREATED)
                .orderTotal(sumLineTotals(items))
                .correlationId(UUID.randomUUID().toString())
                .items(new ArrayList<>())
                .build();

        items.forEach(order::addItem);

        Order saved = orderRepository.save(order);
        log.info("Created order {} with {} items", saved.getOrderRef(), items.size());

        // Spring holds this until the transaction commits, then hands it
        // to OrderEventPublisher. If the transaction rolls back the
        // listener never fires, so no event is published for an order
        // that does not exist.
        applicationEventPublisher.publishEvent(
                new OrderCreatedApplicationEvent(saved.getOrderRef()));

        return saved;
    }

    private BigDecimal sumLineTotals(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}