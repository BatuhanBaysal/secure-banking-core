package com.batuhan.banking_service.controller;

import com.batuhan.banking_service.constant.Messages;
import com.batuhan.banking_service.dto.common.GlobalResponse;
import com.batuhan.banking_service.dto.common.TransactionCategoryDTO;
import com.batuhan.banking_service.dto.common.TransactionSummaryDTO;
import com.batuhan.banking_service.dto.common.WeeklyTrendDTO;
import com.batuhan.banking_service.dto.request.TransactionRequest;
import com.batuhan.banking_service.dto.response.TransactionResponse;
import com.batuhan.banking_service.service.ExcelService;
import com.batuhan.banking_service.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final ExcelService excelService;

    @PostMapping("/transfer")
    @PreAuthorize("@bankingBusinessValidator.isAccountOwner(#request.senderIban)")
    public ResponseEntity<GlobalResponse<TransactionResponse>> transferMoney(
            @Valid @RequestBody TransactionRequest request) {

        log.info("API Request: Transfer initiated from {} to {}", request.getSenderIban(), request.getReceiverIban());
        TransactionResponse response = transactionService.transferMoney(request);
        return ResponseEntity.ok(GlobalResponse.success(response, Messages.TRANSFER_SUCCESS));
    }

    @GetMapping("/history/{iban}")
    @PreAuthorize("hasRole('ADMIN') or @bankingBusinessValidator.isAccountOwner(#iban)")
    public ResponseEntity<GlobalResponse<Page<TransactionResponse>>> getTransactionHistory(
            @PathVariable String iban,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("API Request: Fetch transaction history for IBAN: {}", iban);
        Page<TransactionResponse> history = transactionService.getTransactionHistory(iban, pageable);
        return ResponseEntity.ok(GlobalResponse.success(history, Messages.HISTORY_RETRIEVED));
    }

    @GetMapping("/receipt/{id}")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long id) {
        log.info("API Request: Generating receipt for Transaction ID: {}", id);
        byte[] pdfContent = transactionService.generateTransactionReceipt(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt_" + id + ".pdf")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE)
                .body(pdfContent);
    }

    @GetMapping("/filter")
    @PreAuthorize("@bankingBusinessValidator.isAccountOwner(#iban)")
    public ResponseEntity<GlobalResponse<Page<TransactionResponse>>> filterTransactions(
            @RequestParam String iban,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("API Request: Filtering transactions for IBAN: {}", iban);
        Page<TransactionResponse> result = transactionService.filterTransactions(
                iban, minAmount, maxAmount, startDate, endDate, pageable);

        return ResponseEntity.ok(GlobalResponse.success(result, "Transactions filtered successfully"));
    }

    @GetMapping("/download/excel")
    @PreAuthorize("@bankingBusinessValidator.isAccountOwner(#iban)")
    public ResponseEntity<Resource> downloadTransactionsExcel(@RequestParam String iban) {
        log.info("Excel download requested for IBAN: {}", iban);
        List<TransactionResponse> transactions = transactionService.getAllTransactionsByIban(iban);
        ByteArrayInputStream in = excelService.transactionsToExcel(transactions);

        String filename = "transactions_" + iban + ".xlsx";
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

    @GetMapping("/dashboard/summary")
    @PreAuthorize("@bankingBusinessValidator.isAccountOwner(#iban)")
    public ResponseEntity<GlobalResponse<TransactionSummaryDTO>> getDashboardSummary(@RequestParam String iban) {
        TransactionSummaryDTO summary = transactionService.getDashboardSummary(iban);
        return ResponseEntity.ok(GlobalResponse.success(summary, "Dashboard summary retrieved"));
    }

    @GetMapping("/dashboard/trend")
    @PreAuthorize("@bankingBusinessValidator.isAccountOwner(#iban)")
    public ResponseEntity<GlobalResponse<List<WeeklyTrendDTO>>> getWeeklyTrend(@RequestParam String iban) {
        log.info("API Request: Fetching weekly trend for IBAN: {}", iban);
        List<WeeklyTrendDTO> trend = transactionService.getWeeklyTrend(iban);
        return ResponseEntity.ok(GlobalResponse.success(trend, "Weekly trend retrieved successfully"));
    }

    @GetMapping("/dashboard/categories")
    @PreAuthorize("@bankingBusinessValidator.isAccountOwner(#iban)")
    public ResponseEntity<GlobalResponse<List<TransactionCategoryDTO>>> getCategories(@RequestParam String iban) {
        log.info("API Request: Fetching category analysis for IBAN: {}", iban);
        List<TransactionCategoryDTO> analysis = transactionService.getCategoryAnalysis(iban);
        return ResponseEntity.ok(GlobalResponse.success(analysis, "Category analysis retrieved successfully"));
    }
}