package com.shopflow.producer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.common.constants.MessagingConstants;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * AMQP topology.
 *
 * One direct exchange, three queues bound by exact routing key, and a
 * dead-letter exchange/queue pair. Direct rather than topic because the
 * routing values are a closed set of three with no wildcard semantics,
 * and no message should ever reach more than one queue.
 *
 * Declaring the topology on both services is safe: declarations are
 * idempotent, and whichever starts first creates it.
 */
@Configuration
public class RabbitMQConfig {

    // ── exchanges ────────────────────────────────────────────

    @Bean
    public DirectExchange orderExchange() {
        return ExchangeBuilder
                .directExchange(MessagingConstants.ORDER_EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange orderDeadLetterExchange() {
        return ExchangeBuilder
                .directExchange(MessagingConstants.ORDER_DLX)
                .durable(true)
                .build();
    }

    // ── queues ───────────────────────────────────────────────

    /**
     * Every work queue dead-letters to the same DLX. A message that is
     * rejected without requeue ends up in q.order.dlq rather than being
     * silently dropped.
     */
    private Queue workQueue(String name) {
        return QueueBuilder
                .durable(name)
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

    // ── bindings ─────────────────────────────────────────────

    @Bean
    public Binding standardBinding(Queue standardQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(standardQueue)
                .to(orderExchange)
                .with(MessagingConstants.ROUTING_KEY_STANDARD);
    }

    @Bean
    public Binding expressBinding(Queue expressQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(expressQueue)
                .to(orderExchange)
                .with(MessagingConstants.ROUTING_KEY_EXPRESS);
    }

    @Bean
    public Binding internationalBinding(Queue internationalQueue, DirectExchange orderExchange) {
        return BindingBuilder.bind(internationalQueue)
                .to(orderExchange)
                .with(MessagingConstants.ROUTING_KEY_INTERNATIONAL);
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange orderDeadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue)
                .to(orderDeadLetterExchange)
                .with(MessagingConstants.ROUTING_KEY_DEAD);
    }

    // ── template ─────────────────────────────────────────────

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    /**
     * Mandatory + returns callback so an unroutable message is surfaced
     * rather than silently discarded by the broker.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        template.setMandatory(true);
        return template;
    }
}