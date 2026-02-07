package com.batuhan.banking_service.service;

import com.batuhan.banking_service.dto.response.TransactionResponse;
import java.io.ByteArrayInputStream;
import java.util.List;

public interface ExcelService {

    ByteArrayInputStream transactionsToExcel(List<TransactionResponse> transactions);
}