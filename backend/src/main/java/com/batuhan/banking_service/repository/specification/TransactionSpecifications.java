package com.batuhan.banking_service.repository.specification;

import com.batuhan.banking_service.entity.TransactionEntity;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionSpecifications {

    public static Specification<TransactionEntity> hasIban(String iban) {
        return (root, query, cb) -> cb.or(
                cb.equal(root.get("senderAccount").get("iban"), iban),
                cb.equal(root.get("receiverAccount").get("iban"), iban)
        );
    }

    public static Specification<TransactionEntity> amountBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min != null && max != null) return cb.between(root.get("amount"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("amount"), min);
            if (max != null) return cb.lessThanOrEqualTo(root.get("amount"), max);
            return null;
        };
    }

    public static Specification<TransactionEntity> dateBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> {
            if (start != null && end != null) return cb.between(root.get("createdAt"), start, end);
            if (start != null) return cb.greaterThanOrEqualTo(root.get("createdAt"), start);
            if (end != null) return cb.lessThanOrEqualTo(root.get("createdAt"), end);
            return null;
        };
    }
}