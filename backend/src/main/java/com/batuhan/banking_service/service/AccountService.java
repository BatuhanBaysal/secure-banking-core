package com.batuhan.banking_service.service;

import com.batuhan.banking_service.dto.request.AccountCreateRequest;
import com.batuhan.banking_service.dto.response.AccountResponse;

import java.util.List;

public interface AccountService {

    AccountResponse createAccount(AccountCreateRequest request);
    AccountResponse getAccountByIban(String iban);
    List<AccountResponse> getAccountsByCustomerNumber(String customerNumber);
    void closeAccount(String iban);
}