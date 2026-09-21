package africa.growtogether.platform.school.finance.statement;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class FinanceStudentAccountStatementDtos {

    private FinanceStudentAccountStatementDtos() {
    }

    public record AccountSummary(
            UUID studentId,
            UUID studentFinancialAccountId,
            String currencyCode,
            LocalDate asOfDate,
            BigDecimal totalBilled,
            BigDecimal totalPaid,
            BigDecimal totalOutstanding,
            BigDecimal arrearsOutstanding,
            BigDecimal unallocatedPaymentAmount,
            int invoiceCount,
            int overdueInvoiceCount
    ) {
        public AccountSummary {
            Objects.requireNonNull(studentId);
            Objects.requireNonNull(studentFinancialAccountId);
            Objects.requireNonNull(currencyCode);
            Objects.requireNonNull(asOfDate);
            Objects.requireNonNull(totalBilled);
            Objects.requireNonNull(totalPaid);
            Objects.requireNonNull(totalOutstanding);
            Objects.requireNonNull(arrearsOutstanding);
            Objects.requireNonNull(unallocatedPaymentAmount);
        }
    }

    public record StatementEntry(
            LocalDate effectiveDate,
            Instant occurredAt,
            String entryType,
            UUID sourceId,
            String reference,
            BigDecimal amount,
            String balanceEffect,
            UUID relatedInvoiceId,
            UUID relatedPaymentId,
            String lifecycleStatus
    ) {
        public StatementEntry {
            Objects.requireNonNull(effectiveDate);
            Objects.requireNonNull(entryType);
            Objects.requireNonNull(sourceId);
            Objects.requireNonNull(amount);
            Objects.requireNonNull(balanceEffect);
        }
    }

    public record AccountStatement(
            AccountSummary summary,
            LocalDate fromDate,
            LocalDate toDate,
            List<StatementEntry> entries
    ) {
        public AccountStatement {
            Objects.requireNonNull(summary);
            entries = List.copyOf(entries);
        }
    }

    public record ArrearsItem(
            UUID invoiceId,
            String invoiceNumber,
            LocalDate invoiceDate,
            LocalDate dueDate,
            BigDecimal outstandingAmount,
            long daysOverdue,
            String invoiceStatus
    ) {
        public ArrearsItem {
            Objects.requireNonNull(invoiceId);
            Objects.requireNonNull(invoiceDate);
            Objects.requireNonNull(dueDate);
            Objects.requireNonNull(outstandingAmount);
            Objects.requireNonNull(invoiceStatus);
        }
    }

    public record ArrearsView(
            UUID studentId,
            UUID studentFinancialAccountId,
            String currencyCode,
            LocalDate asOfDate,
            BigDecimal arrearsOutstanding,
            int overdueInvoiceCount,
            List<ArrearsItem> items
    ) {
        public ArrearsView {
            Objects.requireNonNull(studentId);
            Objects.requireNonNull(studentFinancialAccountId);
            Objects.requireNonNull(currencyCode);
            Objects.requireNonNull(asOfDate);
            Objects.requireNonNull(arrearsOutstanding);
            items = List.copyOf(items);
        }
    }
}
