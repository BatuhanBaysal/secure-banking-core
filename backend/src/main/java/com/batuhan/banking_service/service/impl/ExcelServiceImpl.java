package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.dto.response.TransactionResponse;
import com.batuhan.banking_service.service.ExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.List;

@Service
public class ExcelServiceImpl implements ExcelService {

    private static final String[] COLUMNS = {"Reference Number", "Sender Name", "Sender IBAN", "Receiver Name", "Receiver IBAN", "Amount", "Date", "Status"};
    private static final String SHEET_NAME = "Transaction Report";

    @Override
    public ByteArrayInputStream transactionsToExcel(List<TransactionResponse> transactions) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet(SHEET_NAME);

            CellStyle headerStyle = createHeaderStyle(workbook);
            createHeaderRow(sheet, headerStyle);
            fillTransactionData(sheet, transactions);
            autoSizeColumns(sheet);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Excel generation failed: " + e.getMessage());
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());

        CellStyle style = workbook.createCellStyle();
        style.setFont(headerFont);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void createHeaderRow(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int col = 0; col < COLUMNS.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(COLUMNS[col]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void fillTransactionData(Sheet sheet, List<TransactionResponse> transactions) {
        int rowIdx = 1;
        for (TransactionResponse dto : transactions) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(dto.getReferenceNumber());
            row.createCell(1).setCellValue(dto.getSenderName());
            row.createCell(2).setCellValue(dto.getSenderIban());
            row.createCell(3).setCellValue(dto.getReceiverName());
            row.createCell(4).setCellValue(dto.getReceiverIban());
            row.createCell(5).setCellValue(dto.getAmount().doubleValue());
            row.createCell(6).setCellValue(dto.getCreatedAt().toString());
            row.createCell(7).setCellValue(dto.getStatus().toString());
        }
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < COLUMNS.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}