package com.batuhan.banking_service.dto.request;

import com.batuhan.banking_service.entity.enums.CurrencyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateRequest {

    @NotBlank(message = "Customer number is required")
    private String customerNumber;

    @NotNull(message = "Currency type is required")
    private CurrencyType currency;

    @NotNull(message = "Daily limit is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Daily limit must be greater than zero")
    private BigDecimal dailyLimit;

    @DecimalMin(value = "0.0", message = "Initial balance cannot be negative")
    private BigDecimal initialBalance;
}