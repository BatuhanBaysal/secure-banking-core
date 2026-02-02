package com.batuhan.banking_service.entity;

import com.batuhan.banking_service.entity.enums.AccountStatus;
import com.batuhan.banking_service.entity.enums.CurrencyType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 34)
    private String iban;

    @Builder.Default
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurrencyType currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal dailyLimit;

    @Column(nullable = false)
    private boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AccountLimitEntity dailyUsage;

    @OneToMany(mappedBy = "senderAccount")
    private List<TransactionEntity> sentTransactions;

    @OneToMany(mappedBy = "receiverAccount")
    private List<TransactionEntity> receivedTransactions;
}