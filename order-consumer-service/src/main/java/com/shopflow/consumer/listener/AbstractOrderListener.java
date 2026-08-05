package com.shopflow.consumer.listener;

import com.rabbitmq.client.Channel;
import com.shopflow.common.event.OrderCreatedEvent;
import com.shopflow.consumer.service.FulfilmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * Shared delivery handling. The three listeners stay separate classes so
 * each has its own container, thread pool and failure blast radius; only
 * the ack/nack mechanics are common.
 *
 * Acknowledgement is manual and happens after the processing transaction
 * commits, so a crash mid-work leaves the message unacked and the broker
 * redelivers it.
 */
@Slf4j
@RequiredArgsConstructor
abstract class AbstractOrderListener {

    protected final FulfilmentService fulfilmentService;

    protected void handle(OrderCreatedEvent event, Channel channel, long deliveryTag) {

        try {
            fulfilmentService.handle(event);
            channel.basicAck(deliveryTag, false);

        } catch (PermanentProcessingException e) {
            // Retrying will never help - reject without requeue so the
            // broker dead-letters it.
            log.error("Permanent failure for order {}: {}", event.orderRef(), e.getMessage());
            reject(channel, deliveryTag);

        } catch (Exception e) {
            // Transient. Reject without requeue so it dead-letters rather
            // than spinning; default-requeue-rejected is false.
            log.error("Failed to process order {}: {}", event.orderRef(), e.getMessage(), e);
            reject(channel, deliveryTag);
        }
    }

    private void reject(Channel channel, long deliveryTag) {
        try {
            channel.basicReject(deliveryTag, false);
        } catch (IOException io) {
            log.error("Could not reject message {}: {}", deliveryTag, io.getMessage());
        }
    }
}