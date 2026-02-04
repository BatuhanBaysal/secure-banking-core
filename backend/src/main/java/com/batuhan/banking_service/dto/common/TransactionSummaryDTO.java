package com.batuhan.banking_service.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionSummaryDTO {

    private BigDecimal totalSent;
    private BigDecimal totalReceived;
    private Long transactionCount;
}