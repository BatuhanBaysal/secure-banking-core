package com.batuhan.banking_service.controller;

import com.batuhan.banking_service.constant.Messages;
import com.batuhan.banking_service.dto.request.AccountCreateRequest;
import com.batuhan.banking_service.dto.response.AccountResponse;
import com.batuhan.banking_service.dto.common.GlobalResponse;
import com.batuhan.banking_service.service.AccountService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "Operations related to bank accounts, IBANs, and balances")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Create a new bank account", description = "Allows Admin or Owner to create a new account.")
    @ApiResponse(responseCode = "201", description = "Account created successfully")
    @PreAuthorize("hasRole('ADMIN') or @bankingBusinessValidator.isOwner(#request.customerNumber())")
    @RateLimiter(name = "accountCreationLimiter")
    public ResponseEntity<GlobalResponse<AccountResponse>> createAccount(@Valid @RequestBody AccountCreateRequest request) {
        log.info("API Request: Open account for Customer: {}", request.customerNumber());
        AccountResponse response = accountService.createAccount(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{iban}")
                .buildAndExpand(response.iban())
                .toUri();

        return ResponseEntity.created(location)
                .body(GlobalResponse.success(response, Messages.ACCOUNT_CREATED));
    }

    @GetMapping("/{iban}")
    @Operation(summary = "Get account details by IBAN")
    @PreAuthorize("hasRole('ADMIN') or @bankingBusinessValidator.isAccountOwner(#iban)")
    public ResponseEntity<GlobalResponse<AccountResponse>> getAccountByIban(@PathVariable String iban) {
        log.info("API Request: Get account details for IBAN: {}", iban.substring(0, 4) + "...");
        AccountResponse response = accountService.getAccountByIban(iban.trim());
        return ResponseEntity.ok(GlobalResponse.success(response, Messages.ACCOUNT_RETRIEVED));
    }

    @GetMapping("/customer/{customerNumber}")
    @Operation(summary = "List all accounts for a specific customer")
    @PreAuthorize("hasRole('ADMIN') or @bankingBusinessValidator.isOwner(#customerNumber)")
    public ResponseEntity<GlobalResponse<List<AccountResponse>>> getAccountsByCustomer(@PathVariable String customerNumber) {
        log.info("API Request: List accounts for customer: {}", customerNumber);
        List<AccountResponse> responses = accountService.getAccountsByCustomerNumber(customerNumber);
        return ResponseEntity.ok(GlobalResponse.success(responses, Messages.ACCOUNTS_LISTED));
    }

    @DeleteMapping("/{iban}")
    @Operation(summary = "Close a bank account (Soft Delete)")
    @PreAuthorize("hasRole('ADMIN') or @bankingBusinessValidator.isAccountOwner(#iban)")
    public ResponseEntity<GlobalResponse<Void>> closeAccount(@PathVariable String iban) {
        log.warn("API Request: CLOSE account IBAN: {}", iban);
        accountService.closeAccount(iban.trim());
        return ResponseEntity.ok(GlobalResponse.success(null, Messages.ACCOUNT_CLOSED));
    }
}