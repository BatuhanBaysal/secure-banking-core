package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.dto.common.TransactionCategoryDTO;
import com.batuhan.banking_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionAnalysisServiceImpl {

    private final TransactionRepository transactionRepository;

    public List<TransactionCategoryDTO> calculateCategoryAnalysis(String iban) {
        log.info("Analyzing transaction categories for IBAN: {}", iban);
        List<Object[]> rawData = transactionRepository.getRawCategoryData(iban);

        if (rawData == null || rawData.isEmpty()) {
            return List.of();
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        Map<String, BigDecimal> categoryMap = new HashMap<>();

        for (Object[] row : rawData) {
            String description = (row[0] != null) ? (String) row[0] : "";
            BigDecimal amount = (row[1] != null) ? (BigDecimal) row[1] : BigDecimal.ZERO;

            totalAmount = totalAmount.add(amount);

            String category = determineCategory(description);
            categoryMap.merge(category, amount, BigDecimal::add);
        }

        return convertToTransactionCategoryDTOs(categoryMap, totalAmount);
    }

    private List<TransactionCategoryDTO> convertToTransactionCategoryDTOs(Map<String, BigDecimal> categoryMap, BigDecimal totalAmount) {
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) return List.of();

        List<TransactionCategoryDTO> dtos = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : categoryMap.entrySet()) {
            double percentage = calculatePercentage(entry.getValue(), totalAmount);
            dtos.add(new TransactionCategoryDTO(entry.getKey(), entry.getValue(), percentage));
        }
        return dtos;
    }

    private double calculatePercentage(BigDecimal categoryAmount, BigDecimal totalAmount) {
        return categoryAmount
                .multiply(new BigDecimal("100"))
                .divide(totalAmount, 4, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private String determineCategory(String description) {
        if (description == null || description.isBlank()) {
            return "Other";
        }

        String lowerDesc = description.toLowerCase();

        if (lowerDesc.contains("rent") || lowerDesc.contains("housing") || lowerDesc.contains("kira")) return "Rent";
        if (lowerDesc.contains("market") || lowerDesc.contains("grocery") || lowerDesc.contains("shop") || lowerDesc.contains("migros")) return "Shopping";
        if (lowerDesc.contains("bill") || lowerDesc.contains("utility") || lowerDesc.contains("invoice") || lowerDesc.contains("fatura")) return "Bills";
        if (lowerDesc.contains("salary") || lowerDesc.contains("maaş")) return "Salary";
        if (lowerDesc.contains("restaurant") || lowerDesc.contains("food") || lowerDesc.contains("cafe") || lowerDesc.contains("yemek")) return "Dining";
        if (lowerDesc.contains("transfer") || lowerDesc.contains("eft") || lowerDesc.contains("fast")) return "Transfer";

        return "Other";
    }
}