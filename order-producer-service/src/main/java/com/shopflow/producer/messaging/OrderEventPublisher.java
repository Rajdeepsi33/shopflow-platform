package com.shopflow.producer.messaging;

import com.shopflow.common.constants.MessagingConstants;
import com.shopflow.common.event.OrderCreatedEvent;
import com.shopflow.producer.entity.Order;
import com.shopflow.producer.mapper.OrderMapper;
import com.shopflow.producer.repository.OrderRepository;
import com.shopflow.producer.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Publishes ORDER_CREATED after the database transaction commits.
 *
 * Publishing inside the transaction would risk announcing an order that
 * is then rolled back - the dual-write race. AFTER_COMMIT means the
 * database is the source of truth and the message only fires once that
 * truth is durable.
 *
 * The residual gap is a crash between commit and publish. Publisher
 * confirms close that: a nacked or unsent message leaves the order in
 * PUBLISH_FAILED, which is queryable and replayable. A transactional
 * outbox would remove the gap entirely.
 */
@Slf4j
@Component
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderService orderService;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate,
                               OrderRepository orderRepository,
                               OrderMapper orderMapper,
                               OrderService orderService) {
        this.rabbitTemplate = rabbitTemplate;
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.orderService = orderService;
        this.rabbitTemplate.setConfirmCallback(this::handleConfirm);
    }

    /**
     * Runs on the caller thread, after commit.
     *
     * REQUIRES_NEW because the transaction that saved the order has
     * already closed - there is nothing left to join, and the lazily
     * loaded item list needs an open session to read. Spring rejects a
     * plain @Transactional here for exactly that reason.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public void onOrderCreated(OrderCreatedApplicationEvent applicationEvent) {

        String orderRef = applicationEvent.orderRef();

        Order order = orderRepository.findByOrderRefWithItems(orderRef).orElse(null);
        if (order == null) {
            log.error("Order {} vanished between commit and publish", orderRef);
            return;
        }

        OrderCreatedEvent event = buildEvent(order);
        String routingKey = order.getShippingType().routingKey();

        try {
            rabbitTemplate.convertAndSend(
                    MessagingConstants.ORDER_EXCHANGE,
                    routingKey,
                    event,
                    message -> {
                        message.getMessageProperties().setMessageId(event.eventId());
                        message.getMessageProperties().setCorrelationId(order.getCorrelationId());
                        message.getMessageProperties().setHeader(
                                MessagingConstants.HEADER_ORDER_ID, orderRef);
                        message.getMessageProperties().setHeader(
                                MessagingConstants.HEADER_SHIPPING_TYPE, order.getShippingType().name());
                        message.getMessageProperties().setHeader(
                                MessagingConstants.HEADER_CORRELATION_ID, order.getCorrelationId());
                        message.getMessageProperties().setHeader(
                                MessagingConstants.HEADER_SCHEMA_VERSION, MessagingConstants.SCHEMA_VERSION);
                        return message;
                    },
                    new CorrelationData(orderRef));

            log.info("Published order {} with routing key {}", orderRef, routingKey);

        } catch (Exception e) {
            log.error("Failed to publish order {}: {}", orderRef, e.getMessage());
            orderService.markPublishFailed(orderRef);
        }
    }

    /**
     * Confirms arrive asynchronously on a Rabbit I/O thread, after the
     * request transaction has already committed. There is no transaction
     * to join, which is why markPublished/markPublishFailed are
     * REQUIRES_NEW.
     */
    public void handleConfirm(CorrelationData correlationData, boolean ack, String cause) {

        if (correlationData == null || correlationData.getId() == null) {
            log.warn("Publisher confirm with no correlation data (ack={})", ack);
            return;
        }

        String orderRef = correlationData.getId();

        if (ack) {
            orderService.markPublished(orderRef);
        } else {
            log.error("Broker nacked order {}: {}", orderRef, cause);
            orderService.markPublishFailed(orderRef);
        }
    }

    private OrderCreatedEvent buildEvent(Order order) {
        OrderCreatedEvent mapped = orderMapper.toEvent(order);
        return new OrderCreatedEvent(
                UUID.randomUUID().toString(),
                MessagingConstants.EVENT_TYPE_ORDER_CREATED,
                Instant.now(),
                MessagingConstants.SCHEMA_VERSION,
                mapped.orderRef(),
                mapped.customerEmail(),
                mapped.shippingType(),
                mapped.destinationCountry(),
                mapped.orderTotal(),
                mapped.items());
    }
}