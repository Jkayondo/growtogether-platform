package africa.growtogether.platform.school.finance.statement;

import static africa.growtogether.platform.school.finance.statement.FinanceStudentAccountStatementDtos.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FinanceStudentAccountStatementService {

    private final FinanceStudentAccountStatementJdbcRepository repository;
    private final Clock clock;

    @Autowired
    public FinanceStudentAccountStatementService(
            FinanceStudentAccountStatementJdbcRepository repository
    ) {
        this(
                repository,
                Clock.systemUTC()
        );
    }

    FinanceStudentAccountStatementService(
            FinanceStudentAccountStatementJdbcRepository repository,
            Clock clock
    ) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public AccountSummary accountSummary(
            UUID tenantId,
            UUID studentId,
            String currencyCode,
            LocalDate asOfDate
    ) {

        UUID safeTenant = required(tenantId, "tenantId");
        UUID safeStudent = required(studentId, "studentId");
        String currency = normalizeCurrency(currencyCode);
        LocalDate effectiveAsOf = resolveAsOf(asOfDate);

        FinanceStudentAccountStatementJdbcRepository.AccountScope account =
                account(
                        safeTenant,
                        safeStudent,
                        currency
                );

        FinanceStudentAccountStatementJdbcRepository.SummaryNumbers numbers =
                repository.loadSummaryNumbers(
                        safeTenant,
                        safeStudent,
                        currency,
                        effectiveAsOf
                );

        BigDecimal unallocated =
                repository.loadUnallocatedPaymentAmount(
                        safeTenant,
                        safeStudent,
                        currency
                );

        return new AccountSummary(
                safeStudent,
                account.accountId(),
                currency,
                effectiveAsOf,
                numbers.totalBilled(),
                numbers.totalPaid(),
                numbers.totalOutstanding(),
                numbers.arrearsOutstanding(),
                unallocated,
                numbers.invoiceCount(),
                numbers.overdueInvoiceCount()
        );
    }

    @Transactional(readOnly = true)
    public ArrearsView arrears(
            UUID tenantId,
            UUID studentId,
            String currencyCode,
            LocalDate asOfDate
    ) {

        AccountSummary summary =
                accountSummary(
                        tenantId,
                        studentId,
                        currencyCode,
                        asOfDate
                );

        List<ArrearsItem> items =
                repository.listArrears(
                        tenantId,
                        studentId,
                        summary.currencyCode(),
                        summary.asOfDate()
                );

        return new ArrearsView(
                summary.studentId(),
                summary.studentFinancialAccountId(),
                summary.currencyCode(),
                summary.asOfDate(),
                summary.arrearsOutstanding(),
                items.size(),
                items
        );
    }

    @Transactional(readOnly = true)
    public AccountStatement accountStatement(
            UUID tenantId,
            UUID studentId,
            String currencyCode,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate asOfDate
    ) {

        if (fromDate != null
                && toDate != null
                && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "fromDate must not be after toDate."
            );
        }

        AccountSummary summary =
                accountSummary(
                        tenantId,
                        studentId,
                        currencyCode,
                        asOfDate
                );

        List<StatementEntry> entries =
                repository.listStatementEntries(
                        tenantId,
                        studentId,
                        summary.currencyCode(),
                        fromDate,
                        toDate
                );

        return new AccountStatement(
                summary,
                fromDate,
                toDate,
                entries
        );
    }

    private FinanceStudentAccountStatementJdbcRepository.AccountScope account(
            UUID tenantId,
            UUID studentId,
            String currencyCode
    ) {
        return repository.findAccountScope(
                tenantId,
                studentId,
                currencyCode
        ).orElseThrow(
                () -> new IllegalArgumentException(
                        "Student financial account is not available "
                                + "in this tenant and currency."
                )
        );
    }

    private LocalDate resolveAsOf(
            LocalDate asOfDate
    ) {
        return asOfDate == null
                ? LocalDate.now(clock)
                : asOfDate;
    }

    private String normalizeCurrency(
            String currencyCode
    ) {

        if (currencyCode == null
                || currencyCode.isBlank()) {
            throw new IllegalArgumentException(
                    "currencyCode must not be blank."
            );
        }

        String normalized =
                currencyCode.trim()
                        .toUpperCase(Locale.ROOT);

        if (normalized.length() != 3) {
            throw new IllegalArgumentException(
                    "currencyCode must contain exactly three characters."
            );
        }

        return normalized;
    }

    private UUID required(
            UUID value,
            String name
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    name + " must not be null."
            );
        }
        return value;
    }
}
