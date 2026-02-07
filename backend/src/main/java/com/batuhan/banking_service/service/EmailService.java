package com.batuhan.banking_service.service;

import java.math.BigDecimal;

public interface EmailService {

    void sendTransferEmail(String to, String firstName, BigDecimal amount, String receiverIban, String ref);
}