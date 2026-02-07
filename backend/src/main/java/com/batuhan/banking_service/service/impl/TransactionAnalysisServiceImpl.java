package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.dto.common.TransactionCategoryDTO;
import com.batuhan.banking_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionAnalysisServiceImpl {

    private final TransactionRepository transactionRepository;

    public List<TransactionCategoryDTO> calculateCategoryAnalysis(String iban) {
        List<Object[]> rawData = transactionRepository.getRawCategoryData(iban);

        BigDecimal totalAmount = calculateTotalAmount(rawData);
        Map<String, BigDecimal> categoryMap = groupDataByCategory(rawData);

        return convertToTransactionCategoryDTOs(categoryMap, totalAmount);
    }

    private BigDecimal calculateTotalAmount(List<Object[]> rawData) {
        return rawData.stream()
                .map(row -> (BigDecimal) row[1])
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, BigDecimal> groupDataByCategory(List<Object[]> rawData) {
        Map<String, BigDecimal> categoryMap = new HashMap<>();
        for (Object[] row : rawData) {
            String description = ((String) row[0]).toLowerCase();
            BigDecimal amount = (BigDecimal) row[1];

            String category = determineCategory(description);
            categoryMap.merge(category, amount, BigDecimal::add);
        }

        return categoryMap;
    }

    private List<TransactionCategoryDTO> convertToTransactionCategoryDTOs(Map<String, BigDecimal> categoryMap, BigDecimal totalAmount) {
        return categoryMap.entrySet().stream()
                .map(entry -> {
                    double percentage = calculatePercentage(entry.getValue(), totalAmount);
                    return new TransactionCategoryDTO(entry.getKey(), entry.getValue(), percentage);
                })
                .collect(Collectors.toList());
    }

    private double calculatePercentage(BigDecimal categoryAmount, BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0.0;
        }

        return (categoryAmount.doubleValue() / totalAmount.doubleValue()) * 100;
    }

    private String determineCategory(String description) {
        if (description.contains("rent")) return "Rent";
        if (description.contains("market") || description.contains("grocery")) return "Shopping";
        if (description.contains("bill") || description.contains("utility")) return "Bills";
        if (description.contains("salary")) return "Salary";

        return "Other";
    }
}