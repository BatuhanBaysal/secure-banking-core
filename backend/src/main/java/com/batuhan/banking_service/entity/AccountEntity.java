package com.batuhan.banking_service.entity;

import com.batuhan.banking_service.entity.enums.AccountStatus;
import com.batuhan.banking_service.entity.enums.CurrencyType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts", indexes = {
        @Index(name = "idx_account_iban", columnList = "iban"),
        @Index(name = "idx_account_external_id", columnList = "externalId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AccountEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(unique = true, nullable = false, updatable = false)
    private UUID externalId = UUID.randomUUID();

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

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AccountLimitEntity dailyUsage;
}