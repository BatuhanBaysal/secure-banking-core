package com.batuhan.banking_service;

import com.batuhan.banking_service.dto.common.*;
import com.batuhan.banking_service.dto.request.*;
import com.batuhan.banking_service.dto.response.*;
import com.batuhan.banking_service.entity.*;
import com.batuhan.banking_service.entity.enums.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Centralized test data provider for the entire banking service test suite.
 * This factory follows the Object Mother pattern to create entities, DTOs, and requests.
 * It includes utility methods for generating realistic and valid banking identifiers
 * such as IBANs and TCKNs to satisfy domain validation rules.
 */
public final class TestDataFactory {

    private TestDataFactory() {}

    // --- (CONSTANTS & DEFAULTS) ---
    public static final String VALID_IBAN = "TR730008800000002000000000";
    public static final String OTHER_VALID_IBAN = "TR460008800000002000000001";
    private static final BigDecimal DEFAULT_BALANCE = new BigDecimal("1000.00");
    private static final BigDecimal DEFAULT_LIMIT = new BigDecimal("5000.00");

    // --- 1. USER & ADDRESS MODULE ---
    public static UserEntity createTestUser() {
        return UserEntity.builder()
                .externalId(UUID.randomUUID().toString())
                .firstName("Batuhan")
                .lastName("Test")
                .email(String.format("user-%s@test.com", UUID.randomUUID()))
                .tckn(generateValidTckn())
                .customerNumber(generateNumericString(10))
                .birthDate(LocalDate.of(1995, 1, 1))
                .phoneNumber("+90555" + generateNumericString(7))
                .role(Role.USER)
                .active(true)
                .build();
    }

    public static UserCreateRequest createUserCreateRequest() {
        return new UserCreateRequest(
                "Batuhan", "Test", "10000000146",
                "batuhan@test.com", "SafePass123!",
                LocalDate.of(1995, 1, 1),
                createAddressDto(), "+905554443322"
        );
    }

    public static UserResponse createUserResponse(String customerNumber) {
        return new UserResponse(
                UUID.randomUUID().toString(),
                "Batuhan", "Test", "batuhan@test.com",
                "+905554443322", "10000000146",
                customerNumber, createAddressDto(), LocalDateTime.now()
        );
    }

    public static UserUpdateRequest createUserUpdateRequest() {
        return new UserUpdateRequest(
                "Batuhan", "Updated", "updated@test.com",
                "+905554443322", createAddressDto()
        );
    }

    // --- 2. ACCOUNT MODULE ---
    public static AccountEntity createTestAccount(UserEntity user, String iban) {
        return AccountEntity.builder()
                .externalId(UUID.randomUUID())
                .user(user)
                .iban(iban)
                .balance(DEFAULT_BALANCE)
                .dailyLimit(DEFAULT_LIMIT)
                .status(AccountStatus.ACTIVE)
                .active(true)
                .currency(CurrencyType.TRY)
                .build();
    }

    public static AccountCreateRequest createAccountRequest() {
        return new AccountCreateRequest(
                generateNumericString(10),
                CurrencyType.TRY,
                DEFAULT_LIMIT,
                DEFAULT_BALANCE
        );
    }

    public static AccountEntity createEmptyBalanceAccount(UserEntity user) {
        AccountEntity account = createTestAccount(user, VALID_IBAN);
        account.setBalance(BigDecimal.ZERO);
        return account;
    }

    // --- 3. TRANSACTION MODULE ---
    public static TransactionRequest createTransactionRequest(String sender, String receiver, BigDecimal amount) {
        return new TransactionRequest(
                sender, receiver, amount,
                CurrencyType.TRY,
                "Manual Test Transfer Payload"
        );
    }

    public static TransactionResponse createTransactionResponse() {
        return new TransactionResponse(
                UUID.randomUUID(),
                VALID_IBAN, "Sender Name", OTHER_VALID_IBAN, "Receiver Name",
                new BigDecimal("100.00"), TransactionType.TRANSFER, TransactionStatus.COMPLETED,
                "Success", LocalDateTime.now(), "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
        );
    }

    public static TransactionEntity createTransactionEntity(AccountEntity sender, AccountEntity receiver, BigDecimal amount) {
        return TransactionEntity.builder()
                .externalId(UUID.randomUUID())
                .senderAccount(sender)
                .receiverAccount(receiver)
                .amount(amount)
                .currency(CurrencyType.TRY)
                .referenceNumber(UUID.randomUUID().toString().substring(0, 20))
                .status(TransactionStatus.COMPLETED)
                .transactionType(TransactionType.TRANSFER)
                .description("Automated Test Transfer")
                .build();
    }

    // --- 4. DTOs & OTHERS ---
    public static AddressDto createAddressDto() {
        return new AddressDto(
                "Turkey", "Istanbul", "Levent",
                "Street 123", "Block B, No: 5", "34330"
        );
    }

    public static TransactionSummaryDTO createTransactionSummaryDTO() {
        return new TransactionSummaryDTO(new BigDecimal("1000.00"), new BigDecimal("500.00"), 5L);
    }

    public static TransactionCategoryDTO createTransactionCategoryDTO(String category, BigDecimal amount, Double percentage) {
        return new TransactionCategoryDTO(category, amount, percentage);
    }

    // --- 5. INTERNAL UTILITIES (Banking Domain Specific) ---
    private static String generateNumericString(int length) {
        StringBuilder sb = new StringBuilder();
        while (sb.length() < length) {
            sb.append(UUID.randomUUID().toString().replaceAll("[^0-9]", ""));
        }
        return sb.substring(0, length);
    }

    /**
     * Algorithmically generates a valid Turkish Identity Number (TCKN) to bypass validation checks.
     */
    public static String generateValidTckn() {
        Random random = new Random();
        int[] digits = new int[11];
        digits[0] = random.nextInt(9) + 1;

        for (int i = 1; i < 9; i++) {
            digits[i] = random.nextInt(10);
        }

        int oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8];
        int evenSum = digits[1] + digits[3] + digits[5] + digits[7];
        digits[9] = ((oddSum * 7) - evenSum) % 10;
        if (digits[9] < 0) digits[9] += 10;

        int totalSum = 0;
        for (int i = 0; i < 10; i++) {
            totalSum += digits[i];
        }
        digits[10] = totalSum % 10;

        StringBuilder tckn = new StringBuilder();
        for (int digit : digits) {
            tckn.append(digit);
        }

        if (tckn.toString().equals("11111111110") || tckn.toString().chars().distinct().count() == 1) {
            return generateValidTckn();
        }

        return tckn.toString();
    }

    /**
     * Generates a random but valid Turkish IBAN using the Mod-97 checksum algorithm.
     */
    public static String generateRandomValidIban() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 22; i++) {
            sb.append(random.nextInt(10));
        }
        String accountPart = sb.toString();

        String rearranged = accountPart + "292700";
        BigInteger numericIban = new BigInteger(rearranged);
        int remainder = numericIban.mod(BigInteger.valueOf(97)).intValue();
        int checksum = 98 - remainder;

        String checksumStr = (checksum < 10) ? "0" + checksum : String.valueOf(checksum);
        return "TR" + checksumStr + accountPart;
    }
}