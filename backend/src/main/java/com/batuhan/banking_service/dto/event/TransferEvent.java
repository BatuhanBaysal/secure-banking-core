package com.batuhan.banking_service.dto.event;

import java.math.BigDecimal;
import java.io.Serializable;

public record TransferEvent(
        String toEmail,
        String firstName,
        BigDecimal amount,
        String receiverIban,
        String referenceNumber
) implements Serializable {}