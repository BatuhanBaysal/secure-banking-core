package com.batuhan.banking_service.dto.request;

import com.batuhan.banking_service.validator.ValidIban;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank(message = "Sender IBAN is required") @ValidIban String senderIban,
        @NotBlank(message = "Receiver IBAN is required") @ValidIban String receiverIban,
        @NotNull(message = "Amount is required") @DecimalMin(value = "0.01") BigDecimal amount,
        String description
) {}