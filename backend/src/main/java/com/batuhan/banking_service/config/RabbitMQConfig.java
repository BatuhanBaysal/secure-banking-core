package com.batuhan.banking_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE = "transfer_email_queue";
    public static final String EXCHANGE = "transfer_exchange";
    public static final String ROUTING_KEY = "transfer_routing_key";

    @Bean
    public Queue transferEmailQueue() {
        return new Queue(QUEUE, true);
    }

    @Bean
    public DirectExchange transferExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}