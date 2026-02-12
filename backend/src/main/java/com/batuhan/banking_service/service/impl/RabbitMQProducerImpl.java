package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.config.RabbitMQConfig;
import com.batuhan.banking_service.dto.event.TransferEvent;
import com.batuhan.banking_service.service.RabbitMQProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQProducerImpl implements RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendToQueue(TransferEvent event) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, event);
    }
}