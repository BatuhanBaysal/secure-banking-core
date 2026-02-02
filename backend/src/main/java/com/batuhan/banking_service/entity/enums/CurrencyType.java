package com.batuhan.banking_service.entity.enums;

import lombok.Getter;

@Getter
public enum CurrencyType {

    TRY("Turkish Lira", "₺"),
    USD("US Dollar", "$"),
    EUR("Euro", "€"),
    GBP("British Pound", "£");

    private final String description;
    private final String symbol;

    CurrencyType(String description, String symbol) {
        this.description = description;
        this.symbol = symbol;
    }
}