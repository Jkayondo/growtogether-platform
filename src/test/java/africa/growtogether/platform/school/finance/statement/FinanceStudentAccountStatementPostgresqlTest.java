package africa.growtogether.platform.school.finance.statement;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class FinanceStudentAccountStatementPostgresqlTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:16-alpine"
            );

    private JdbcTemplate jdbc;
    private FinanceStudentAccountStatementJdbcRepository repository;

    @BeforeEach
    void prepareSchema() {

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword()
                );

        jdbc = new JdbcTemplate(dataSource);

        repository =
                new FinanceStudentAccountStatementJdbcRepository(
                        new NamedParameterJdbcTemplate(
                                dataSource
                        )
                );

        jdbc.execute("DROP TABLE IF EXISTS gts_payment_receipt");
        jdbc.execute("DROP TABLE IF EXISTS gts_payment_allocation_correction");
        jdbc.execute("DROP TABLE IF EXISTS gts_payment_allocation");
        jdbc.execute("DROP TABLE IF EXISTS gts_student_payment");
        jdbc.execute("DROP TABLE IF EXISTS gts_student_invoice");
        jdbc.execute("DROP TABLE IF EXISTS gts_student_financial_account");

        jdbc.execute(
                """
                CREATE TABLE gts_student_financial_account (
                    id UUID PRIMARY KEY,
                    tenant_id UUID NOT NULL,
                    account_number VARCHAR(100) NOT NULL,
                    student_id UUID NOT NULL,
                    currency_code VARCHAR(3) NOT NULL,
                    billing_status VARCHAR(30) NOT NULL,
                    status VARCHAR(20) NOT NULL
                )
                """
        );

        jdbc.execute(
                """
                CREATE TABLE gts_student_invoice (
                    id UUID PRIMARY KEY,
                    tenant_id UUID NOT NULL,
                    invoice_number VARCHAR(100) NOT NULL,
                    student_financial_account_id UUID NOT NULL,
                    student_id UUID NOT NULL,
                    invoice_date DATE NOT NULL,
                    due_date DATE,
                    currency_code VARCHAR(3) NOT NULL,
                    total_amount NUMERIC(18,2) NOT NULL,
                    paid_amount NUMERIC(18,2) NOT NULL,
                    outstanding_amount NUMERIC(18,2) NOT NULL,
                    invoice_status VARCHAR(30) NOT NULL,
                    issued_at TIMESTAMPTZ,
                    status VARCHAR(20) NOT NULL
                )
                """
        );

        jdbc.execute(
                """
                CREATE TABLE gts_student_payment (
                    id UUID PRIMARY KEY,
                    tenant_id UUID NOT NULL,
                    student_id UUID NOT NULL,
                    currency_code VARCHAR(3) NOT NULL,
                    payment_amount NUMERIC(18,2) NOT NULL,
                    payment_reference VARCHAR(255) NOT NULL,
                    payment_date TIMESTAMPTZ NOT NULL,
                    payment_status VARCHAR(30) NOT NULL,
                    status VARCHAR(20) NOT NULL
                )
                """
        );

        jdbc.execute(
                """
                CREATE TABLE gts_payment_allocation (
                    id UUID PRIMARY KEY,
                    tenant_id UUID NOT NULL,
                    student_payment_id UUID NOT NULL,
                    invoice_id UUID NOT NULL,
                    allocated_amount NUMERIC(18,2) NOT NULL,
                    allocated_at TIMESTAMPTZ NOT NULL,
                    allocation_status VARCHAR(30) NOT NULL,
                    status VARCHAR(20) NOT NULL
                )
                """
        );

        jdbc.execute(
                """
                CREATE TABLE gts_payment_allocation_correction (
                    id UUID PRIMARY KEY,
                    tenant_id UUID NOT NULL,
                    allocation_id UUID NOT NULL,
                    correction_type VARCHAR(30) NOT NULL,
                    reason TEXT NOT NULL,
                    replacement_allocation_id UUID,
                    created_at TIMESTAMPTZ NOT NULL,
                    created_by VARCHAR(150) NOT NULL
                )
                """
        );

        jdbc.execute(
                """
                CREATE TABLE gts_payment_receipt (
                    id UUID PRIMARY KEY,
                    tenant_id UUID NOT NULL,
                    receipt_number VARCHAR(100) NOT NULL,
                    student_payment_id UUID NOT NULL,
                    student_id UUID NOT NULL,
                    receipt_date TIMESTAMPTZ NOT NULL,
                    currency_code VARCHAR(3) NOT NULL,
                    receipt_amount NUMERIC(18,2) NOT NULL,
                    issued_at TIMESTAMPTZ NOT NULL,
                    receipt_status VARCHAR(30) NOT NULL,
                    status VARCHAR(20) NOT NULL
                )
                """
        );
    }

    @Test
    void calculatesSummaryArrearsAndUnallocatedMoney() {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_student_financial_account
                (
                    id, tenant_id, account_number,
                    student_id, currency_code,
                    billing_status, status
                )
                VALUES (?, ?, 'ACC-1', ?, 'UGX', 'ACTIVE', 'ACTIVE')
                """,
                accountId,
                tenantId,
                studentId
        );

        jdbc.update(
                """
                INSERT INTO gts_student_invoice
                (
                    id, tenant_id, invoice_number,
                    student_financial_account_id, student_id,
                    invoice_date, due_date, currency_code,
                    total_amount, paid_amount, outstanding_amount,
                    invoice_status, issued_at, status
                )
                VALUES (
                    ?, ?, 'INV-1', ?, ?,
                    DATE '2026-08-01',
                    DATE '2026-09-01',
                    'UGX',
                    1000000, 300000, 700000,
                    'ISSUED', CURRENT_TIMESTAMP, 'ACTIVE'
                )
                """,
                invoiceId,
                tenantId,
                accountId,
                studentId
        );

        jdbc.update(
                """
                INSERT INTO gts_student_payment
                (
                    id, tenant_id, student_id,
                    currency_code, payment_amount,
                    payment_reference, payment_date,
                    payment_status, status
                )
                VALUES (
                    ?, ?, ?, 'UGX', 500000,
                    'PAY-1', CURRENT_TIMESTAMP,
                    'VERIFIED', 'ACTIVE'
                )
                """,
                paymentId,
                tenantId,
                studentId
        );

        jdbc.update(
                """
                INSERT INTO gts_payment_allocation
                (
                    id, tenant_id, student_payment_id,
                    invoice_id, allocated_amount,
                    allocated_at, allocation_status, status
                )
                VALUES (
                    ?, ?, ?, ?, 300000,
                    CURRENT_TIMESTAMP, 'ACTIVE', 'ACTIVE'
                )
                """,
                UUID.randomUUID(),
                tenantId,
                paymentId,
                invoiceId
        );

        var numbers =
                repository.loadSummaryNumbers(
                        tenantId,
                        studentId,
                        "UGX",
                        LocalDate.of(2026, 9, 21)
                );

        assertEquals(
                0,
                new BigDecimal("1000000")
                        .compareTo(numbers.totalBilled())
        );

        assertEquals(
                0,
                new BigDecimal("300000")
                        .compareTo(numbers.totalPaid())
        );

        assertEquals(
                0,
                new BigDecimal("700000")
                        .compareTo(numbers.totalOutstanding())
        );

        assertEquals(
                0,
                new BigDecimal("700000")
                        .compareTo(numbers.arrearsOutstanding())
        );

        assertEquals(1, numbers.overdueInvoiceCount());

        BigDecimal unallocated =
                repository.loadUnallocatedPaymentAmount(
                        tenantId,
                        studentId,
                        "UGX"
                );

        assertEquals(
                0,
                new BigDecimal("200000")
                        .compareTo(unallocated)
        );

        var arrears =
                repository.listArrears(
                        tenantId,
                        studentId,
                        "UGX",
                        LocalDate.of(2026, 9, 21)
                );

        assertEquals(1, arrears.size());
        assertEquals(invoiceId, arrears.getFirst().invoiceId());
        assertEquals(20, arrears.getFirst().daysOverdue());
    }

    @Test
    void dueTodayIsNotArrears() {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_student_financial_account
                (
                    id, tenant_id, account_number,
                    student_id, currency_code,
                    billing_status, status
                )
                VALUES (?, ?, 'ACC-2', ?, 'UGX', 'ACTIVE', 'ACTIVE')
                """,
                accountId,
                tenantId,
                studentId
        );

        jdbc.update(
                """
                INSERT INTO gts_student_invoice
                (
                    id, tenant_id, invoice_number,
                    student_financial_account_id, student_id,
                    invoice_date, due_date, currency_code,
                    total_amount, paid_amount, outstanding_amount,
                    invoice_status, issued_at, status
                )
                VALUES (
                    ?, ?, 'INV-2', ?, ?,
                    DATE '2026-09-01',
                    DATE '2026-09-21',
                    'UGX',
                    100000, 0, 100000,
                    'ISSUED', CURRENT_TIMESTAMP, 'ACTIVE'
                )
                """,
                UUID.randomUUID(),
                tenantId,
                accountId,
                studentId
        );

        var numbers =
                repository.loadSummaryNumbers(
                        tenantId,
                        studentId,
                        "UGX",
                        LocalDate.of(2026, 9, 21)
                );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        numbers.arrearsOutstanding()
                )
        );

        assertEquals(0, numbers.overdueInvoiceCount());
    }


    @Test
    void nullDueDateIsNotArrears() {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_student_financial_account
                (
                    id, tenant_id, account_number,
                    student_id, currency_code,
                    billing_status, status
                )
                VALUES (?, ?, 'ACC-NULL-DUE', ?, 'UGX', 'ACTIVE', 'ACTIVE')
                """,
                accountId,
                tenantId,
                studentId
        );

        jdbc.update(
                """
                INSERT INTO gts_student_invoice
                (
                    id, tenant_id, invoice_number,
                    student_financial_account_id, student_id,
                    invoice_date, due_date, currency_code,
                    total_amount, paid_amount, outstanding_amount,
                    invoice_status, issued_at, status
                )
                VALUES (
                    ?, ?, 'INV-NULL-DUE', ?, ?,
                    DATE '2026-08-01',
                    NULL,
                    'UGX',
                    100000, 0, 100000,
                    'ISSUED',
                    CURRENT_TIMESTAMP,
                    'ACTIVE'
                )
                """,
                UUID.randomUUID(),
                tenantId,
                accountId,
                studentId
        );

        var result =
                repository.loadSummaryNumbers(
                        tenantId,
                        studentId,
                        "UGX",
                        LocalDate.of(2026, 9, 21)
                );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.arrearsOutstanding()
                )
        );

        assertEquals(0, result.overdueInvoiceCount());

        assertTrue(
                repository.listArrears(
                        tenantId,
                        studentId,
                        "UGX",
                        LocalDate.of(2026, 9, 21)
                ).isEmpty()
        );
    }

    @Test
    void zeroOutstandingInvoiceIsNotArrears() {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_student_financial_account
                (
                    id, tenant_id, account_number,
                    student_id, currency_code,
                    billing_status, status
                )
                VALUES (?, ?, 'ACC-ZERO', ?, 'UGX', 'ACTIVE', 'ACTIVE')
                """,
                accountId,
                tenantId,
                studentId
        );

        jdbc.update(
                """
                INSERT INTO gts_student_invoice
                (
                    id, tenant_id, invoice_number,
                    student_financial_account_id, student_id,
                    invoice_date, due_date, currency_code,
                    total_amount, paid_amount, outstanding_amount,
                    invoice_status, issued_at, status
                )
                VALUES (
                    ?, ?, 'INV-ZERO', ?, ?,
                    DATE '2026-08-01',
                    DATE '2026-08-15',
                    'UGX',
                    100000, 100000, 0,
                    'PAID',
                    CURRENT_TIMESTAMP,
                    'ACTIVE'
                )
                """,
                UUID.randomUUID(),
                tenantId,
                accountId,
                studentId
        );

        var result =
                repository.loadSummaryNumbers(
                        tenantId,
                        studentId,
                        "UGX",
                        LocalDate.of(2026, 9, 21)
                );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.arrearsOutstanding()
                )
        );

        assertEquals(0, result.overdueInvoiceCount());
    }

    @Test
    void draftAndCancelledInvoicesAreExcludedFromFinancialTotals() {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_student_financial_account
                (
                    id, tenant_id, account_number,
                    student_id, currency_code,
                    billing_status, status
                )
                VALUES (?, ?, 'ACC-EXCLUDE', ?, 'UGX', 'ACTIVE', 'ACTIVE')
                """,
                accountId,
                tenantId,
                studentId
        );

        for (String invoiceStatus :
                new String[]{"DRAFT", "CANCELLED"}) {

            jdbc.update(
                    """
                    INSERT INTO gts_student_invoice
                    (
                        id, tenant_id, invoice_number,
                        student_financial_account_id, student_id,
                        invoice_date, due_date, currency_code,
                        total_amount, paid_amount, outstanding_amount,
                        invoice_status, issued_at, status
                    )
                    VALUES (
                        ?, ?, ?, ?, ?,
                        DATE '2026-08-01',
                        DATE '2026-08-15',
                        'UGX',
                        500000, 0, 500000,
                        ?,
                        CURRENT_TIMESTAMP,
                        'ACTIVE'
                    )
                    """,
                    UUID.randomUUID(),
                    tenantId,
                    "INV-" + invoiceStatus,
                    accountId,
                    studentId,
                    invoiceStatus
            );
        }

        var result =
                repository.loadSummaryNumbers(
                        tenantId,
                        studentId,
                        "UGX",
                        LocalDate.of(2026, 9, 21)
                );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.totalBilled()
                )
        );

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(
                        result.totalOutstanding()
                )
        );

        assertEquals(0, result.invoiceCount());
        assertEquals(0, result.overdueInvoiceCount());
    }

    @Test
    void currencyAggregationIsStrictlyIsolated() {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        UUID ugxAccount = UUID.randomUUID();
        UUID usdAccount = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_student_financial_account
                (
                    id, tenant_id, account_number,
                    student_id, currency_code,
                    billing_status, status
                )
                VALUES
                    (?, ?, 'ACC-UGX', ?, 'UGX', 'ACTIVE', 'ACTIVE'),
                    (?, ?, 'ACC-USD', ?, 'USD', 'ACTIVE', 'ACTIVE')
                """,
                ugxAccount,
                tenantId,
                studentId,
                usdAccount,
                tenantId,
                studentId
        );

        jdbc.update(
                """
                INSERT INTO gts_student_invoice
                (
                    id, tenant_id, invoice_number,
                    student_financial_account_id, student_id,
                    invoice_date, due_date, currency_code,
                    total_amount, paid_amount, outstanding_amount,
                    invoice_status, issued_at, status
                )
                VALUES
                (
                    ?, ?, 'INV-UGX', ?, ?,
                    DATE '2026-08-01',
                    DATE '2026-09-01',
                    'UGX',
                    900000, 0, 900000,
                    'ISSUED', CURRENT_TIMESTAMP, 'ACTIVE'
                ),
                (
                    ?, ?, 'INV-USD', ?, ?,
                    DATE '2026-08-01',
                    DATE '2026-09-01',
                    'USD',
                    500, 0, 500,
                    'ISSUED', CURRENT_TIMESTAMP, 'ACTIVE'
                )
                """,
                UUID.randomUUID(),
                tenantId,
                ugxAccount,
                studentId,
                UUID.randomUUID(),
                tenantId,
                usdAccount,
                studentId
        );

        var ugx =
                repository.loadSummaryNumbers(
                        tenantId,
                        studentId,
                        "UGX",
                        LocalDate.of(2026, 9, 21)
                );

        var usd =
                repository.loadSummaryNumbers(
                        tenantId,
                        studentId,
                        "USD",
                        LocalDate.of(2026, 9, 21)
                );

        assertEquals(
                0,
                new BigDecimal("900000")
                        .compareTo(ugx.totalBilled())
        );

        assertEquals(
                0,
                new BigDecimal("500")
                        .compareTo(usd.totalBilled())
        );

        assertEquals(1, ugx.invoiceCount());
        assertEquals(1, usd.invoiceCount());
    }

    @Test
    void correctedAllocationStateControlsUnallocatedPayment() {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        UUID paymentId = UUID.randomUUID();
        UUID originalAllocation = UUID.randomUUID();
        UUID replacementAllocation = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_student_payment
                (
                    id, tenant_id, student_id,
                    currency_code, payment_amount,
                    payment_reference, payment_date,
                    payment_status, status
                )
                VALUES (
                    ?, ?, ?, 'UGX', 500000,
                    'PAY-CORRECTED',
                    TIMESTAMPTZ '2026-09-05 10:00:00+00',
                    'VERIFIED', 'ACTIVE'
                )
                """,
                paymentId,
                tenantId,
                studentId
        );

        jdbc.update(
                """
                INSERT INTO gts_payment_allocation
                (
                    id, tenant_id, student_payment_id,
                    invoice_id, allocated_amount,
                    allocated_at, allocation_status, status
                )
                VALUES
                (
                    ?, ?, ?, ?, 300000,
                    TIMESTAMPTZ '2026-09-05 11:00:00+00',
                    'REALLOCATED', 'ACTIVE'
                ),
                (
                    ?, ?, ?, ?, 300000,
                    TIMESTAMPTZ '2026-09-06 11:00:00+00',
                    'ACTIVE', 'ACTIVE'
                )
                """,
                originalAllocation,
                tenantId,
                paymentId,
                UUID.randomUUID(),
                replacementAllocation,
                tenantId,
                paymentId,
                UUID.randomUUID()
        );

        jdbc.update(
                """
                INSERT INTO gts_payment_allocation_correction
                (
                    id, tenant_id, allocation_id,
                    correction_type, reason,
                    replacement_allocation_id,
                    created_at, created_by
                )
                VALUES (
                    ?, ?, ?,
                    'REALLOCATION',
                    'Move allocation to correct invoice',
                    ?,
                    TIMESTAMPTZ '2026-09-06 10:00:00+00',
                    's5-test'
                )
                """,
                UUID.randomUUID(),
                tenantId,
                originalAllocation,
                replacementAllocation
        );

        BigDecimal unallocated =
                repository.loadUnallocatedPaymentAmount(
                        tenantId,
                        studentId,
                        "UGX"
                );

        assertEquals(
                0,
                new BigDecimal("200000")
                        .compareTo(unallocated)
        );
    }

    @Test
    void statementAppliesPeriodFilterChronologyAndReceiptReference() {

        UUID tenantId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_student_financial_account
                (
                    id, tenant_id, account_number,
                    student_id, currency_code,
                    billing_status, status
                )
                VALUES (?, ?, 'ACC-STMT', ?, 'UGX', 'ACTIVE', 'ACTIVE')
                """,
                accountId,
                tenantId,
                studentId
        );

        jdbc.update(
                """
                INSERT INTO gts_student_invoice
                (
                    id, tenant_id, invoice_number,
                    student_financial_account_id, student_id,
                    invoice_date, due_date, currency_code,
                    total_amount, paid_amount, outstanding_amount,
                    invoice_status, issued_at, status
                )
                VALUES (
                    ?, ?, 'INV-STMT', ?, ?,
                    DATE '2026-08-01',
                    DATE '2026-09-01',
                    'UGX',
                    300000, 100000, 200000,
                    'ISSUED',
                    TIMESTAMPTZ '2026-08-01 08:00:00+00',
                    'ACTIVE'
                )
                """,
                invoiceId,
                tenantId,
                accountId,
                studentId
        );

        jdbc.update(
                """
                INSERT INTO gts_student_payment
                (
                    id, tenant_id, student_id,
                    currency_code, payment_amount,
                    payment_reference, payment_date,
                    payment_status, status
                )
                VALUES (
                    ?, ?, ?, 'UGX', 100000,
                    'PAY-STMT',
                    TIMESTAMPTZ '2026-09-05 09:00:00+00',
                    'VERIFIED', 'ACTIVE'
                )
                """,
                paymentId,
                tenantId,
                studentId
        );

        jdbc.update(
                """
                INSERT INTO gts_payment_allocation
                (
                    id, tenant_id, student_payment_id,
                    invoice_id, allocated_amount,
                    allocated_at, allocation_status, status
                )
                VALUES (
                    ?, ?, ?, ?, 100000,
                    TIMESTAMPTZ '2026-09-05 10:00:00+00',
                    'ACTIVE', 'ACTIVE'
                )
                """,
                UUID.randomUUID(),
                tenantId,
                paymentId,
                invoiceId
        );

        jdbc.update(
                """
                INSERT INTO gts_payment_receipt
                (
                    id, tenant_id, receipt_number,
                    student_payment_id, student_id,
                    receipt_date, currency_code,
                    receipt_amount, issued_at,
                    receipt_status, status
                )
                VALUES (
                    ?, ?, 'RCT-0000000001',
                    ?, ?,
                    TIMESTAMPTZ '2026-09-05 11:00:00+00',
                    'UGX',
                    100000,
                    TIMESTAMPTZ '2026-09-05 11:00:00+00',
                    'ISSUED', 'ACTIVE'
                )
                """,
                UUID.randomUUID(),
                tenantId,
                paymentId,
                studentId
        );

        var entries =
                repository.listStatementEntries(
                        tenantId,
                        studentId,
                        "UGX",
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 9, 30)
                );

        assertEquals(3, entries.size());

        assertEquals(
                "ALLOCATION",
                entries.get(0).entryType()
        );

        assertEquals(
                "PAYMENT",
                entries.get(1).entryType()
        );

        assertEquals(
                "RECEIPT",
                entries.get(2).entryType()
        );

        assertEquals(
                "RCT-0000000001",
                entries.get(2).reference()
        );

        assertEquals(
                paymentId,
                entries.get(2).relatedPaymentId()
        );

        assertTrue(
                entries.stream()
                        .noneMatch(
                                entry ->
                                        "INVOICE".equals(
                                                entry.entryType()
                                        )
                        )
        );
    }
}
