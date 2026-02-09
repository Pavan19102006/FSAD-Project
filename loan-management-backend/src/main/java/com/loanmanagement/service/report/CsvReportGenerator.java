package com.loanmanagement.service.report;

import com.loanmanagement.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service for generating CSV reports
 */
@Service
public class CsvReportGenerator {

    private static final Logger logger = LoggerFactory.getLogger(CsvReportGenerator.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Generate Loan Summary CSV
     */
    public byte[] generateLoanSummaryReport(List<Loan> loans) {
        logger.info("Generating Loan Summary CSV Report");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(baos)) {

            // Header
            writer.println(
                    "Loan ID,Borrower,Lender,Principal,Interest Rate,Term (Months),Monthly EMI,Total Interest,Status,Start Date,End Date");

            // Data
            for (Loan loan : loans) {
                writer.println(String.format("%d,%s,%s,%.2f,%.2f,%d,%.2f,%.2f,%s,%s,%s",
                        loan.getId(),
                        escapeCsv(loan.getBorrower() != null
                                ? loan.getBorrower().getFirstName() + " " + loan.getBorrower().getLastName()
                                : "N/A"),
                        escapeCsv(loan.getLender() != null
                                ? loan.getLender().getFirstName() + " " + loan.getLender().getLastName()
                                : "N/A"),
                        loan.getPrincipalAmount(),
                        loan.getInterestRate(),
                        loan.getTermMonths(),
                        loan.getMonthlyPayment() != null ? loan.getMonthlyPayment() : BigDecimal.ZERO,
                        loan.getTotalInterest() != null ? loan.getTotalInterest() : BigDecimal.ZERO,
                        loan.getStatus().name(),
                        loan.getStartDate() != null ? loan.getStartDate().format(DATE_FORMAT) : "",
                        loan.getEndDate() != null ? loan.getEndDate().format(DATE_FORMAT) : ""));
            }

            writer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating loan summary CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate CSV report", e);
        }
    }

    /**
     * Generate EMI Schedule CSV
     */
    public byte[] generateEMIScheduleReport(Loan loan, List<EMISchedule> schedules) {
        logger.info("Generating EMI Schedule CSV for loan: {}", loan.getId());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(baos)) {

            // Loan info
            writer.println("# Loan ID: " + loan.getId());
            writer.println("# Principal: $" + loan.getPrincipalAmount());
            writer.println("# Interest Rate: " + loan.getInterestRate() + "%");
            writer.println("# Monthly EMI: $" + loan.getMonthlyPayment());
            writer.println();

            // Header
            writer.println("EMI #,Due Date,Principal,Interest,EMI Amount,Penalty,Amount Paid,Outstanding,Status");

            // Data
            for (EMISchedule emi : schedules) {
                writer.println(String.format("%d,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%s",
                        emi.getEmiNumber(),
                        emi.getDueDate().format(DATE_FORMAT),
                        emi.getPrincipalComponent(),
                        emi.getInterestComponent(),
                        emi.getEmiAmount(),
                        emi.getPenaltyAmount() != null ? emi.getPenaltyAmount() : BigDecimal.ZERO,
                        emi.getAmountPaid() != null ? emi.getAmountPaid() : BigDecimal.ZERO,
                        emi.getOutstandingPrincipal(),
                        emi.getStatus().name()));
            }

            writer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating EMI schedule CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate CSV report", e);
        }
    }

    /**
     * Generate Payment History CSV
     */
    public byte[] generatePaymentHistoryReport(List<Payment> payments) {
        logger.info("Generating Payment History CSV Report");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(baos)) {

            // Header
            writer.println(
                    "Payment #,Loan ID,Due Date,Amount Due,Principal,Interest,Late Fee,Amount Paid,Paid Date,Status");

            // Data
            for (Payment payment : payments) {
                writer.println(String.format("%d,%d,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%s,%s",
                        payment.getPaymentNumber(),
                        payment.getLoan().getId(),
                        payment.getDueDate().format(DATE_FORMAT),
                        payment.getAmountDue(),
                        payment.getPrincipalPortion() != null ? payment.getPrincipalPortion() : BigDecimal.ZERO,
                        payment.getInterestPortion() != null ? payment.getInterestPortion() : BigDecimal.ZERO,
                        payment.getLateFee() != null ? payment.getLateFee() : BigDecimal.ZERO,
                        payment.getAmountPaid() != null ? payment.getAmountPaid() : BigDecimal.ZERO,
                        payment.getPaidDate() != null ? payment.getPaidDate().format(DATE_FORMAT) : "",
                        payment.getStatus().name()));
            }

            writer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating payment history CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate CSV report", e);
        }
    }

    /**
     * Generate Transaction Ledger CSV
     */
    public byte[] generateTransactionLedger(List<Transaction> transactions) {
        logger.info("Generating Transaction Ledger CSV");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(baos)) {

            // Header
            writer.println("Transaction ID,Loan ID,Type,Amount,Description,Created At");

            // Data
            for (Transaction tx : transactions) {
                writer.println(String.format("%d,%d,%s,%.2f,%s,%s",
                        tx.getId(),
                        tx.getLoan().getId(),
                        tx.getType().name(),
                        tx.getAmount(),
                        escapeCsv(tx.getDescription() != null ? tx.getDescription() : ""),
                        tx.getCreatedAt() != null ? tx.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                                : ""));
            }

            writer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Error generating transaction ledger CSV: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate CSV report", e);
        }
    }

    // Escape CSV special characters
    private String escapeCsv(String value) {
        if (value == null)
            return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
