package com.loanmanagement.service.report;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.loanmanagement.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating PDF reports
 */
@Service
@SuppressWarnings("unused")
public class PdfReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PdfReportGenerator.class);
    private static final DeviceRgb HEADER_BG = new DeviceRgb(51, 51, 51);
    private static final DeviceRgb HEADER_TEXT = new DeviceRgb(255, 255, 255);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    /**
     * Generate Loan Summary Report PDF
     */
    public byte[] generateLoanSummaryReport(List<Loan> loans, String title) {
        logger.info("Generating Loan Summary PDF Report");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            addTitle(document, title);
            addGeneratedDate(document);

            // Summary stats
            addParagraph(document, "Total Loans: " + loans.size());

            BigDecimal totalPrincipal = loans.stream()
                    .map(Loan::getPrincipalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            addParagraph(document, "Total Principal Amount: $" + totalPrincipal);

            // Loans table
            Table table = new Table(UnitValue.createPercentArray(new float[] { 1, 2, 2, 2, 2, 2 }));
            table.setWidth(UnitValue.createPercentValue(100));

            // Headers
            addTableHeader(table, "ID", "Borrower", "Principal", "Interest Rate", "Status", "Start Date");

            // Data rows
            for (Loan loan : loans) {
                addCell(table, loan.getId().toString());
                addCell(table,
                        loan.getBorrower() != null
                                ? loan.getBorrower().getFirstName() + " " + loan.getBorrower().getLastName()
                                : "N/A");
                addCell(table, "$" + loan.getPrincipalAmount());
                addCell(table, loan.getInterestRate() + "%");
                addCell(table, loan.getStatus().name());
                addCell(table, loan.getStartDate() != null ? loan.getStartDate().format(DATE_FORMAT) : "N/A");
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating loan summary PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    /**
     * Generate EMI Schedule Report PDF
     */
    public byte[] generateEMIScheduleReport(Loan loan, List<EMISchedule> schedules) {
        logger.info("Generating EMI Schedule PDF for loan: {}", loan.getId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Title
            addTitle(document, "EMI Schedule Report");
            addGeneratedDate(document);

            // Loan details
            addParagraph(document, "Loan ID: " + loan.getId());
            if (loan.getBorrower() != null) {
                addParagraph(document,
                        "Borrower: " + loan.getBorrower().getFirstName() + " " + loan.getBorrower().getLastName());
            }
            addParagraph(document, "Principal Amount: $" + loan.getPrincipalAmount());
            addParagraph(document, "Interest Rate: " + loan.getInterestRate() + "%");
            addParagraph(document, "Tenure: " + loan.getTermMonths() + " months");
            addParagraph(document, "Monthly EMI: $" + loan.getMonthlyPayment());
            addParagraph(document, "");

            // EMI table
            Table table = new Table(UnitValue.createPercentArray(new float[] { 1, 2, 2, 2, 2, 2 }));
            table.setWidth(UnitValue.createPercentValue(100));

            addTableHeader(table, "EMI #", "Due Date", "Principal", "Interest", "EMI Amount", "Status");

            for (EMISchedule emi : schedules) {
                addCell(table, emi.getEmiNumber().toString());
                addCell(table, emi.getDueDate().format(DATE_FORMAT));
                addCell(table, "$" + emi.getPrincipalComponent());
                addCell(table, "$" + emi.getInterestComponent());
                addCell(table, "$" + emi.getEmiAmount());
                addCell(table, emi.getStatus().name());
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating EMI schedule PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    /**
     * Generate Payment History Report PDF
     */
    public byte[] generatePaymentHistoryReport(List<Payment> payments, String borrowerName) {
        logger.info("Generating Payment History PDF Report");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            addTitle(document, "Payment History Report");
            addGeneratedDate(document);
            if (borrowerName != null) {
                addParagraph(document, "Borrower: " + borrowerName);
            }

            // Summary
            BigDecimal totalPaid = payments.stream()
                    .filter(p -> p.getAmountPaid() != null)
                    .map(Payment::getAmountPaid)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            addParagraph(document, "Total Payments: " + payments.size());
            addParagraph(document, "Total Amount Paid: $" + totalPaid);
            addParagraph(document, "");

            // Payments table
            Table table = new Table(UnitValue.createPercentArray(new float[] { 1, 2, 2, 2, 2, 2 }));
            table.setWidth(UnitValue.createPercentValue(100));

            addTableHeader(table, "#", "Due Date", "Amount Due", "Amount Paid", "Paid Date", "Status");

            for (Payment payment : payments) {
                addCell(table, payment.getPaymentNumber().toString());
                addCell(table, payment.getDueDate().format(DATE_FORMAT));
                addCell(table, "$" + payment.getAmountDue());
                addCell(table, payment.getAmountPaid() != null ? "$" + payment.getAmountPaid() : "-");
                addCell(table, payment.getPaidDate() != null ? payment.getPaidDate().format(DATE_FORMAT) : "-");
                addCell(table, payment.getStatus().name());
            }

            document.add(table);
            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating payment history PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    /**
     * Generate Monthly Summary Report PDF
     */
    public byte[] generateMonthlySummaryReport(int month, int year,
            long totalLoans, BigDecimal totalDisbursed, BigDecimal totalCollected,
            long overdueCount, BigDecimal overdueAmount) {
        logger.info("Generating Monthly Summary PDF for {}/{}", month, year);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            addTitle(document, String.format("Monthly Summary Report - %02d/%d", month, year));
            addGeneratedDate(document);

            addParagraph(document, "");
            addParagraph(document, "=== LOAN STATISTICS ===");
            addParagraph(document, "Total Active Loans: " + totalLoans);
            addParagraph(document, "Total Amount Disbursed: $" + totalDisbursed);
            addParagraph(document, "");
            addParagraph(document, "=== COLLECTION STATISTICS ===");
            addParagraph(document, "Total Amount Collected: $" + totalCollected);
            addParagraph(document, "");
            addParagraph(document, "=== OVERDUE STATISTICS ===");
            addParagraph(document, "Overdue Payments Count: " + overdueCount);
            addParagraph(document, "Overdue Amount: $" + overdueAmount);

            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating monthly summary PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    // Helper methods
    private void addTitle(Document document, String title) {
        Paragraph p = new Paragraph(title)
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(p);
    }

    private void addGeneratedDate(Document document) {
        Paragraph p = new Paragraph(
                "Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(p);
        document.add(new Paragraph(""));
    }

    private void addParagraph(Document document, String text) {
        document.add(new Paragraph(text).setFontSize(11));
    }

    private void addTableHeader(Table table, String... headers) {
        for (String header : headers) {
            Cell cell = new Cell()
                    .add(new Paragraph(header).setBold())
                    .setBackgroundColor(HEADER_BG)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(cell);
        }
    }

    private void addCell(Table table, String value) {
        table.addCell(new Cell().add(new Paragraph(value)).setFontSize(10));
    }
}
