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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final String GB = "GB";

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductCatalogClient productCatalogClient;
    private final OrderPersistenceService orderPersistenceService;

    /**
     * Deliberately NOT @Transactional.
     *
     * The catalogue call is external HTTP and can take seconds when the
     * upstream is slow. Holding a pooled database connection across it
     * would exhaust the pool under load, so the enrichment happens here
     * and only the write is transactional, inside OrderPersistenceService.
     */
    @Override
    public OrderResponseDto createOrder(OrderRequestDto request) {

        assertShippingDestinationIsValid(request.shippingType(), request.destinationCountry());

        List<OrderItem> items = buildItems(request.items());

        Order saved = orderPersistenceService.persist(request, items);

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
     * Rabbit I/O thread, after the request transaction has committed,
     * so there is no transaction to join.
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

    /**
     * Prices and titles always come from the catalogue, never from the
     * client, so a caller cannot declare their own price.
     */
    private List<OrderItem> buildItems(List<OrderItemRequestDto> requested) {
        return requested.stream()
                .map(item -> {
                    ProductDto product = productCatalogClient.fetchProduct(item.productId());
                    return orderMapper.toItem(product, item.quantity());
                })
                .toList();
    }

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
}