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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

    private final Random random = new Random();

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            if (userRepository.count() > 0) {
                log.info(">>> Database already contains data. Skipping seeding.");
                return;
            }

            log.info(">>> Starting production-ready seeding for Batuhan Banking...");

            String[] firstNames = {"Ahmet", "Mehmet", "Ayşe", "Fatma", "Can", "Zeynep", "Ali", "Hülya", "Burak", "Selin", "Mustafa", "Derya", "Emre", "Gökhan", "Ece"};
            String[] lastNames = {"Yılmaz", "Kaya", "Demir", "Çelik", "Yıldız", "Öztürk", "Aydın", "Arslan", "Polat", "Şahin", "Bulut", "Koç", "Kurt", "Özkan", "Aslan"};
            String[] cities = {"Istanbul", "Ankara", "Izmir", "Bursa", "Antalya"};
            String[] districts = {"Kadikoy", "Cankaya", "Karsiyaka", "Nilufer", "Muratpasa"};

            List<AccountEntity> createdAccounts = new ArrayList<>();

            for (int i = 0; i < 25; i++) {
                String fName = firstNames[i % firstNames.length];
                String lName = lastNames[i % lastNames.length];

                // Address
                AddressEntity address = AddressEntity.builder()
                        .externalId(UUID.randomUUID())
                        .country("Turkey")
                        .city(cities[i % cities.length])
                        .district(districts[i % districts.length])
                        .street("Street No: " + (random.nextInt(100) + 1))
                        .addressDetail("Floor: " + (i % 5) + " Apartment: " + i)
                        .zipCode(String.valueOf(34000 + i))
                        .active(true)
                        .build();
                addressRepository.save(address);

                // User
                UserEntity user = UserEntity.builder()
                        .externalId(UUID.randomUUID().toString())
                        .firstName(fName)
                        .lastName(lName)
                        .tckn(DataGenerator.generateTckn(i))
                        .customerNumber("CUST" + (1000 + i))
                        .email(fName.toLowerCase() + "." + lName.toLowerCase() + i + "@batuhanbanking.com")
                        .birthDate(LocalDate.of(1980, 1, 1).plusYears(random.nextInt(30)))
                        .phoneNumber("555" + (1000000 + i))
                        .role(i == 0 ? Role.ADMIN : Role.USER)
                        .address(address)
                        .active(true)
                        .build();
                userRepository.save(user);

                // Account
                BigDecimal initialBalance = new BigDecimal(1000 + random.nextInt(90000)).setScale(4);
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
                AccountEntity savedAccount = accountRepository.save(account);
                createdAccounts.add(savedAccount);

                // Limit
                accountLimitRepository.save(AccountLimitEntity.builder()
                        .externalId(UUID.randomUUID())
                        .account(savedAccount)
                        .limitDate(LocalDate.now())
                        .dailyLimit(savedAccount.getDailyLimit())
                        .usedAmount(BigDecimal.ZERO)
                        .active(true)
                        .build());
            }

            seedTransactions(createdAccounts);
            seedAuditLogs();
            log.info(">>> Seeding completed successfully with 25 realistic users and accounts.");
        };
    }

    private void seedTransactions(List<AccountEntity> accounts) {
        log.info(">>> Seeding Transactions...");
        for (int j = 0; j < 15; j++) {
            AccountEntity sender = accounts.get(j);
            AccountEntity receiver = accounts.get(j + 1);
            transactionRepository.save(TransactionEntity.builder()
                    .externalId(UUID.randomUUID())
                    .referenceNumber("TX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .senderAccount(sender)
                    .receiverAccount(receiver)
                    .amount(new BigDecimal(100 + random.nextInt(500)).setScale(4))
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
            log.info(">>> Audit logs seeded successfully.");
        } catch (Exception e) {
            log.error(">>> Audit log table might not be ready: {}", e.getMessage());
        }
    }
}