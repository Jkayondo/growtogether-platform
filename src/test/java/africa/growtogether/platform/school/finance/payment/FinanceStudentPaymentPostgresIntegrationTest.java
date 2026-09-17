package africa.growtogether.platform.school.finance.payment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class FinanceStudentPaymentPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:16-alpine"
            );

    private static final UUID TENANT_ID =
            UUID.fromString(
                    "30000000-0000-0000-0000-000000000001"
            );

    private static final UUID OTHER_TENANT_ID =
            UUID.fromString(
                    "30000000-0000-0000-0000-000000000002"
            );

    private static final UUID ACCOUNT_ID =
            UUID.fromString(
                    "40000000-0000-0000-0000-000000000001"
            );

    private static final UUID STUDENT_ID =
            UUID.fromString(
                    "50000000-0000-0000-0000-000000000001"
            );

    private static final String ACTOR =
            "90000000-0000-0000-0000-000000000001";

    private NamedParameterJdbcTemplate jdbc;
    private FinanceStudentPaymentService service;

    @BeforeEach
    void setUp() {
        var dataSource =
                new DriverManagerDataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword()
                );

        jdbc =
                new NamedParameterJdbcTemplate(
                        dataSource
                );

        jdbc.getJdbcTemplate().execute(
                "CREATE EXTENSION IF NOT EXISTS pgcrypto"
        );

        jdbc.getJdbcTemplate().execute(
                "DROP TABLE IF EXISTS gts_student_payment"
        );

        jdbc.getJdbcTemplate().execute(
                "DROP TABLE IF EXISTS gts_student_financial_account"
        );

        jdbc.getJdbcTemplate().execute(
                "CREATE TABLE gts_student_financial_account ("
                + "id UUID PRIMARY KEY, tenant_id UUID NOT NULL, student_id UUID, currency_code VARCHAR(16) NOT NULL"
                + ")"
        );

        jdbc.getJdbcTemplate().execute(
                "CREATE TABLE gts_student_payment ("
                + "id UUID PRIMARY KEY DEFAULT gen_random_uuid(), tenant_id UUID NOT NULL, student_financial_account_id UUID NOT NULL REFERENCES gts_student_financial_account(id), payment_amount NUMERIC(19,2) NOT NULL CHECK (payment_amount > 0), student_id UUID NOT NULL, currency_code VARCHAR(16) NOT NULL, payment_method VARCHAR(64) NOT NULL CHECK (payment_method IN ('BANK_DEPOSIT', 'BANK_TRANSFER', 'CARD', 'CASH', 'CHEQUE', 'CREDIT_BALANCE', 'DIRECT_DEBIT', 'MOBILE_MONEY', 'OTHER', 'SALARY_DEDUCTION', 'SCHOLARSHIP', 'SPONSOR')), payment_reference VARCHAR(255) NOT NULL UNIQUE, status VARCHAR(64) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'ALLOCATED', 'ARCHIVED', 'COMPLETED', 'DISPUTED', 'FAILED', 'INACTIVE', 'PENDING', 'PROCESSING', 'RECEIVED', 'REFUNDED', 'REVERSED', 'VERIFIED')), payment_date TIMESTAMPTZ NOT NULL, eip_payment_transaction_id UUID NULL, created_at TIMESTAMPTZ NOT NULL, created_by VARCHAR(150) NOT NULL, updated_at TIMESTAMPTZ NOT NULL, updated_by VARCHAR(150) NOT NULL"
                + ")"
        );

        var params =
                new MapSqlParameterSource()
                        .addValue(
                                "id",
                                ACCOUNT_ID
                        )
                        .addValue(
                                "tenant",
                                TENANT_ID
                        )
                        .addValue(
                                "student",
                                STUDENT_ID
                        )
                        .addValue(
                                "currency",
                                "UGX"
                        );

        jdbc.update(
                "INSERT INTO gts_student_financial_account (id, tenant_id, student_id, currency_code) VALUES (:id, :tenant, :student, :currency)",
                params
        );

        var repository =
                new FinanceStudentPaymentJdbcRepository(
                        jdbc
                );

        service =
                new FinanceStudentPaymentService(
                        repository
                );
    }

    @Test
    void createsReadsAndListsPaymentWithTenantIsolation() {
        var request =
                new FinanceStudentPaymentDtos.CreateRequest(
                        ACCOUNT_ID,
            new BigDecimal("150000.00"),
            "BANK_DEPOSIT",
            "FIN-B5-S1-PG-001",
            Instant.parse("2026-09-15T05:30:00Z")
                );

        var payment =
                service.create(
                        TENANT_ID,
                        request,
                        ACTOR
                );

        var auditParams =
                new MapSqlParameterSource()
                        .addValue(
                                "paymentId",
                                payment.id()
                        );

        var audit =
                jdbc.queryForMap(
                        "SELECT created_by, updated_by FROM gts_student_payment WHERE id = :paymentId",
                        auditParams
                );

        assertEquals(
                ACTOR,
                audit.get("created_by")
        );

        assertEquals(
                ACTOR,
                audit.get("updated_by")
        );

        assertEquals(
                TENANT_ID,
                payment.tenantId()
        );

        assertEquals(
                ACCOUNT_ID,
                payment.studentFinancialAccountId()
        );

                assertEquals(
            STUDENT_ID,
            payment.studentId()
    );

        assertEquals(
                0,
                new BigDecimal("150000.00")
                        .compareTo(
                                payment.amount()
                        )
        );

        assertEquals(
                "UGX",
                payment.currencyCode()
        );

        assertEquals(
                payment.id(),
                service.get(
                        TENANT_ID,
                        payment.id()
                ).id()
        );

        assertFalse(
                service.listByFinancialAccount(
                        TENANT_ID,
                        ACCOUNT_ID
                ).isEmpty()
        );

        ResponseStatusException error =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.get(
                                OTHER_TENANT_ID,
                                payment.id()
                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                error.getStatusCode()
        );
    }
}
