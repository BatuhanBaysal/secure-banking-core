package com.batuhan.banking_service.dto.common;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WeeklyTrendDTO(
        LocalDateTime date,
        BigDecimal totalAmount
) {}