package com.batuhan.banking_service.entity.enums;

import lombok.Getter;

@Getter
public enum Role {

    USER("Standard Bank Customer"),
    ADMIN("Bank Administrator");

    private final String description;

    Role(String description) {
        this.description = description;
    }
}