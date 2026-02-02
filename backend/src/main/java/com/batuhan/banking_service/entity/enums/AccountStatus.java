package com.batuhan.banking_service.entity.enums;

import lombok.Getter;

@Getter
public enum AccountStatus {

    ACTIVE("Account is open and usable"),
    SUSPENDED("Account is temporarily frozen due to security or user request"),
    CLOSED("Account is permanently closed and cannot be reopened");

    private final String description;

    AccountStatus(String description) {
        this.description = description;
    }
}