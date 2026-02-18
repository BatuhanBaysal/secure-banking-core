package com.batuhan.banking_service.entity;

import com.batuhan.banking_service.entity.enums.CurrencyType;
import com.batuhan.banking_service.entity.enums.TransactionStatus;
import com.batuhan.banking_service.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_external_id", columnList = "externalId"),
        @Index(name = "idx_transaction_reference", columnList = "referenceNumber"),
        @Index(name = "idx_transaction_sender_acc", columnList = "sender_account_id"),
        @Index(name = "idx_transaction_receiver_acc", columnList = "receiver_account_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class TransactionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(unique = true, nullable = false, updatable = false)
    private UUID externalId = UUID.randomUUID();

    @Column(unique = true, nullable = false, updatable = false, length = 50)
    private String referenceNumber;

    @Column(nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private CurrencyType currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_account_id", nullable = false)
    private AccountEntity senderAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_account_id", nullable = false)
    private AccountEntity receiverAccount;
}