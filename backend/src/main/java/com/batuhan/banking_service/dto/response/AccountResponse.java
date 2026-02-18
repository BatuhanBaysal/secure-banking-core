package com.batuhan.banking_service.dto.response;

import com.batuhan.banking_service.entity.enums.AccountStatus;
import com.batuhan.banking_service.entity.enums.CurrencyType;
import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(

        UUID externalId,
        String iban,
        BigDecimal balance,
        CurrencyType currency,
        AccountStatus status,
        String customerNumber
) {}