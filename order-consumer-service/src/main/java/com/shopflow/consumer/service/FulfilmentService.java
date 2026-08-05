package com.shopflow.consumer.service;

import com.shopflow.common.enums.ShippingType;
import com.shopflow.common.event.OrderCreatedEvent;
import com.shopflow.consumer.entity.Fulfilment;
import com.shopflow.consumer.listener.PermanentProcessingException;
import com.shopflow.consumer.repository.FulfilmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Entry point for message processing.
 *
 * Order of work matters: the idempotency check happens before any
 * database work, so a duplicate costs one Redis round trip rather than a
 * transaction. The unique constraint on order_ref is the backstop if
 * Redis has been flushed.
 */
@Slf4j
@Service
public class FulfilmentService {

    private final FulfilmentRepository fulfilmentRepository;
    private final IdempotencyGuard idempotencyGuard;
    private final Map<ShippingType, OrderProcessor> processors = new EnumMap<>(ShippingType.class);

    /**
     * Processors are injected as a list and indexed by the type each one
     * declares it supports. Adding a processor requires no change here.
     */
    public FulfilmentService(FulfilmentRepository fulfilmentRepository,
                             IdempotencyGuard idempotencyGuard,
                             List<OrderProcessor> orderProcessors) {
        this.fulfilmentRepository = fulfilmentRepository;
        this.idempotencyGuard = idempotencyGuard;
        orderProcessors.forEach(p -> processors.put(p.supports(), p));
    }

    /**
     * Transactional so the fulfilment write commits or rolls back as a
     * unit. The ack happens outside this method, after commit.
     */
    @Transactional
    public void handle(OrderCreatedEvent event) {

        String orderRef = event.orderRef();

        if (!idempotencyGuard.tryAcquire(orderRef)) {
            log.debug("Order {} already processed, dropping duplicate", orderRef);
            return;
        }

        try {
            OrderProcessor processor = processors.get(event.shippingType());
            if (processor == null) {
                throw new PermanentProcessingException(
                        "No processor for shipping type " + event.shippingType());
            }

            Fulfilment fulfilment = processor.process(event);
            fulfilmentRepository.save(fulfilment);

            log.info("Fulfilled order {} as {} - warehouse {}, SLA {}, total {}",
                    orderRef, fulfilment.getShippingType(), fulfilment.getWarehouseCode(),
                    fulfilment.getSlaDate(), fulfilment.getFinalTotal());

        } catch (DataIntegrityViolationException e) {
            // The unique constraint fired: a duplicate slipped past Redis.
            // Correct outcome, not an error - one row exists, so ack.
            log.warn("Duplicate fulfilment for {} caught by unique constraint", orderRef);

        } catch (Exception e) {
            // Release the guard so a redelivery can retry. Without this a
            // transient failure would permanently block the order.
            idempotencyGuard.release(orderRef);
            throw e;
        }
    }
}