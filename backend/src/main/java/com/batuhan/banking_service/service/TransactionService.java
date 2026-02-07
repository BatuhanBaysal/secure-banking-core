package com.batuhan.banking_service.service;

import com.batuhan.banking_service.dto.common.TransactionCategoryDTO;
import com.batuhan.banking_service.dto.common.TransactionSummaryDTO;
import com.batuhan.banking_service.dto.common.WeeklyTrendDTO;
import com.batuhan.banking_service.dto.request.TransactionRequest;
import com.batuhan.banking_service.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {

    TransactionResponse transferMoney(TransactionRequest request);
    Page<TransactionResponse> getTransactionHistory(String iban, Pageable pageable);
    List<TransactionResponse> getAllTransactionsByIban(String iban);
    TransactionSummaryDTO getDashboardSummary(String iban);
    List<WeeklyTrendDTO> getWeeklyTrend(String iban);
    List<TransactionCategoryDTO> getCategoryAnalysis(String iban);
    byte[] generateTransactionReceipt(Long id);
    Page<TransactionResponse> filterTransactions(
            String iban,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );
}