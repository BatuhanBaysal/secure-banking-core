package com.batuhan.banking_service.repository;

import com.batuhan.banking_service.entity.AccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    boolean existsByIban(String iban);

    @Query("SELECT a FROM AccountEntity a JOIN FETCH a.user WHERE a.iban = :iban")
    Optional<AccountEntity> findByIban(@Param("iban") String iban);

    @Query("SELECT a FROM AccountEntity a JOIN FETCH a.user WHERE a.user.customerNumber = :customerNumber")
    List<AccountEntity> findByUserCustomerNumber(@Param("customerNumber") String customerNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a JOIN FETCH a.user WHERE a.iban = :iban")
    Optional<AccountEntity> findByIbanWithLock(@Param("iban") String iban);

    List<AccountEntity> findByActiveTrue();
}