package com.batuhan.banking_service.dto.common;

import java.math.BigDecimal;

public record TransactionSummaryDTO(
        BigDecimal totalSent,
        BigDecimal totalReceived,
        Long transactionCount
) {}