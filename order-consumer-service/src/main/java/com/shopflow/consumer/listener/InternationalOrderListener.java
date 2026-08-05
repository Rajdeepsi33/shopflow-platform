package com.shopflow.consumer.listener;

import com.rabbitmq.client.Channel;
import com.shopflow.common.constants.MessagingConstants;
import com.shopflow.common.event.OrderCreatedEvent;
import com.shopflow.consumer.service.FulfilmentService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * Cross-border orders. Lowest volume, heaviest per-message work, so 1-2.
 */
@Component
public class InternationalOrderListener extends AbstractOrderListener {

    public InternationalOrderListener(FulfilmentService fulfilmentService) {
        super(fulfilmentService);
    }

    @RabbitListener(queues = MessagingConstants.QUEUE_INTERNATIONAL, concurrency = "1-2")
    public void onMessage(OrderCreatedEvent event,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        handle(event, channel, deliveryTag);
    }
}