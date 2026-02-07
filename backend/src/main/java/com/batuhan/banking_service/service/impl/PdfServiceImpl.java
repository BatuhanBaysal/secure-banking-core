package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.entity.TransactionEntity;
import com.batuhan.banking_service.service.PdfService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfServiceImpl implements PdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public ByteArrayInputStream generateTransactionReceipt(TransactionEntity transaction) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            addTitle(document);
            addTransactionDetailsTable(document, transaction);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error while creating PDF: " + e.getMessage());
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addTitle(Document document) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Paragraph title = new Paragraph("BANK TRANSACTION RECEIPT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
    }

    private void addTransactionDetailsTable(Document document, TransactionEntity transaction) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        fillTableData(table, transaction, headerFont, bodyFont);
        document.add(table);
    }

    private void fillTableData(PdfPTable table, TransactionEntity transaction, Font header, Font body) {
        addCellToTable(table, "Transaction ID", String.valueOf(transaction.getId()), header, body);
        addCellToTable(table, "Date", transaction.getCreatedAt().format(DATE_FORMATTER), header, body);

        String senderName = transaction.getSenderAccount().getUser().getFirstName() + " " + transaction.getSenderAccount().getUser().getLastName();
        addCellToTable(table, "Sender Name", senderName, header, body);
        addCellToTable(table, "Sender IBAN", transaction.getSenderAccount().getIban(), header, body);

        String receiverName = transaction.getReceiverAccount().getUser().getFirstName() + " " + transaction.getReceiverAccount().getUser().getLastName();
        addCellToTable(table, "Receiver Name", receiverName, header, body);
        addCellToTable(table, "Receiver IBAN", transaction.getReceiverAccount().getIban(), header, body);

        String amountWithCurrency = transaction.getAmount().toString() + " " + transaction.getSenderAccount().getCurrency().name();
        addCellToTable(table, "Amount", amountWithCurrency, header, body);

        addCellToTable(table, "Description", transaction.getDescription(), header, body);
        addCellToTable(table, "Reference Number", transaction.getReferenceNumber(), header, body);
    }

    private void addCellToTable(PdfPTable table, String label, String value, Font headerFont, Font bodyFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, headerFont));
        labelCell.setPadding(8);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "-", bodyFont));
        valueCell.setPadding(8);
        table.addCell(valueCell);
    }
}