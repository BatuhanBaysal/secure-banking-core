package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private static final String FROM_EMAIL = "no-reply@batuhanbanking.com";
    private static final String SUBJECT_PREFIX = "Transfer Notification - Ref: ";

    @Override
    public void sendTransferEmail(String toEmail, String name, BigDecimal amount, String receiverIban, String referenceNumber) {
        log.info("Preparing to send notification email for Ref: {}", referenceNumber);

        try {
            SimpleMailMessage message = prepareTransferMessage(toEmail, name, amount, receiverIban, referenceNumber);
            mailSender.send(message);
            log.info("Notification email successfully sent to: {} for Ref: {}", toEmail, referenceNumber);
        } catch (Exception e) {
            log.error("CRITICAL: Failed to send notification email for Ref {}. Reason: {}", referenceNumber, e.getMessage(), e);
        }
    }

    private SimpleMailMessage prepareTransferMessage(String toEmail, String name, BigDecimal amount, String receiverIban, String referenceNumber) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM_EMAIL);
        message.setTo(toEmail);
        message.setSubject(SUBJECT_PREFIX + referenceNumber);
        message.setText(buildTransferEmailBody(name, amount, receiverIban, referenceNumber));
        return message;
    }

    private String buildTransferEmailBody(String name, BigDecimal amount, String receiverIban, String referenceNumber) {
        return """
                Dear %s,
                
                A transfer of %s has been successfully made to IBAN %s.
                
                Reference Number: %s
                
                You can use this reference number for your inquiries. Have a nice day.
                
                Batuhan Banking Digital Services
                """.formatted(name, amount, receiverIban, referenceNumber);
    }
}