package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.entity.TransactionEntity;
import com.batuhan.banking_service.service.PdfService;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class PdfServiceImpl implements PdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font BODY_FONT = FontFactory.getFont(FontFactory.HELVETICA, 12);

    @Override
    public ByteArrayInputStream generateTransactionReceipt(TransactionEntity transaction) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();
            addTitle(document);
            addTransactionDetailsTable(document, transaction);
            addFooter(document);
            document.close();
        } catch (DocumentException e) {
            log.error("PDF Document error for TX {}: {}", transaction.getReferenceNumber(), e.getMessage());
            throw new RuntimeException("Error while creating PDF: " + e.getMessage());
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void addTitle(Document document) throws DocumentException {
        Paragraph title = new Paragraph("BANK TRANSACTION RECEIPT", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(25);
        document.add(title);
    }

    private void addTransactionDetailsTable(Document document, TransactionEntity transaction) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        fillTableData(table, transaction);
        document.add(table);
    }

    private void fillTableData(PdfPTable table, TransactionEntity transaction) {
        addCellToTable(table, "Transaction Reference", transaction.getReferenceNumber());
        addCellToTable(table, "Date", transaction.getCreatedAt().format(DATE_FORMATTER));

        String senderName = transaction.getSenderAccount().getUser().getFirstName() + " " +
                transaction.getSenderAccount().getUser().getLastName();
        addCellToTable(table, "Sender Name", senderName);
        addCellToTable(table, "Sender IBAN", transaction.getSenderAccount().getIban());

        String receiverName = transaction.getReceiverAccount().getUser().getFirstName() + " " +
                transaction.getReceiverAccount().getUser().getLastName();
        addCellToTable(table, "Receiver Name", receiverName);
        addCellToTable(table, "Receiver IBAN", transaction.getReceiverAccount().getIban());

        String amountWithCurrency = transaction.getAmount().toString() + " " +
                transaction.getSenderAccount().getCurrency().name();
        addCellToTable(table, "Amount", amountWithCurrency);
        addCellToTable(table, "Description", transaction.getDescription());
        addCellToTable(table, "Status", transaction.getStatus().name());
    }

    private void addCellToTable(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADER_FONT));
        labelCell.setPadding(10);
        labelCell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "-", BODY_FONT));
        valueCell.setPadding(10);
        table.addCell(valueCell);
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph("\nThis is a computer-generated receipt and does not require a physical signature.",
                FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
    }
}