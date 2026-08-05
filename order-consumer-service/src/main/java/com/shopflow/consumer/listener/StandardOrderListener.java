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
 * Domestic ground orders. Low volume, so 1-3 consumers.
 */
@Component
public class StandardOrderListener extends AbstractOrderListener {

    public StandardOrderListener(FulfilmentService fulfilmentService) {
        super(fulfilmentService);
    }

    @RabbitListener(queues = MessagingConstants.QUEUE_STANDARD, concurrency = "1-3")
    public void onMessage(OrderCreatedEvent event,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        handle(event, channel, deliveryTag);
    }
}