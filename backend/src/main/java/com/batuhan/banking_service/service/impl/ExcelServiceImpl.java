package com.batuhan.banking_service.service.impl;

import com.batuhan.banking_service.dto.response.TransactionResponse;
import com.batuhan.banking_service.exception.BankingServiceException;
import com.batuhan.banking_service.service.ExcelService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class ExcelServiceImpl implements ExcelService {

    private static final String[] COLUMNS = {"Reference Number", "Sender Name", "Sender IBAN", "Receiver Name", "Receiver IBAN", "Amount", "Date", "Status"};
    private static final String SHEET_NAME = "Transaction Report";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public ByteArrayInputStream transactionsToExcel(List<TransactionResponse> transactions) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET_NAME);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle amountStyle = createAmountStyle(workbook);

            createHeaderRow(sheet, headerStyle);
            fillTransactionData(sheet, transactions, dataStyle, amountStyle);
            autoSizeColumns(sheet);

            workbook.write(out);
            log.info("Excel report generated successfully with {} rows", transactions.size());
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Excel generation failed for {} transactions: {}", transactions.size(), e.getMessage());
            throw new BankingServiceException("Failed to generate Excel report", HttpStatus.INTERNAL_SERVER_ERROR);
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
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    private CellStyle createAmountStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
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

    private void fillTransactionData(Sheet sheet, List<TransactionResponse> transactions, CellStyle dataStyle, CellStyle amountStyle) {
        int rowIdx = 1;
        for (TransactionResponse dto : transactions) {
            Row row = sheet.createRow(rowIdx++);

            createCell(row, 0, dto.referenceNumber(), dataStyle);
            createCell(row, 1, dto.senderName(), dataStyle);
            createCell(row, 2, dto.senderIban(), dataStyle);
            createCell(row, 3, dto.receiverName(), dataStyle);
            createCell(row, 4, dto.receiverIban(), dataStyle);

            Cell amountCell = row.createCell(5);
            amountCell.setCellValue(dto.amount() != null ? dto.amount().doubleValue() : 0.0);
            amountCell.setCellStyle(amountStyle);

            String formattedDate = dto.createdAt() != null ? dto.createdAt().format(DATE_FORMATTER) : "-";
            createCell(row, 6, formattedDate, dataStyle);

            createCell(row, 7, dto.status() != null ? dto.status().toString() : "-", dataStyle);
        }
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "-");
        cell.setCellStyle(style);
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < COLUMNS.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}