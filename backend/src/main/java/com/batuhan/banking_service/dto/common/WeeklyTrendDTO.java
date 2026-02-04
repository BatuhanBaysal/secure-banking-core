package com.batuhan.banking_service.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WeeklyTrendDTO {

    private LocalDateTime date;
    private BigDecimal totalAmount;
}