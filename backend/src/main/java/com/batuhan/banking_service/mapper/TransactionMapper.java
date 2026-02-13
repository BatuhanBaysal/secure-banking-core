package com.batuhan.banking_service.mapper;

import com.batuhan.banking_service.dto.response.TransactionResponse;
import com.batuhan.banking_service.entity.AccountEntity;
import com.batuhan.banking_service.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "senderIban", source = "senderAccount.iban")
    @Mapping(target = "receiverIban", source = "receiverAccount.iban")
    @Mapping(target = "senderName", expression = "java(getFullName(entity.getSenderAccount()))")
    @Mapping(target = "receiverName", expression = "java(getFullName(entity.getReceiverAccount()))")
    TransactionResponse toResponse(TransactionEntity entity);

    default String getFullName(AccountEntity account) {
        if (account == null || account.getUser() == null) {
            return "Unknown User";
        }
        return account.getUser().getFirstName() + " " + account.getUser().getLastName();
    }
}