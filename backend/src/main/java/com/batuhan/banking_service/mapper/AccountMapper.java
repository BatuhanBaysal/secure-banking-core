package com.batuhan.banking_service.mapper;

import com.batuhan.banking_service.dto.request.AccountCreateRequest;
import com.batuhan.banking_service.dto.response.AccountResponse;
import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.entity.enums.AccountStatus;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountEntity toEntity(AccountCreateRequest request) {
        return AccountEntity.builder()
                .currency(request.getCurrency())
                .dailyLimit(request.getDailyLimit())
                .balance(request.getInitialBalance())
                .status(AccountStatus.ACTIVE)
                .isActive(true)
                .build();
    }

    public AccountResponse toResponse(AccountEntity account) {
        if (account == null) return null;

        return AccountResponse.builder()
                .iban(account.getIban())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .status(account.getStatus())
                .customerNumber(account.getUser() != null ? account.getUser().getCustomerNumber() : null)
                .build();
    }
}