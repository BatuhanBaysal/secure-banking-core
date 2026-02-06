package com.batuhan.banking_service.repository;

import com.batuhan.banking_service.entity.AccountLimitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AccountLimitRepository extends JpaRepository<AccountLimitEntity, Long> {

    Optional<AccountLimitEntity> findByAccountIdAndLimitDate(Long accountId, LocalDate limitDate);
}