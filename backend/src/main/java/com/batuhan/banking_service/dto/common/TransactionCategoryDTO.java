package com.batuhan.banking_service.dto.common;

import java.math.BigDecimal;

public record TransactionCategoryDTO(
        String category,
        BigDecimal amount,
        Double percentage
) {}