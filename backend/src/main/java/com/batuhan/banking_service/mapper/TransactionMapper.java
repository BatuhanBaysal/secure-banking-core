package com.batuhan.banking_service.mapper;

import com.batuhan.banking_service.dto.response.TransactionResponse;
import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "senderIban", source = "senderAccount.iban")
    @Mapping(target = "receiverIban", source = "receiverAccount.iban")
    @Mapping(target = "senderName", source = "senderAccount", qualifiedByName = "getFullName")
    @Mapping(target = "receiverName", source = "receiverAccount", qualifiedByName = "getFullName")
    TransactionResponse toResponse(TransactionEntity transaction);

    @Named("getFullName")
    default String getFullName(AccountEntity account) {
        if (account == null || account.getUser() == null) {
            return "Unknown Customer";
        }
        return account.getUser().getFirstName() + " " + account.getUser().getLastName();
    }
}