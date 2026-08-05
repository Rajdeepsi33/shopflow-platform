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
 * Next-day orders. Expected hot partition, so 3-8 consumers - the
 * concrete reason these are separate classes rather than one listener.
 */
@Component
public class ExpressOrderListener extends AbstractOrderListener {

    public ExpressOrderListener(FulfilmentService fulfilmentService) {
        super(fulfilmentService);
    }

    @RabbitListener(queues = MessagingConstants.QUEUE_EXPRESS, concurrency = "3-8")
    public void onMessage(OrderCreatedEvent event,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        handle(event, channel, deliveryTag);
    }
}