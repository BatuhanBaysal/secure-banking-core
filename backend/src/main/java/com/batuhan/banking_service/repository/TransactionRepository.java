package com.batuhan.banking_service.repository;

import com.batuhan.banking_service.dto.common.TransactionSummaryDTO;
import com.batuhan.banking_service.dto.common.WeeklyTrendDTO;
import com.batuhan.banking_service.entity.TransactionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long>,
        JpaSpecificationExecutor<TransactionEntity> {

    @Query("SELECT t FROM TransactionEntity t WHERE t.senderAccount.iban = :iban OR t.receiverAccount.iban = :iban")
    Page<TransactionEntity> findAllByIban(@Param("iban") String iban, Pageable pageable);

    @Query("SELECT new com.batuhan.banking_service.dto.common.TransactionSummaryDTO(" +
            "COALESCE(SUM(CASE WHEN t.senderAccount.iban = :iban THEN t.amount ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN t.receiverAccount.iban = :iban THEN t.amount ELSE 0 END), 0), " +
            "COUNT(t)) " +
            "FROM TransactionEntity t WHERE t.senderAccount.iban = :iban OR t.receiverAccount.iban = :iban")
    TransactionSummaryDTO getTransactionSummary(@Param("iban") String iban);

    @Query("SELECT new com.batuhan.banking_service.dto.common.WeeklyTrendDTO(t.createdAt, SUM(t.amount)) " +
            "FROM TransactionEntity t " +
            "WHERE t.senderAccount.iban = :iban AND t.createdAt >= :startDate " +
            "GROUP BY t.createdAt " +
            "ORDER BY t.createdAt ASC")
    List<WeeklyTrendDTO> getWeeklySpendingTrend(@Param("iban") String iban, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT t.description, SUM(t.amount) " +
            "FROM TransactionEntity t " +
            "WHERE t.senderAccount.iban = :iban " +
            "GROUP BY t.description")
    List<Object[]> getRawCategoryData(@Param("iban") String iban);
}