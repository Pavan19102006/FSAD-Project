package com.loanmanagement.service.report;

import com.loanmanagement.entity.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating Excel reports
 */
@Service
public class ExcelReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ExcelReportGenerator.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    /**
     * Generate Loan Summary Report Excel
     */
    public byte[] generateLoanSummaryReport(List<Loan> loans, String title) {
        logger.info("Generating Loan Summary Excel Report");

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Loan Summary");
            CellStyle headerStyle = createHeaderStyle(workbook);
            int rowNum = 0;

            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);
            rowNum++;

            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = { "Loan ID", "Borrower", "Lender", "Principal", "Interest Rate",
                    "Term (Months)", "Monthly EMI", "Status", "Start Date", "End Date" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (Loan loan : loans) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(loan.getId());
                row.createCell(1)
                        .setCellValue(loan.getBorrower() != null
                                ? loan.getBorrower().getFirstName() + " " + loan.getBorrower().getLastName()
                                : "N/A");
                row.createCell(2)
                        .setCellValue(loan.getLender() != null
                                ? loan.getLender().getFirstName() + " " + loan.getLender().getLastName()
                                : "N/A");
                row.createCell(3).setCellValue(loan.getPrincipalAmount().doubleValue());
                row.createCell(4).setCellValue(loan.getInterestRate().doubleValue());
                row.createCell(5).setCellValue(loan.getTermMonths());
                row.createCell(6)
                        .setCellValue(loan.getMonthlyPayment() != null ? loan.getMonthlyPayment().doubleValue() : 0);
                row.createCell(7).setCellValue(loan.getStatus().name());
                row.createCell(8)
                        .setCellValue(loan.getStartDate() != null ? loan.getStartDate().format(DATE_FORMAT) : "");
                row.createCell(9).setCellValue(loan.getEndDate() != null ? loan.getEndDate().format(DATE_FORMAT) : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating loan summary Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    /**
     * Generate EMI Schedule Report Excel
     */
    public byte[] generateEMIScheduleReport(Loan loan, List<EMISchedule> schedules) {
        logger.info("Generating EMI Schedule Excel for loan: {}", loan.getId());

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("EMI Schedule");
            CellStyle headerStyle = createHeaderStyle(workbook);
            int rowNum = 0;

            // Loan details
            Row infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Loan ID:");
            infoRow.createCell(1).setCellValue(loan.getId());

            infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Principal:");
            infoRow.createCell(1).setCellValue("$" + loan.getPrincipalAmount());

            infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Interest Rate:");
            infoRow.createCell(1).setCellValue(loan.getInterestRate() + "%");

            infoRow = sheet.createRow(rowNum++);
            infoRow.createCell(0).setCellValue("Monthly EMI:");
            infoRow.createCell(1).setCellValue("$" + loan.getMonthlyPayment());
            rowNum++;

            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = { "EMI #", "Due Date", "Principal", "Interest", "EMI Amount",
                    "Penalty", "Amount Paid", "Outstanding", "Status" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (EMISchedule emi : schedules) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(emi.getEmiNumber());
                row.createCell(1).setCellValue(emi.getDueDate().format(DATE_FORMAT));
                row.createCell(2).setCellValue(emi.getPrincipalComponent().doubleValue());
                row.createCell(3).setCellValue(emi.getInterestComponent().doubleValue());
                row.createCell(4).setCellValue(emi.getEmiAmount().doubleValue());
                row.createCell(5)
                        .setCellValue(emi.getPenaltyAmount() != null ? emi.getPenaltyAmount().doubleValue() : 0);
                row.createCell(6).setCellValue(emi.getAmountPaid() != null ? emi.getAmountPaid().doubleValue() : 0);
                row.createCell(7).setCellValue(emi.getOutstandingPrincipal().doubleValue());
                row.createCell(8).setCellValue(emi.getStatus().name());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating EMI schedule Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    /**
     * Generate Payment History Report Excel
     */
    public byte[] generatePaymentHistoryReport(List<Payment> payments, String borrowerName) {
        logger.info("Generating Payment History Excel Report");

        try (Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Payment History");
            CellStyle headerStyle = createHeaderStyle(workbook);
            int rowNum = 0;

            // Title
            if (borrowerName != null) {
                Row infoRow = sheet.createRow(rowNum++);
                infoRow.createCell(0).setCellValue("Borrower:");
                infoRow.createCell(1).setCellValue(borrowerName);
                rowNum++;
            }

            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = { "Payment #", "Loan ID", "Due Date", "Amount Due",
                    "Principal", "Interest", "Late Fee", "Amount Paid", "Paid Date", "Status" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (Payment payment : payments) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(payment.getPaymentNumber());
                row.createCell(1).setCellValue(payment.getLoan().getId());
                row.createCell(2).setCellValue(payment.getDueDate().format(DATE_FORMAT));
                row.createCell(3).setCellValue(payment.getAmountDue().doubleValue());
                row.createCell(4).setCellValue(
                        payment.getPrincipalPortion() != null ? payment.getPrincipalPortion().doubleValue() : 0);
                row.createCell(5).setCellValue(
                        payment.getInterestPortion() != null ? payment.getInterestPortion().doubleValue() : 0);
                row.createCell(6).setCellValue(payment.getLateFee() != null ? payment.getLateFee().doubleValue() : 0);
                row.createCell(7)
                        .setCellValue(payment.getAmountPaid() != null ? payment.getAmountPaid().doubleValue() : 0);
                row.createCell(8)
                        .setCellValue(payment.getPaidDate() != null ? payment.getPaidDate().format(DATE_FORMAT) : "");
                row.createCell(9).setCellValue(payment.getStatus().name());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating payment history Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    // Helper method to create header style
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}
