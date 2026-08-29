package africa.growtogether.platform.school.admission.payment;

import org.flywaydb.core.Flyway;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@Testcontainers
class AdmissionPaymentAllocationPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_admission_payment_test"
                    )
                    .withUsername(
                            "gt_test"
                    )
                    .withPassword(
                            "gt_test"
                    );

    private static JdbcTemplate jdbc;

    private static DataSourceTransactionManager transactionManager;

    @BeforeAll
    static void migrateDatabase() {

        Flyway.configure()
                .dataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword()
                )
                .locations(
                        "classpath:db/migration"
                )
                .load()
                .migrate();

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource();

        dataSource.setUrl(
                POSTGRES.getJdbcUrl()
        );

        dataSource.setUsername(
                POSTGRES.getUsername()
        );

        dataSource.setPassword(
                POSTGRES.getPassword()
        );

        jdbc =
                new JdbcTemplate(
                        dataSource
                );

        transactionManager =
                new DataSourceTransactionManager(
                        dataSource
                );
    }

    @Test
    void concurrentObligationsCannotOverAllocateSamePayment()
            throws Exception {

        Fixture fixture =
                createFixture();

        CountDownLatch firstAllocationInserted =
                new CountDownLatch(
                        1
                );

        CountDownLatch allowFirstCommit =
                new CountDownLatch(
                        1
                );

        CountDownLatch secondAboutToLockPayment =
                new CountDownLatch(
                        1
                );

        ExecutorService executor =
                Executors.newFixedThreadPool(
                        2
                );

        try {

            Future<BigDecimal> first =
                    executor.submit(
                            () ->
                                    allocateWithLocks(
                                            fixture,
                                            fixture.obligationOneId(),
                                            null,
                                            () -> {
                                                firstAllocationInserted
                                                        .countDown();

                                                await(
                                                        allowFirstCommit
                                                );
                                            }
                                    )
                    );

            /*
             * Thread 1 has:
             *
             * 1. locked obligation one,
             * 2. locked the shared payment,
             * 3. inserted UGX 50,000,
             * 4. deliberately not committed yet.
             */
            firstAllocationInserted.await();

            Future<BigDecimal> second =
                    executor.submit(
                            () ->
                                    allocateWithLocks(
                                            fixture,
                                            fixture.obligationTwoId(),
                                            secondAboutToLockPayment::countDown,
                                            null
                                    )
                    );

            /*
             * Thread 2 has already locked its different obligation and
             * is now about to request the SAME payment row lock.
             */
            secondAboutToLockPayment.await();

            /*
             * Because Thread 1 still owns the payment lock, Thread 2
             * must not have completed its transaction.
             */
            assertFalse(
                    second.isDone(),
                    "Second allocation must wait for the shared payment lock"
            );

            allowFirstCommit.countDown();

            BigDecimal firstAllocated =
                    first.get();

            BigDecimal secondAllocated =
                    second.get();

            assertMoney(
                    "50000.00",
                    firstAllocated
            );

            assertMoney(
                    "0.00",
                    secondAllocated
            );

            BigDecimal totalAllocated =
                    jdbc.queryForObject(
                            """
                            SELECT COALESCE(
                                SUM(allocated_amount),
                                0
                            )
                            FROM gts_admission_payment_allocation
                            WHERE tenant_id = ?
                              AND eip_payment_transaction_id = ?
                              AND allocation_status = 'APPLIED'
                              AND status = 'ACTIVE'
                            """,
                            BigDecimal.class,
                            fixture.tenantId(),
                            fixture.paymentId()
                    );

            assertMoney(
                    "50000.00",
                    totalAllocated
            );

            Integer allocationCount =
                    jdbc.queryForObject(
                            """
                            SELECT COUNT(*)
                            FROM gts_admission_payment_allocation
                            WHERE tenant_id = ?
                              AND eip_payment_transaction_id = ?
                              AND allocation_status = 'APPLIED'
                              AND status = 'ACTIVE'
                            """,
                            Integer.class,
                            fixture.tenantId(),
                            fixture.paymentId()
                    );

            assertEquals(
                    1,
                    allocationCount
            );

        } finally {

            /*
             * Prevent a failed assertion from leaving Thread 1 held
             * inside the transaction.
             */
            allowFirstCommit.countDown();

            executor.shutdownNow();
        }
    }

    private BigDecimal allocateWithLocks(
            Fixture fixture,
            UUID obligationId,
            Runnable beforePaymentLock,
            Runnable beforeCommit
    ) {

        TransactionTemplate transaction =
                new TransactionTemplate(
                        transactionManager
                );

        BigDecimal result =
                transaction.execute(
                        status -> {

                            /*
                             * Mirrors:
                             *
                             * AdmissionPaymentObligationRepository
                             *     .findForUpdate(...)
                             */
                            BigDecimal amountDue =
                                    jdbc.queryForObject(
                                            """
                                            SELECT
                                                required_amount
                                                - waived_amount
                                            FROM gts_admission_payment_obligation
                                            WHERE tenant_id = ?
                                              AND id = ?
                                            FOR UPDATE
                                            """,
                                            BigDecimal.class,
                                            fixture.tenantId(),
                                            obligationId
                                    );

                            if (beforePaymentLock != null) {
                                beforePaymentLock.run();
                            }

                            /*
                             * Mirrors:
                             *
                             * PaymentTransactionRepository
                             *     .findForUpdate(...)
                             *
                             * Both service repositories use
                             * PESSIMISTIC_WRITE, whose PostgreSQL row
                             * behavior is SELECT ... FOR UPDATE.
                             */
                            BigDecimal paymentAmount =
                                    jdbc.queryForObject(
                                            """
                                            SELECT amount
                                            FROM eip_payment_transactions
                                            WHERE tenant_id = ?
                                              AND id = ?
                                              AND payment_status = 'SUCCEEDED'
                                            FOR UPDATE
                                            """,
                                            BigDecimal.class,
                                            fixture.tenantId(),
                                            fixture.paymentId()
                                    );

                            BigDecimal obligationAlreadyAllocated =
                                    jdbc.queryForObject(
                                            """
                                            SELECT COALESCE(
                                                SUM(allocated_amount),
                                                0
                                            )
                                            FROM gts_admission_payment_allocation
                                            WHERE tenant_id = ?
                                              AND admission_payment_obligation_id = ?
                                              AND allocation_status = 'APPLIED'
                                              AND status = 'ACTIVE'
                                            """,
                                            BigDecimal.class,
                                            fixture.tenantId(),
                                            obligationId
                                    );

                            BigDecimal paymentAlreadyAllocated =
                                    jdbc.queryForObject(
                                            """
                                            SELECT COALESCE(
                                                SUM(allocated_amount),
                                                0
                                            )
                                            FROM gts_admission_payment_allocation
                                            WHERE tenant_id = ?
                                              AND eip_payment_transaction_id = ?
                                              AND allocation_status = 'APPLIED'
                                              AND status = 'ACTIVE'
                                            """,
                                            BigDecimal.class,
                                            fixture.tenantId(),
                                            fixture.paymentId()
                                    );

                            BigDecimal outstanding =
                                    amountDue
                                            .subtract(
                                                    obligationAlreadyAllocated
                                            )
                                            .max(
                                                    BigDecimal.ZERO
                                            );

                            BigDecimal available =
                                    paymentAmount
                                            .subtract(
                                                    paymentAlreadyAllocated
                                            )
                                            .max(
                                                    BigDecimal.ZERO
                                            );

                            BigDecimal allocatedAmount =
                                    outstanding.min(
                                            available
                                    );

                            if (
                                    allocatedAmount.signum()
                                            > 0
                            ) {

                                UUID allocationId =
                                        UUID.randomUUID();

                                jdbc.update(
                                        """
                                        INSERT INTO
                                        gts_admission_payment_allocation (
                                            id,
                                            tenant_id,
                                            admission_payment_obligation_id,
                                            eip_payment_transaction_id,
                                            allocated_amount,
                                            allocation_status,
                                            applied_at,
                                            applied_by,
                                            status,
                                            created_at,
                                            created_by,
                                            updated_at,
                                            updated_by,
                                            version
                                        )
                                        VALUES (
                                            ?, ?, ?, ?, ?,
                                            'APPLIED',
                                            CURRENT_TIMESTAMP,
                                            ?,
                                            'ACTIVE',
                                            CURRENT_TIMESTAMP,
                                            ?,
                                            CURRENT_TIMESTAMP,
                                            ?,
                                            0
                                        )
                                        """,
                                        allocationId,
                                        fixture.tenantId(),
                                        obligationId,
                                        fixture.paymentId(),
                                        allocatedAmount,
                                        fixture.actorId(),
                                        "a12-payment-concurrency-test",
                                        "a12-payment-concurrency-test"
                                );
                            }

                            if (beforeCommit != null) {
                                beforeCommit.run();
                            }

                            return allocatedAmount;
                        }
                );

        if (result == null) {
            throw new IllegalStateException(
                    "Allocation transaction returned no result"
            );
        }

        return result;
    }

    private Fixture createFixture() {

        UUID organizationId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID educationLevelId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        UUID applicationId =
                UUID.randomUUID();

        UUID feeCategoryId =
                UUID.randomUUID();

        UUID feeItemOneId =
                UUID.randomUUID();

        UUID feeItemTwoId =
                UUID.randomUUID();

        UUID obligationOneId =
                UUID.randomUUID();

        UUID obligationTwoId =
                UUID.randomUUID();

        UUID paymentId =
                UUID.randomUUID();

        UUID actorId =
                UUID.randomUUID();

        String suffix =
                shortId(
                        tenantId
                );

        String auditUser =
                "a12-payment-concurrency-test";

        String applicationNumber =
                "PPIS-2026-ADM-" + suffix;

        jdbc.update(
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (
                    ?, ?, ?, CURRENT_TIMESTAMP
                )
                """,
                organizationId,
                "ORG-" + suffix,
                "A12 Payment Test Organisation " + suffix
        );

        jdbc.update(
                """
                INSERT INTO eiam_tenant (
                    id,
                    organization_id,
                    code,
                    name,
                    status,
                    created_at,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    0
                )
                """,
                tenantId,
                organizationId,
                "TEN-" + suffix,
                "A12 Payment Test Tenant " + suffix
        );

        jdbc.update(
                """
                INSERT INTO gts_school_profile (
                    id,
                    tenant_id,
                    school_code,
                    school_name,
                    country_code,
                    default_currency,
                    timezone,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    'UG',
                    'UGX',
                    'Africa/Kampala',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                schoolProfileId,
                tenantId,
                "PPIS-" + suffix,
                "A12 Payment Test School",
                auditUser,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO gts_campus (
                    id,
                    tenant_id,
                    school_profile_id,
                    campus_code,
                    campus_name,
                    main_campus,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    TRUE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                campusId,
                tenantId,
                schoolProfileId,
                "MAIN-" + suffix,
                "Main Campus",
                auditUser,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO gts_academic_year (
                    id,
                    tenant_id,
                    academic_year_code,
                    academic_year_name,
                    start_date,
                    end_date,
                    current_year,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    ?, ?,
                    TRUE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                academicYearId,
                tenantId,
                "AY-2026-" + suffix,
                "Academic Year 2026",
                Date.valueOf(
                        "2026-01-01"
                ),
                Date.valueOf(
                        "2026-12-31"
                ),
                auditUser,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO gts_education_level (
                    id,
                    tenant_id,
                    level_code,
                    level_name,
                    sequence_number,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    1,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                educationLevelId,
                tenantId,
                "PRIMARY-" + suffix,
                "Primary",
                auditUser,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO gts_class_grade (
                    id,
                    tenant_id,
                    education_level_id,
                    class_code,
                    class_name,
                    sequence_number,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    1,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                classGradeId,
                tenantId,
                educationLevelId,
                "P1-" + suffix,
                "Primary One",
                auditUser,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO gts_admission_application (
                    id,
                    tenant_id,
                    application_number,
                    academic_year_id,
                    campus_id,
                    desired_class_grade_id,
                    application_date,
                    admission_status,
                    submission_channel,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?,
                    CURRENT_DATE,
                    'DRAFT',
                    'OFFICE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                applicationId,
                tenantId,
                applicationNumber,
                academicYearId,
                campusId,
                classGradeId,
                auditUser,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO gts_fee_category (
                    id,
                    tenant_id,
                    category_code,
                    category_name,
                    category_type,
                    refundable,
                    mandatory_by_default,
                    recurring,
                    active,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    'ADMISSION',
                    FALSE,
                    TRUE,
                    FALSE,
                    TRUE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                feeCategoryId,
                tenantId,
                "ADM-" + suffix,
                "Admission Fees",
                auditUser,
                auditUser
        );

        createFeeItem(
                tenantId,
                feeCategoryId,
                feeItemOneId,
                "ADM-ONE-" + suffix,
                "Admission Charge One",
                auditUser
        );

        createFeeItem(
                tenantId,
                feeCategoryId,
                feeItemTwoId,
                "ADM-TWO-" + suffix,
                "Admission Charge Two",
                auditUser
        );

        createObligation(
                tenantId,
                applicationId,
                feeItemOneId,
                obligationOneId,
                auditUser
        );

        createObligation(
                tenantId,
                applicationId,
                feeItemTwoId,
                obligationTwoId,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO eip_payment_transactions (
                    id,
                    tenant_id,
                    merchant_reference,
                    payer_reference,
                    amount,
                    currency,
                    payment_channel,
                    connector_code,
                    idempotency_key,
                    payment_status,
                    provider_reference,
                    completed_at,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?, ?, ?,
                    50000.00,
                    'UGX',
                    'CASH_REFERENCE',
                    'A12-TEST',
                    ?,
                    'SUCCEEDED',
                    ?,
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0,
                    'ACTIVE'
                )
                """,
                paymentId,
                tenantId,
                applicationNumber,
                "PAYER-" + suffix,
                "IDEM-" + paymentId,
                "PROVIDER-" + suffix,
                auditUser,
                auditUser
        );

        return new Fixture(
                tenantId,
                obligationOneId,
                obligationTwoId,
                paymentId,
                actorId
        );
    }

    private void createFeeItem(
            UUID tenantId,
            UUID feeCategoryId,
            UUID feeItemId,
            String itemCode,
            String itemName,
            String auditUser
    ) {

        jdbc.update(
                """
                INSERT INTO gts_fee_item (
                    id,
                    tenant_id,
                    fee_category_id,
                    item_code,
                    item_name,
                    currency_code,
                    default_amount,
                    charge_frequency,
                    quantity_allowed,
                    partial_payment_allowed,
                    tax_applicable,
                    active,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    'UGX',
                    50000.00,
                    'ONCE',
                    FALSE,
                    TRUE,
                    FALSE,
                    TRUE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                feeItemId,
                tenantId,
                feeCategoryId,
                itemCode,
                itemName,
                auditUser,
                auditUser
        );
    }

    private void createObligation(
            UUID tenantId,
            UUID applicationId,
            UUID feeItemId,
            UUID obligationId,
            String auditUser
    ) {

        jdbc.update(
                """
                INSERT INTO gts_admission_payment_obligation (
                    id,
                    tenant_id,
                    admission_application_id,
                    fee_item_id,
                    currency_code,
                    required_amount,
                    waived_amount,
                    required_for_onboarding,
                    gate_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    'UGX',
                    50000.00,
                    0.00,
                    TRUE,
                    'PAYMENT_REQUIRED',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                obligationId,
                tenantId,
                applicationId,
                feeItemId,
                auditUser,
                auditUser
        );
    }

    private void await(
            CountDownLatch latch
    ) {

        try {

            latch.await();

        } catch (InterruptedException exception) {

            Thread.currentThread()
                    .interrupt();

            throw new IllegalStateException(
                    "Concurrency test interrupted",
                    exception
            );
        }
    }

    private void assertMoney(
            String expected,
            BigDecimal actual
    ) {

        assertEquals(
                0,
                new BigDecimal(
                        expected
                ).compareTo(
                        actual
                )
        );
    }

    private String shortId(
            UUID id
    ) {

        return id
                .toString()
                .substring(
                        0,
                        8
                )
                .toUpperCase();
    }

    private record Fixture(
            UUID tenantId,
            UUID obligationOneId,
            UUID obligationTwoId,
            UUID paymentId,
            UUID actorId
    ) {
    }
}
