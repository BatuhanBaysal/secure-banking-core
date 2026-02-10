package com.batuhan.banking_service.config;

import com.batuhan.banking_service.entity.*;
import com.batuhan.banking_service.entity.enums.*;
import com.batuhan.banking_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountLimitRepository accountLimitRepository;
    private final AuditLogRepository auditLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    @Bean
    public CommandLineRunner loadData() {
        return args -> {
            if (userRepository.count() > 0) {
                System.out.println(">>> Database already contains data. Seeding skipped to prevent duplicates.");
                return;
            }

            String[] firstNames = {"Ahmet", "Mehmet", "Ayşe", "Fatma", "Can", "Ece", "Gökhan", "Selin", "Burak", "Derya", "Mustafa", "Zeynep", "Emre", "Seda", "Hüseyin", "Merve", "Kerem", "Aslı", "Onur", "Pelin", "Deniz", "Ege", "Irmak", "Mert", "Gizem"};
            String[] lastNames = {"Yılmaz", "Kaya", "Demir", "Çelik", "Şahin", "Öztürk", "Arslan", "Doğan", "Kılıç", "Aydın", "Yıldız", "Aras", "Güneş", "Korkmaz", "Polat", "Özdemir", "Bulut", "Yalçın", "Güler", "Aksoy", "Sarı", "Avcı", "Eren", "Turan", "Yavuz"};
            String[] cities = {"İstanbul", "Ankara", "İzmir", "Bursa", "Antalya", "Eskişehir", "Adana", "Trabzon", "Samsun", "Muğla", "Kocaeli", "Diyarbakır", "Denizli", "Sakarya", "Tekirdağ", "Aydın", "Balıkesir", "Kayseri", "Gaziantep", "Konya", "Mersin", "Hatay", "Manisa", "Çanakkale", "Ordu"};

            System.out.println(">>> Initializing Mega Data Seeding (25 records per table)...");

            for (int i = 0; i < 25; i++) {
                String tckn = String.valueOf(10000000000L + (i * 1234567L));
                String fName = firstNames[i % firstNames.length];
                String lName = lastNames[i % lastNames.length];

                AddressEntity address = AddressEntity.builder()
                        .city(cities[i % cities.length])
                        .street(lastNames[random.nextInt(lastNames.length)] + " Street No:" + (random.nextInt(100) + 1))
                        .zipCode(String.valueOf(10000 + random.nextInt(80000)))
                        .phoneNumber("5" + (30 + random.nextInt(10)) + " " + (100 + random.nextInt(900)) + " " + (10 + random.nextInt(89)) + " " + (10 + random.nextInt(89)))
                        .isActive(true)
                        .build();

                UserEntity user = UserEntity.builder()
                        .firstName(fName)
                        .lastName(lName)
                        .tckn(tckn)
                        .customerNumber("CUST" + (9000 + i))
                        .email(fName.toLowerCase() + "." + lName.toLowerCase() + i + "@securebank.com")
                        .password(passwordEncoder.encode("Secure123!"))
                        .birthDate(LocalDate.of(1970, 1, 1).plusYears(random.nextInt(40)))
                        .isActive(true)
                        .role(i == 0 ? Role.ADMIN : Role.USER)
                        .address(address)
                        .build();

                UserEntity savedUser = userRepository.save(user);

                BigDecimal balance = new BigDecimal(1000 + random.nextInt(250000));
                AccountEntity account = AccountEntity.builder()
                        .iban("TR" + (10 + random.nextInt(89)) + "00062" + String.format("%017d", Math.abs(random.nextLong() % 100000000000000000L)))
                        .balance(balance)
                        .currency(CurrencyType.values()[random.nextInt(CurrencyType.values().length)])
                        .status(AccountStatus.ACTIVE)
                        .dailyLimit(new BigDecimal(5000 + (random.nextInt(5) * 5000)))
                        .isActive(true)
                        .user(savedUser)
                        .build();

                AccountEntity savedAccount = accountRepository.save(account);

                accountLimitRepository.save(AccountLimitEntity.builder()
                        .account(savedAccount)
                        .dailyLimit(savedAccount.getDailyLimit())
                        .usedAmount(BigDecimal.ZERO)
                        .limitDate(LocalDate.now())
                        .isActive(true)
                        .build());
            }

            List<AccountEntity> allAccounts = accountRepository.findAll();
            for (int j = 0; j < 25; j++) {
                AccountEntity sender = allAccounts.get(random.nextInt(allAccounts.size()));
                AccountEntity receiver = allAccounts.get(random.nextInt(allAccounts.size()));

                if (sender.getId().equals(receiver.getId())) continue;

                transactionRepository.save(TransactionEntity.builder()
                        .senderAccount(sender)
                        .receiverAccount(receiver)
                        .amount(new BigDecimal(50 + random.nextInt(3000)))
                        .transactionType(TransactionType.TRANSFER)
                        .status(TransactionStatus.COMPLETED)
                        .description("Payment for Invoice #" + (500 + j))
                        .referenceNumber("TRX-" + System.nanoTime())
                        .build());
            }

            String[] actions = {"LOGIN_SUCCESS", "TRANSFER_INITIATED", "LIMIT_UPDATE", "PROFILE_VIEW", "ACCOUNT_CREATED"};
            for (int k = 0; k < 25; k++) {
                auditLogRepository.save(AuditLogEntity.builder()
                        .action(actions[random.nextInt(actions.length)])
                        .email("user" + k + "@securebank.com")
                        .details("System event log entry number " + k)
                        .build());
            }

            System.out.println(">>> Mega Seeding Success: 25 Users, 25 Accounts, 25 Limits, 25 Transactions, and 25 Logs created.");
        };
    }
}