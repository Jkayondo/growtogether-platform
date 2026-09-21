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
class FinanceStudentAccountStatementTenantIsolationTest {

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
    }

    @Test
    void crossTenantRowsNeverContributeToSummary() {

        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();

        UUID studentId = UUID.randomUUID();

        UUID accountA = UUID.randomUUID();
        UUID accountB = UUID.randomUUID();

        insertAccount(
                tenantA,
                studentId,
                accountA,
                "A-ACCOUNT"
        );

        insertAccount(
                tenantB,
                studentId,
                accountB,
                "B-ACCOUNT"
        );

        insertInvoice(
                tenantA,
                studentId,
                accountA,
                "A-INV",
                new BigDecimal("100000")
        );

        insertInvoice(
                tenantB,
                studentId,
                accountB,
                "B-INV",
                new BigDecimal("900000")
        );

        var tenantANumbers =
                repository.loadSummaryNumbers(
                        tenantA,
                        studentId,
                        "UGX",
                        LocalDate.of(2026, 9, 21)
                );

        assertEquals(
                0,
                new BigDecimal("100000")
                        .compareTo(
                                tenantANumbers.totalBilled()
                        )
        );

        assertEquals(1, tenantANumbers.invoiceCount());

        var account =
                repository.findAccountScope(
                        tenantA,
                        studentId,
                        "UGX"
                ).orElseThrow();

        assertEquals(accountA, account.accountId());
        assertNotEquals(accountB, account.accountId());
    }

    private void insertAccount(
            UUID tenantId,
            UUID studentId,
            UUID accountId,
            String accountNumber
    ) {

        jdbc.update(
                """
                INSERT INTO gts_student_financial_account
                (
                    id, tenant_id, account_number,
                    student_id, currency_code,
                    billing_status, status
                )
                VALUES (?, ?, ?, ?, 'UGX', 'ACTIVE', 'ACTIVE')
                """,
                accountId,
                tenantId,
                accountNumber,
                studentId
        );
    }

    private void insertInvoice(
            UUID tenantId,
            UUID studentId,
            UUID accountId,
            String invoiceNumber,
            BigDecimal amount
    ) {

        jdbc.update(
                """
                INSERT INTO gts_student_invoice
                (
                    id, tenant_id, invoice_number,
                    student_financial_account_id, student_id,
                    invoice_date, due_date, currency_code,
                    total_amount, paid_amount,
                    outstanding_amount, invoice_status,
                    issued_at, status
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    DATE '2026-08-01',
                    DATE '2026-09-01',
                    'UGX',
                    ?, 0, ?,
                    'ISSUED',
                    CURRENT_TIMESTAMP,
                    'ACTIVE'
                )
                """,
                UUID.randomUUID(),
                tenantId,
                invoiceNumber,
                accountId,
                studentId,
                amount,
                amount
        );
    }


    @Test
    void sameTenantDifferentStudentRowsNeverLeak() {

        UUID tenantId = UUID.randomUUID();

        UUID studentA = UUID.randomUUID();
        UUID studentB = UUID.randomUUID();

        UUID accountA = UUID.randomUUID();
        UUID accountB = UUID.randomUUID();

        insertAccount(
                tenantId,
                studentA,
                accountA,
                "STUDENT-A"
        );

        insertAccount(
                tenantId,
                studentB,
                accountB,
                "STUDENT-B"
        );

        insertInvoice(
                tenantId,
                studentA,
                accountA,
                "A-INVOICE",
                new BigDecimal("120000")
        );

        insertInvoice(
                tenantId,
                studentB,
                accountB,
                "B-INVOICE",
                new BigDecimal("880000")
        );

        var studentASummary =
                repository.loadSummaryNumbers(
                        tenantId,
                        studentA,
                        "UGX",
                        LocalDate.of(2026, 9, 21)
                );

        assertEquals(
                0,
                new BigDecimal("120000")
                        .compareTo(
                                studentASummary.totalBilled()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("120000")
                        .compareTo(
                                studentASummary.totalOutstanding()
                        )
        );

        assertEquals(1, studentASummary.invoiceCount());

        var account =
                repository.findAccountScope(
                        tenantId,
                        studentA,
                        "UGX"
                ).orElseThrow();

        assertEquals(accountA, account.accountId());
        assertNotEquals(accountB, account.accountId());
    }
}
