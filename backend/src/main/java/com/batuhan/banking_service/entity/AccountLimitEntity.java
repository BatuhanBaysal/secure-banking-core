package com.batuhan.banking_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "account_limits",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_limit_account_date", columnNames = {"account_id", "limitDate"})
        },
        indexes = {
                @Index(name = "idx_limit_account_date", columnList = "account_id, limitDate")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class AccountLimitEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(unique = true, nullable = false, updatable = false)
    private UUID externalId = UUID.randomUUID();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private AccountEntity account;

    @Builder.Default
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal usedAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal dailyLimit;

    @Column(nullable = false)
    private LocalDate limitDate;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}