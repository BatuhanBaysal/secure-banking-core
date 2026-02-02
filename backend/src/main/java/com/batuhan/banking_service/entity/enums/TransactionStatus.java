package com.batuhan.banking_service.entity.enums;

import lombok.Getter;

@Getter
public enum TransactionStatus {

    PENDING("Transaction is initiated and waiting for processing"),
    COMPLETED("Transaction successfully finished"),
    FAILED("Transaction failed due to insufficient funds or technical errors");

    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }
}