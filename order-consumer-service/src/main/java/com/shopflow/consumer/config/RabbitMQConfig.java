package com.shopflow.consumer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.common.constants.MessagingConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * The consumer declares the same topology as the producer.
 *
 * Declarations are idempotent in AMQP, so whichever service starts first
 * creates it and the other is a no-op. This means the consumer can run
 * standalone in tests without the producer present.
 */
@Configuration
public class RabbitMQConfig {

    @Bean
    public DirectExchange orderExchange() {
        return ExchangeBuilder.directExchange(MessagingConstants.ORDER_EXCHANGE).durable(true).build();
    }

    @Bean
    public DirectExchange orderDeadLetterExchange() {
        return ExchangeBuilder.directExchange(MessagingConstants.ORDER_DLX).durable(true).build();
    }

    private Queue workQueue(String name) {
        return QueueBuilder.durable(name)
                .withArguments(Map.of(
                        MessagingConstants.ARG_DEAD_LETTER_EXCHANGE, MessagingConstants.ORDER_DLX,
                        MessagingConstants.ARG_DEAD_LETTER_ROUTING_KEY, MessagingConstants.ROUTING_KEY_DEAD))
                .build();
    }

    @Bean
    public Queue standardQueue() {
        return workQueue(MessagingConstants.QUEUE_STANDARD);
    }

    @Bean
    public Queue expressQueue() {
        return workQueue(MessagingConstants.QUEUE_EXPRESS);
    }

    @Bean
    public Queue internationalQueue() {
        return workQueue(MessagingConstants.QUEUE_INTERNATIONAL);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(MessagingConstants.QUEUE_DLQ).build();
    }

    @Bean
    public Binding standardBinding(Queue standardQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(standardQueue).to(orderExchange)
                .with(MessagingConstants.ROUTING_KEY_STANDARD);
    }

    @Bean
    public Binding expressBinding(Queue expressQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(expressQueue).to(orderExchange)
                .with(MessagingConstants.ROUTING_KEY_EXPRESS);
    }

    @Bean
    public Binding internationalBinding(Queue internationalQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(internationalQueue).to(orderExchange)
                .with(MessagingConstants.ROUTING_KEY_INTERNATIONAL);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange orderDeadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(orderDeadLetterExchange)
                .with(MessagingConstants.ROUTING_KEY_DEAD);
    }

    /**
     * Must match the producer's converter so the JSON payload deserialises
     * back into OrderCreatedEvent.
     */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}