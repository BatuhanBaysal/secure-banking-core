package com.batuhan.banking_service.dto.response;

import com.batuhan.banking_service.entity.enums.TransactionStatus;
import com.batuhan.banking_service.entity.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String senderIban,
        String senderName,
        String receiverIban,
        String receiverName,
        BigDecimal amount,
        TransactionType transactionType,
        TransactionStatus status,
        String description,
        LocalDateTime createdAt,
        String referenceNumber
) {}