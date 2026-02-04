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
public class TransactionCategoryDTO {

    private String category;
    private BigDecimal amount;
    private Double percentage;
}