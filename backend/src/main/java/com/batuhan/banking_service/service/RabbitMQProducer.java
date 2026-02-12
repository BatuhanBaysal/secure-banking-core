package com.batuhan.banking_service.service;

import com.batuhan.banking_service.dto.event.TransferEvent;

public interface RabbitMQProducer {

    void sendToQueue(TransferEvent event);
}