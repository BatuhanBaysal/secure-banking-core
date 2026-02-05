package com.batuhan.banking_service.mapper;

import com.batuhan.banking_service.dto.response.TransactionResponse;
import com.batuhan.banking_service.entity.TransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponse toResponse(TransactionEntity entity) {
        if (entity == null) return null;

        return TransactionResponse.builder()
                .senderIban(entity.getSenderAccount().getIban())
                .senderName(entity.getSenderAccount().getUser().getFirstName() + " " + entity.getSenderAccount().getUser().getLastName())
                .receiverIban(entity.getReceiverAccount().getIban())
                .receiverName(entity.getReceiverAccount().getUser().getFirstName() + " " + entity.getReceiverAccount().getUser().getLastName())
                .amount(entity.getAmount())
                .transactionType(entity.getTransactionType())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .referenceNumber(entity.getReferenceNumber())
                .build();
    }
}