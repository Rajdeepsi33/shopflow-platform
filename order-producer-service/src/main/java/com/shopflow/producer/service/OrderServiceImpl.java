package com.shopflow.producer.service;

import com.shopflow.common.enums.ShippingType;
import com.shopflow.producer.dto.*;
import com.shopflow.producer.entity.Order;
import com.shopflow.producer.entity.OrderItem;
import com.shopflow.producer.entity.OrderStatus;
import com.shopflow.producer.exception.InvalidOrderStateException;
import com.shopflow.producer.exception.InvalidShippingDestinationException;
import com.shopflow.producer.exception.OrderNotFoundException;
import com.shopflow.producer.mapper.OrderMapper;
import com.shopflow.producer.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String GB = "GB";
    private static final int MONEY_SCALE = 2;

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto request) {

        assertShippingDestinationIsValid(request.shippingType(), request.destinationCountry());

       
        List<OrderItem> items = buildItems(request.items());

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

        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto findByRef(String orderRef) {
        Order order = orderRepository.findByOrderRefWithItems(orderRef)
                .orElseThrow(() -> new OrderNotFoundException(orderRef));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> search(OrderSearchCriteria criteria, Pageable pageable) {
        return orderRepository
                .search(criteria.status(), criteria.shippingType(), pageable)
                .map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponseDto cancel(String orderRef) {
        Order order = orderRepository.findByOrderRefWithItems(orderRef)
                .orElseThrow(() -> new OrderNotFoundException(orderRef));

        assertCancellable(order);
        order.setStatus(OrderStatus.CANCELLED);

        return orderMapper.toResponse(order);
    }

    /**
     * Runs in its own transaction: the publisher confirm arrives on a
     * Rabbit I/O thread, outside the request transaction, which has
     * already committed by then.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublished(String orderRef) {
        updateStatus(orderRef, OrderStatus.PUBLISHED);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markPublishFailed(String orderRef) {
        updateStatus(orderRef, OrderStatus.PUBLISH_FAILED);
    }

    // ── helpers ──────────────────────────────────────────────

    private void updateStatus(String orderRef, OrderStatus status) {
        orderRepository.findByOrderRef(orderRef).ifPresentOrElse(
                order -> {
                    order.setStatus(status);
                    log.debug("Order {} moved to {}", orderRef, status);
                },
                () -> log.warn("Cannot set status {} - order {} not found", status, orderRef));
    }

    private void assertShippingDestinationIsValid(ShippingType type, String country) {
        if (type == ShippingType.INTERNATIONAL && GB.equals(country)) {
            throw new InvalidShippingDestinationException(type, country);
        }
    }

    private void assertCancellable(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStateException(
                    order.getOrderRef(), order.getStatus(), OrderStatus.CANCELLED);
        }
    }

    private BigDecimal sumLineTotals(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Temporary stand-in until the catalogue client lands in slice 3.
     */
    private List<OrderItem> buildItems(List<OrderItemRequestDto> requested) {
        return requested.stream()
                .map(item -> {
                    ProductDto placeholder = new ProductDto(
                            item.productId(),
                            "Product " + item.productId(),
                            new BigDecimal("10.00"),
                            "placeholder",
                            null,
                            ProductDto.DataSource.CATALOG);
                    return orderMapper.toItem(placeholder, item.quantity());
                })
                .toList();
    }
}