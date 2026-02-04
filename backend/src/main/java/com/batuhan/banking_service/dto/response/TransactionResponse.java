package com.batuhan.banking_service.dto.response;

import com.batuhan.banking_service.entity.enums.TransactionStatus;
import com.batuhan.banking_service.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {

    private Long id;
    private String senderIban;
    private String senderName;
    private String receiverIban;
    private String receiverName;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionStatus status;
    private String description;
    private LocalDateTime createdAt;
    private String referenceNumber;
}