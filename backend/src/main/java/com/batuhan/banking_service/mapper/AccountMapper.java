package com.batuhan.banking_service.mapper;

import com.batuhan.banking_service.dto.request.AccountCreateRequest;
import com.batuhan.banking_service.dto.response.AccountResponse;
import com.batuhan.banking_service.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "dailyUsage", ignore = true)
    @Mapping(target = "sentTransactions", ignore = true)
    @Mapping(target = "receivedTransactions", ignore = true)
    @Mapping(target = "iban", ignore = true)
    @Mapping(target = "balance", source = "initialBalance")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "isActive", constant = "true")
    AccountEntity toEntity(AccountCreateRequest request);

    @Mapping(target = "customerNumber", source = "user.customerNumber")
    AccountResponse toResponse(AccountEntity account);
}