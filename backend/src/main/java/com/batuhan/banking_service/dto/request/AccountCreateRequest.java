package com.batuhan.banking_service.dto.request;

import com.batuhan.banking_service.entity.enums.CurrencyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountCreateRequest(
        @NotBlank(message = "Customer number is required") String customerNumber,
        @NotNull(message = "Currency type is required") CurrencyType currency,
        @NotNull(message = "Daily limit is required")
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal dailyLimit,
        @DecimalMin(value = "0.0") BigDecimal initialBalance
) {}