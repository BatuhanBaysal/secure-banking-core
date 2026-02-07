package com.batuhan.banking_service.service;

import com.batuhan.banking_service.entity.TransactionEntity;
import java.io.ByteArrayInputStream;

public interface PdfService {

    ByteArrayInputStream generateTransactionReceipt(TransactionEntity transaction);
}