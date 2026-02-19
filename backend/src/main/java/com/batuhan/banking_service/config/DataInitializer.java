package com.batuhan.banking_service.config;

import com.batuhan.banking_service.config.util.DataGenerator;
import com.batuhan.banking_service.entity.*;
import com.batuhan.banking_service.entity.enums.*;
import com.batuhan.banking_service.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountLimitRepository accountLimitRepository;
    private final AddressRepository addressRepository;
    private final AuditLogRepository auditLogRepository;
    private final TransactionRepository transactionRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            if (userRepository.count() > 0) {
                log.info(">>> Database already contains data. Skipping seeding.");
                return;
            }

            log.info(">>> Starting production-ready seeding for Batuhan Banking...");

            List<AccountEntity> createdAccounts = seedUsersAndAccounts();
            seedTransactions(createdAccounts);
            seedAuditLogs();

            log.info(">>> Seeding completed successfully with 25 realistic users and accounts.");
        };
    }

    private List<AccountEntity> seedUsersAndAccounts() {
        String[] firstNames = {"Ahmet", "Mehmet", "Ayşe", "Fatma", "Can", "Zeynep", "Ali", "Hülya", "Burak", "Selin", "Mustafa", "Derya", "Emre", "Gökhan", "Ece"};
        String[] lastNames = {"Yılmaz", "Kaya", "Demir", "Çelik", "Yıldız", "Öztürk", "Aydın", "Arslan", "Polat", "Şahin", "Bulut", "Koç", "Kurt", "Özkan", "Aslan"};
        List<AccountEntity> createdAccounts = new ArrayList<>();

        for (int i = 0; i < 25; i++) {
            AddressEntity address = createAndSaveAddress(i);
            UserEntity user = createAndSaveUser(i, firstNames[i % firstNames.length], lastNames[i % lastNames.length], address);
            AccountEntity account = createAndSaveAccount(i, user);

            createAndSaveLimit(account);
            createdAccounts.add(account);
        }
        return createdAccounts;
    }

    private AddressEntity createAndSaveAddress(int i) {
        String[] cities = {"Istanbul", "Ankara", "Izmir", "Bursa", "Antalya"};
        String[] districts = {"Kadikoy", "Cankaya", "Karsiyaka", "Nilufer", "Muratpasa"};

        AddressEntity address = AddressEntity.builder()
                .externalId(UUID.randomUUID())
                .country("Turkey")
                .city(cities[i % cities.length])
                .district(districts[i % districts.length])
                .street("Street No: " + (secureRandom.nextInt(100) + 1))
                .addressDetail("Floor: " + (i % 5) + " Apartment: " + i)
                .zipCode(String.valueOf(34000 + i))
                .active(true)
                .build();
        return addressRepository.save(address);
    }

    private UserEntity createAndSaveUser(int i, String fName, String lName, AddressEntity address) {
        UserEntity user = UserEntity.builder()
                .externalId(UUID.randomUUID().toString())
                .firstName(fName)
                .lastName(lName)
                .tckn(DataGenerator.generateTckn(i))
                .customerNumber("CUST" + (1000 + i))
                .email(fName.toLowerCase() + "." + lName.toLowerCase() + i + "@batuhanbanking.com")
                .birthDate(LocalDate.of(1980, 1, 1).plusYears(secureRandom.nextInt(30)))
                .phoneNumber("555" + (1000000 + i))
                .role(i == 0 ? Role.ADMIN : Role.USER)
                .address(address)
                .active(true)
                .build();
        return userRepository.save(user);
    }

    private AccountEntity createAndSaveAccount(int i, UserEntity user) {
        BigDecimal initialBalance = new BigDecimal(1000 + secureRandom.nextInt(90000)).setScale(4, RoundingMode.HALF_UP);
        AccountEntity account = AccountEntity.builder()
                .externalId(UUID.randomUUID())
                .iban(DataGenerator.generateIban(i))
                .balance(initialBalance)
                .currency(CurrencyType.TRY)
                .status(AccountStatus.ACTIVE)
                .dailyLimit(new BigDecimal("20000.0000"))
                .user(user)
                .active(true)
                .build();
        return accountRepository.save(account);
    }

    private void createAndSaveLimit(AccountEntity account) {
        accountLimitRepository.save(AccountLimitEntity.builder()
                .externalId(UUID.randomUUID())
                .account(account)
                .limitDate(LocalDate.now())
                .dailyLimit(account.getDailyLimit())
                .usedAmount(BigDecimal.ZERO)
                .active(true)
                .build());
    }

    private void seedTransactions(List<AccountEntity> accounts) {
        log.info(">>> Seeding Transactions...");
        for (int j = 0; j < Math.min(accounts.size() - 1, 15); j++) {
            AccountEntity sender = accounts.get(j);
            AccountEntity receiver = accounts.get(j + 1);
            transactionRepository.save(TransactionEntity.builder()
                    .externalId(UUID.randomUUID())
                    .referenceNumber("TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .senderAccount(sender)
                    .receiverAccount(receiver)
                    .amount(new BigDecimal(100 + secureRandom.nextInt(500)).setScale(4, RoundingMode.HALF_UP))
                    .currency(CurrencyType.TRY)
                    .transactionType(TransactionType.TRANSFER)
                    .status(TransactionStatus.COMPLETED)
                    .description("Transfer for service payment - " + j)
                    .build());
        }
    }

    private void seedAuditLogs() {
        try {
            log.info(">>> Seeding Audit Logs...");
            for (int k = 0; k < 5; k++) {
                auditLogRepository.save(AuditLogEntity.builder()
                        .action("SYSTEM_INIT")
                        .email("admin@batuhanbanking.com")
                        .details("System data initialization check #" + k)
                        .ipAddress("127.0.0.1")
                        .build());
            }
        } catch (Exception e) {
            log.error(">>> Audit log seeding failed: {}", e.getMessage());
        }
    }
}