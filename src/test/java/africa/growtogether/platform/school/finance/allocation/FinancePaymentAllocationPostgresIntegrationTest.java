
package africa.growtogether.platform.school.finance.allocation;

import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.AllocationResponse;
import static africa.growtogether.platform.school.finance.allocation.FinancePaymentAllocationDtos.CreateRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class FinancePaymentAllocationPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            );

    private static final UUID TENANT =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111111"
            );

    private static final UUID OTHER_TENANT =
            UUID.fromString(
                    "11111111-1111-1111-1111-111111111112"
            );

    private static final UUID STUDENT =
            UUID.fromString(
                    "22222222-2222-2222-2222-222222222222"
            );

    private static final UUID OTHER_STUDENT =
            UUID.fromString(
                    "22222222-2222-2222-2222-222222222223"
            );

    private static final UUID ACTOR =
            UUID.fromString(
                    "33333333-3333-3333-3333-333333333333"
            );

    private JdbcTemplate jdbc;
    private FinancePaymentAllocationService service;
    private TransactionTemplate transaction;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource =
                new DriverManagerDataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword()
                );

        jdbc = new JdbcTemplate(dataSource);

        transaction =
                new TransactionTemplate(
                        new DataSourceTransactionManager(
                                dataSource
                        )
                );

        service =
                new FinancePaymentAllocationService(
                        new FinancePaymentAllocationJdbcRepository(
                                jdbc
                        )
                );

        resetSchema();
    }

    @Test
    void persistsReadsListsAuditsAndEnforcesTenant() {
        UUID paymentId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID arrangementId = UUID.randomUUID();
        UUID installmentId = UUID.randomUUID();

        seedPayment(
                TENANT,
                paymentId,
                STUDENT,
                new BigDecimal("200.00"),
                "UGX"
        );

        seedInvoice(
                TENANT,
                invoiceId,
                STUDENT,
                new BigDecimal("150.00"),
                "UGX"
        );

        seedInvoiceLine(
                TENANT,
                lineId,
                invoiceId
        );

        seedArrangement(
                TENANT,
                arrangementId,
                STUDENT
        );

        seedInstallment(
                TENANT,
                installmentId,
                arrangementId
        );

        AllocationResponse created =
                inTx(() -> service.create(
                        TENANT,
                        paymentId,
                        new CreateRequest(
                                invoiceId,
                                lineId,
                                installmentId,
                                new BigDecimal("40.00")
                        ),
                        ACTOR
                ));

        assertEquals(
                paymentId,
                created.studentPaymentId()
        );

        assertEquals(
                invoiceId,
                created.invoiceId()
        );

        assertEquals(
                lineId,
                created.invoiceLineId()
        );

        assertEquals(
                installmentId,
                created.paymentInstallmentId()
        );

        assertEquals(
                0,
                new BigDecimal("40.00")
                        .compareTo(
                                created.allocatedAmount()
                        )
        );

        assertEquals(
                ACTOR,
                created.allocatedBy()
        );

        assertEquals(
                "ACTIVE",
                created.allocationStatus()
        );

        assertEquals(
                "ACTIVE",
                created.status()
        );

        assertEquals(
                ACTOR.toString(),
                jdbc.queryForObject(
                        """
                        SELECT created_by
                        FROM gts_payment_allocation
                        WHERE id = ?
                        """,
                        String.class,
                        created.allocationId()
                )
        );

        assertEquals(
                ACTOR.toString(),
                jdbc.queryForObject(
                        """
                        SELECT updated_by
                        FROM gts_payment_allocation
                        WHERE id = ?
                        """,
                        String.class,
                        created.allocationId()
                )
        );

        assertEquals(
                created,
                inTx(() -> service.get(
                        TENANT,
                        paymentId,
                        created.allocationId()
                ))
        );

        assertEquals(
                List.of(created),
                inTx(() -> service.list(
                        TENANT,
                        paymentId
                ))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inTx(() -> service.get(
                        OTHER_TENANT,
                        paymentId,
                        created.allocationId()
                ))
        );
    }

    @Test
    void validatesPaymentInvoiceStudentCurrencyAndPositiveAmount() {
        UUID paymentId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();

        seedPayment(
                TENANT,
                paymentId,
                STUDENT,
                new BigDecimal("100.00"),
                "UGX"
        );

        seedInvoice(
                TENANT,
                invoiceId,
                STUDENT,
                new BigDecimal("100.00"),
                "UGX"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(
                        TENANT,
                        paymentId,
                        new CreateRequest(
                                invoiceId,
                                null,
                                null,
                                BigDecimal.ZERO
                        ),
                        ACTOR
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inTx(() -> service.create(
                        TENANT,
                        UUID.randomUUID(),
                        new CreateRequest(
                                invoiceId,
                                null,
                                null,
                                BigDecimal.ONE
                        ),
                        ACTOR
                ))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inTx(() -> service.create(
                        TENANT,
                        paymentId,
                        new CreateRequest(
                                UUID.randomUUID(),
                                null,
                                null,
                                BigDecimal.ONE
                        ),
                        ACTOR
                ))
        );

        UUID wrongStudentInvoice =
                UUID.randomUUID();

        seedInvoice(
                TENANT,
                wrongStudentInvoice,
                OTHER_STUDENT,
                new BigDecimal("100.00"),
                "UGX"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inTx(() -> service.create(
                        TENANT,
                        paymentId,
                        new CreateRequest(
                                wrongStudentInvoice,
                                null,
                                null,
                                BigDecimal.ONE
                        ),
                        ACTOR
                ))
        );

        UUID wrongCurrencyInvoice =
                UUID.randomUUID();

        seedInvoice(
                TENANT,
                wrongCurrencyInvoice,
                STUDENT,
                new BigDecimal("100.00"),
                "USD"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inTx(() -> service.create(
                        TENANT,
                        paymentId,
                        new CreateRequest(
                                wrongCurrencyInvoice,
                                null,
                                null,
                                BigDecimal.ONE
                        ),
                        ACTOR
                ))
        );
    }

    @Test
    void validatesOptionalLineAndInstallmentStudentContext() {
        UUID paymentId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();
        UUID otherInvoiceId = UUID.randomUUID();

        seedPayment(
                TENANT,
                paymentId,
                STUDENT,
                new BigDecimal("100.00"),
                "UGX"
        );

        seedInvoice(
                TENANT,
                invoiceId,
                STUDENT,
                new BigDecimal("100.00"),
                "UGX"
        );

        seedInvoice(
                TENANT,
                otherInvoiceId,
                STUDENT,
                new BigDecimal("100.00"),
                "UGX"
        );

        UUID wrongLine = UUID.randomUUID();

        seedInvoiceLine(
                TENANT,
                wrongLine,
                otherInvoiceId
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inTx(() -> service.create(
                        TENANT,
                        paymentId,
                        new CreateRequest(
                                invoiceId,
                                wrongLine,
                                null,
                                new BigDecimal("10.00")
                        ),
                        ACTOR
                ))
        );

        UUID wrongArrangement =
                UUID.randomUUID();

        UUID wrongInstallment =
                UUID.randomUUID();

        seedArrangement(
                TENANT,
                wrongArrangement,
                OTHER_STUDENT
        );

        seedInstallment(
                TENANT,
                wrongInstallment,
                wrongArrangement
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inTx(() -> service.create(
                        TENANT,
                        paymentId,
                        new CreateRequest(
                                invoiceId,
                                null,
                                wrongInstallment,
                                new BigDecimal("10.00")
                        ),
                        ACTOR
                ))
        );
    }

    @Test
    void activeAllocationsLimitPaymentAndInvoiceRemainingAmounts() {
        UUID paymentId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();

        seedPayment(
                TENANT,
                paymentId,
                STUDENT,
                new BigDecimal("100.00"),
                "UGX"
        );

        seedInvoice(
                TENANT,
                invoiceId,
                STUDENT,
                new BigDecimal("100.00"),
                "UGX"
        );

        inTx(() -> service.create(
                TENANT,
                paymentId,
                new CreateRequest(
                        invoiceId,
                        null,
                        null,
                        new BigDecimal("60.00")
                ),
                ACTOR
        ));

        assertThrows(
                IllegalArgumentException.class,
                () -> inTx(() -> service.create(
                        TENANT,
                        paymentId,
                        new CreateRequest(
                                invoiceId,
                                null,
                                null,
                                new BigDecimal("50.00")
                        ),
                        ACTOR
                ))
        );

        UUID secondPayment = UUID.randomUUID();

        seedPayment(
                TENANT,
                secondPayment,
                STUDENT,
                new BigDecimal("100.00"),
                "UGX"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inTx(() -> service.create(
                        TENANT,
                        secondPayment,
                        new CreateRequest(
                                invoiceId,
                                null,
                                null,
                                new BigDecimal("50.00")
                        ),
                        ACTOR
                ))
        );
    }

    @Test
    @Timeout(15)
    void concurrentRequestsCannotDoubleAllocate() throws Exception {
        UUID paymentId = UUID.randomUUID();
        UUID invoiceId = UUID.randomUUID();

        seedPayment(
                TENANT,
                paymentId,
                STUDENT,
                new BigDecimal("100.00"),
                "UGX"
        );

        seedInvoice(
                TENANT,
                invoiceId,
                STUDENT,
                new BigDecimal("100.00"),
                "UGX"
        );

        CountDownLatch start =
                new CountDownLatch(1);

        Callable<Boolean> action = () -> {
            start.await();

            try {
                inTx(() -> service.create(
                        TENANT,
                        paymentId,
                        new CreateRequest(
                                invoiceId,
                                null,
                                null,
                                new BigDecimal("80.00")
                        ),
                        ACTOR
                ));
                return true;
            } catch (IllegalArgumentException ex) {
                return false;
            }
        };

        ExecutorService executor =
                Executors.newFixedThreadPool(2);

        try {
            Future<Boolean> first =
                    executor.submit(action);

            Future<Boolean> second =
                    executor.submit(action);

            start.countDown();

            boolean firstResult =
                    first.get(
                            10,
                            TimeUnit.SECONDS
                    );

            boolean secondResult =
                    second.get(
                            10,
                            TimeUnit.SECONDS
                    );

            assertTrue(
                    firstResult ^ secondResult
            );

            BigDecimal total =
                    jdbc.queryForObject(
                            """
                            SELECT COALESCE(
                                SUM(allocated_amount),
                                0
                            )
                            FROM gts_payment_allocation
                            WHERE tenant_id = ?
                              AND student_payment_id = ?
                              AND allocation_status = 'ACTIVE'
                              AND status = 'ACTIVE'
                            """,
                            BigDecimal.class,
                            TENANT,
                            paymentId
                    );

            assertEquals(
                    0,
                    new BigDecimal("80.00")
                            .compareTo(total)
            );
        } finally {
            executor.shutdownNow();
        }
    }

    private <T> T inTx(
            Supplier<T> action
    ) {
        return transaction.execute(
                status -> action.get()
        );
    }

    private void resetSchema() {
        jdbc.execute(
                "DROP SCHEMA public CASCADE"
        );

        jdbc.execute(
                "CREATE SCHEMA public"
        );

        jdbc.execute("""
                CREATE TABLE gts_student_payment (
                    id UUID PRIMARY KEY,
                    tenant_id UUID NOT NULL,
                    student_id UUID NOT NULL,
                    payment_amount NUMERIC(18,2) NOT NULL,
                    currency_code VARCHAR(3) NOT NULL
                )
                """);

        jdbc.execute("""
                CREATE TABLE gts_student_invoice (
                    id UUID PRIMARY KEY,
                    tenant_id UUID NOT NULL,
                    student_id UUID NOT NULL,
                    outstanding_amount NUMERIC(18,2) NOT NULL,
                    currency_code VARCHAR(3) NOT NULL
                )
                """);

        jdbc.execute("""
                CREATE TABLE gts_student_invoice_line (
                    id UUID PRIMARY KEY,
                    tenant_id UUID NOT NULL,
                    invoice_id UUID NOT NULL
                        REFERENCES gts_student_invoice(id)
                )
                """);

        jdbc.execute("""
                CREATE TABLE gts_payment_arrangement (
                    id UUID PRIMARY KEY,
                    tenant_id UUID NOT NULL,
                    student_id UUID NOT NULL
                )
                """);

        jdbc.execute("""
                CREATE TABLE gts_payment_installment (
                    id UUID PRIMARY KEY,
                    tenant_id UUID NOT NULL,
                    payment_arrangement_id UUID NOT NULL
                        REFERENCES gts_payment_arrangement(id)
                )
                """);

        jdbc.execute("""
                CREATE TABLE gts_payment_allocation (
                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                    tenant_id UUID NOT NULL,
                    student_payment_id UUID NOT NULL
                        REFERENCES gts_student_payment(id)
                        ON DELETE CASCADE,
                    invoice_id UUID NOT NULL
                        REFERENCES gts_student_invoice(id),
                    invoice_line_id UUID
                        REFERENCES gts_student_invoice_line(id),
                    payment_installment_id UUID
                        REFERENCES gts_payment_installment(id),
                    allocated_amount NUMERIC(18,2) NOT NULL,
                    allocated_at TIMESTAMPTZ NOT NULL,
                    allocated_by UUID,
                    allocation_status VARCHAR(30)
                        NOT NULL DEFAULT 'ACTIVE',
                    status VARCHAR(20)
                        NOT NULL DEFAULT 'ACTIVE',
                    created_at TIMESTAMPTZ NOT NULL,
                    created_by VARCHAR(150) NOT NULL,
                    updated_at TIMESTAMPTZ NOT NULL,
                    updated_by VARCHAR(150) NOT NULL,
                    version BIGINT NOT NULL DEFAULT 0,
                    CHECK (allocated_amount > 0),
                    CHECK (
                        allocation_status IN (
                            'ACTIVE',
                            'REVERSED',
                            'REALLOCATED',
                            'ARCHIVED'
                        )
                    ),
                    CHECK (
                        status IN (
                            'ACTIVE',
                            'INACTIVE',
                            'ARCHIVED'
                        )
                    )
                )
                """);
    }

    private void seedPayment(
            UUID tenantId,
            UUID paymentId,
            UUID studentId,
            BigDecimal amount,
            String currency
    ) {
        jdbc.update(
                """
                INSERT INTO gts_student_payment (
                    id,
                    tenant_id,
                    student_id,
                    payment_amount,
                    currency_code
                ) VALUES (?, ?, ?, ?, ?)
                """,
                paymentId,
                tenantId,
                studentId,
                amount,
                currency
        );
    }

    private void seedInvoice(
            UUID tenantId,
            UUID invoiceId,
            UUID studentId,
            BigDecimal outstanding,
            String currency
    ) {
        jdbc.update(
                """
                INSERT INTO gts_student_invoice (
                    id,
                    tenant_id,
                    student_id,
                    outstanding_amount,
                    currency_code
                ) VALUES (?, ?, ?, ?, ?)
                """,
                invoiceId,
                tenantId,
                studentId,
                outstanding,
                currency
        );
    }

    private void seedInvoiceLine(
            UUID tenantId,
            UUID lineId,
            UUID invoiceId
    ) {
        jdbc.update(
                """
                INSERT INTO gts_student_invoice_line (
                    id,
                    tenant_id,
                    invoice_id
                ) VALUES (?, ?, ?)
                """,
                lineId,
                tenantId,
                invoiceId
        );
    }

    private void seedArrangement(
            UUID tenantId,
            UUID arrangementId,
            UUID studentId
    ) {
        jdbc.update(
                """
                INSERT INTO gts_payment_arrangement (
                    id,
                    tenant_id,
                    student_id
                ) VALUES (?, ?, ?)
                """,
                arrangementId,
                tenantId,
                studentId
        );
    }

    private void seedInstallment(
            UUID tenantId,
            UUID installmentId,
            UUID arrangementId
    ) {
        jdbc.update(
                """
                INSERT INTO gts_payment_installment (
                    id,
                    tenant_id,
                    payment_arrangement_id
                ) VALUES (?, ?, ?)
                """,
                installmentId,
                tenantId,
                arrangementId
        );
    }
}
