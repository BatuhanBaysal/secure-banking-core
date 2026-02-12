package com.batuhan.banking_service.service.messaging;

import com.batuhan.banking_service.config.RabbitMQConfig;
import com.batuhan.banking_service.dto.event.TransferEvent;
import com.batuhan.banking_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void consumeTransferEvent(TransferEvent event) {
        log.info("Message received from queue for Ref: {}", event.referenceNumber());
        emailService.sendTransferEmail(
                event.toEmail(),
                event.firstName(),
                event.amount(),
                event.receiverIban(),
                event.referenceNumber()
        );
    }
}