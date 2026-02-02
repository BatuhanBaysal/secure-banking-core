package com.batuhan.banking_service.entity.enums;

import lombok.Getter;

@Getter
public enum TransactionType {

    TRANSFER("Money transfer between accounts"),
    DEPOSIT("Cash or digital deposit to account"),
    WITHDRAW("Cash withdrawal from account"),
    FEE("System or service fee deduction");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }
}