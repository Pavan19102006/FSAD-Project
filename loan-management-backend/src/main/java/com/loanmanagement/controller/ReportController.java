package com.loanmanagement.controller;

import com.loanmanagement.entity.*;
import com.loanmanagement.repository.EMIScheduleRepository;
import com.loanmanagement.repository.LoanRepository;
import com.loanmanagement.repository.PaymentRepository;
import com.loanmanagement.repository.TransactionRepository;
import com.loanmanagement.service.report.CsvReportGenerator;
import com.loanmanagement.service.report.ExcelReportGenerator;
import com.loanmanagement.service.report.PdfReportGenerator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * REST API for generating and downloading reports
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'LENDER', 'ANALYST')")
@SuppressWarnings("null")
public class ReportController {

    private final PdfReportGenerator pdfGenerator;
    private final ExcelReportGenerator excelGenerator;
    private final CsvReportGenerator csvGenerator;
    private final LoanRepository loanRepository;
    private final EMIScheduleRepository emiScheduleRepository;
    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;

    public ReportController(PdfReportGenerator pdfGenerator,
            ExcelReportGenerator excelGenerator,
            CsvReportGenerator csvGenerator,
            LoanRepository loanRepository,
            EMIScheduleRepository emiScheduleRepository,
            PaymentRepository paymentRepository,
            TransactionRepository transactionRepository) {
        this.pdfGenerator = pdfGenerator;
        this.excelGenerator = excelGenerator;
        this.csvGenerator = csvGenerator;
        this.loanRepository = loanRepository;
        this.emiScheduleRepository = emiScheduleRepository;
        this.paymentRepository = paymentRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Download loan summary report
     */
    @GetMapping("/loans")
    public ResponseEntity<byte[]> downloadLoanSummary(
            @RequestParam(defaultValue = "pdf") String format,
            @RequestParam(required = false) String status) {

        List<Loan> loans;
        if (status != null && !status.isEmpty()) {
            loans = loanRepository.findByStatus(LoanStatus.valueOf(status.toUpperCase()));
        } else {
            loans = loanRepository.findAll();
        }

        String title = "Loan Summary Report - " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filename = "loan_summary_" + LocalDate.now();

        return generateReport(format, filename, () -> {
            switch (format.toLowerCase()) {
                case "excel":
                case "xlsx":
                    return excelGenerator.generateLoanSummaryReport(loans, title);
                case "csv":
                    return csvGenerator.generateLoanSummaryReport(loans);
                default:
                    return pdfGenerator.generateLoanSummaryReport(loans, title);
            }
        });
    }

    /**
     * Download EMI schedule report for a loan
     */
    @GetMapping("/emi-schedule/{loanId}")
    public ResponseEntity<byte[]> downloadEMISchedule(
            @PathVariable Long loanId,
            @RequestParam(defaultValue = "pdf") String format) {

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
        List<EMISchedule> schedules = emiScheduleRepository.findByLoanIdOrderByEmiNumber(loanId);

        String filename = "emi_schedule_loan_" + loanId;

        return generateReport(format, filename, () -> {
            switch (format.toLowerCase()) {
                case "excel":
                case "xlsx":
                    return excelGenerator.generateEMIScheduleReport(loan, schedules);
                case "csv":
                    return csvGenerator.generateEMIScheduleReport(loan, schedules);
                default:
                    return pdfGenerator.generateEMIScheduleReport(loan, schedules);
            }
        });
    }

    /**
     * Download payment history report
     */
    @GetMapping("/payments")
    public ResponseEntity<byte[]> downloadPaymentHistory(
            @RequestParam(required = false) Long loanId,
            @RequestParam(defaultValue = "pdf") String format) {

        List<Payment> payments;
        String borrowerName = null;

        if (loanId != null) {
            Loan loan = loanRepository.findById(loanId)
                    .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
            payments = paymentRepository.findByLoanOrderByPaymentNumberAsc(loan);
            if (loan.getBorrower() != null) {
                borrowerName = loan.getBorrower().getFirstName() + " " + loan.getBorrower().getLastName();
            }
        } else {
            payments = paymentRepository.findAll();
        }

        String filename = "payment_history_" + LocalDate.now();
        final String finalBorrowerName = borrowerName;

        return generateReport(format, filename, () -> {
            switch (format.toLowerCase()) {
                case "excel":
                case "xlsx":
                    return excelGenerator.generatePaymentHistoryReport(payments, finalBorrowerName);
                case "csv":
                    return csvGenerator.generatePaymentHistoryReport(payments);
                default:
                    return pdfGenerator.generatePaymentHistoryReport(payments, finalBorrowerName);
            }
        });
    }

    /**
     * Download transaction ledger
     */
    @GetMapping("/transactions")
    public ResponseEntity<byte[]> downloadTransactionLedger(
            @RequestParam(required = false) Long loanId,
            @RequestParam(defaultValue = "csv") String format) {

        List<Transaction> transactions;
        if (loanId != null) {
            transactions = transactionRepository.findByLoanId(loanId);
        } else {
            transactions = transactionRepository.findAll();
        }

        String filename = "transaction_ledger_" + LocalDate.now();

        return generateReport(format, filename, () -> csvGenerator.generateTransactionLedger(transactions));
    }

    /**
     * Download overdue report
     */
    @GetMapping("/overdue")
    public ResponseEntity<byte[]> downloadOverdueReport(@RequestParam(defaultValue = "pdf") String format) {
        List<Payment> overduePayments = paymentRepository.findOverduePayments(LocalDate.now());

        String filename = "overdue_report_" + LocalDate.now();

        return generateReport(format, filename, () -> {
            switch (format.toLowerCase()) {
                case "excel":
                case "xlsx":
                    return excelGenerator.generatePaymentHistoryReport(overduePayments, "Overdue Payments");
                case "csv":
                    return csvGenerator.generatePaymentHistoryReport(overduePayments);
                default:
                    return pdfGenerator.generatePaymentHistoryReport(overduePayments, "Overdue Payments");
            }
        });
    }

    // Helper to build response
    private ResponseEntity<byte[]> generateReport(String format, String filename, ReportSupplier supplier) {
        byte[] content = supplier.get();

        MediaType mediaType;
        String extension;

        switch (format.toLowerCase()) {
            case "excel":
            case "xlsx":
                mediaType = MediaType
                        .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                extension = ".xlsx";
                break;
            case "csv":
                mediaType = MediaType.parseMediaType("text/csv");
                extension = ".csv";
                break;
            default:
                mediaType = MediaType.APPLICATION_PDF;
                extension = ".pdf";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + extension + "\"")
                .contentType(mediaType)
                .contentLength(content.length)
                .body(content);
    }

    @FunctionalInterface
    interface ReportSupplier {
        byte[] get();
    }
}
