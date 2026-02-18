package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.config.RabbitMQConfig;
import com.batuhan.banking_service.dto.event.TransferEvent;
import com.batuhan.banking_service.service.RabbitMQProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQProducerImpl implements RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendToQueue(TransferEvent event) {
        log.debug("Sending transfer event to RabbitMQ. Ref: {}, To: {}",
                event.referenceNumber(), event.toEmail());

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE,
                    RabbitMQConfig.ROUTING_KEY,
                    event
            );
            log.info("Transfer event successfully published to exchange for Ref: {}", event.referenceNumber());
        } catch (Exception e) {
            log.error("Failed to publish event to RabbitMQ for Ref: {}. Error: {}",
                    event.referenceNumber(), e.getMessage());
        }
    }
}