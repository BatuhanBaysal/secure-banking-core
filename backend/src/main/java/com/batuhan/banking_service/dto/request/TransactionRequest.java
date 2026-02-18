package com.batuhan.banking_service.dto.request;

import com.batuhan.banking_service.entity.enums.CurrencyType;
import com.batuhan.banking_service.validator.ValidIban;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionRequest(

        @NotBlank(message = "Sender IBAN is required")
        @ValidIban
        String senderIban,

        @NotBlank(message = "Receiver IBAN is required")
        @ValidIban
        String receiverIban,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01")
        BigDecimal amount,

        @NotNull(message = "Currency type is required")
        CurrencyType currency,

        @Size(max = 255, message = "Description can be max 255 characters")
        String description
) {}