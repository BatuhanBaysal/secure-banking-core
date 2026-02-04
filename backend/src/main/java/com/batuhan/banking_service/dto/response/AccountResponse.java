package com.batuhan.banking_service.dto.response;

import com.batuhan.banking_service.entity.enums.AccountStatus;
import com.batuhan.banking_service.entity.enums.CurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {

    private Long id;
    private String iban;
    private BigDecimal balance;
    private CurrencyType currency;
    private AccountStatus status;
    private String customerNumber;
}