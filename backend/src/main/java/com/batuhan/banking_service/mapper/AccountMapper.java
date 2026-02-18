package com.batuhan.banking_service.mapper;

import com.batuhan.banking_service.dto.request.AccountCreateRequest;
import com.batuhan.banking_service.dto.response.AccountResponse;
import com.batuhan.banking_service.entity.AccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "externalId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "iban", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastModifiedBy", ignore = true)
    @Mapping(target = "version", ignore = true)
    AccountEntity toEntity(AccountCreateRequest request);

    @Mapping(target = "customerNumber", source = "user.customerNumber")
    AccountResponse toResponse(AccountEntity account);
}