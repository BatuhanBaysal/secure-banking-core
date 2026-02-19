package com.batuhan.banking_service.repository.specification;

import com.batuhan.banking_service.entity.TransactionEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionSpecifications {

    private static final String AMOUNT_FIELD = "amount";
    private static final String CREATED_AT_FIELD = "createdAt";
    private static final String IBAN_FIELD = "iban";

    private TransactionSpecifications() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<TransactionEntity> hasIban(String iban) {
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("senderAccount").get(IBAN_FIELD), iban),
                cb.equal(root.get("receiverAccount").get(IBAN_FIELD), iban)
        );
    }

    public static Specification<TransactionEntity> amountBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min != null && max != null) return cb.between(root.get(AMOUNT_FIELD), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get(AMOUNT_FIELD), min);
            if (max != null) return cb.lessThanOrEqualTo(root.get(AMOUNT_FIELD), max);
            return null;
        };
    }

    public static Specification<TransactionEntity> dateBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start != null && end != null) return cb.between(root.get(CREATED_AT_FIELD), start, end);
            if (start != null) return cb.greaterThanOrEqualTo(root.get(CREATED_AT_FIELD), start);
            if (end != null) return cb.lessThanOrEqualTo(root.get(CREATED_AT_FIELD), end);
            return null;
        };
    }
}