package africa.growtogether.platform.school.finance.discount;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static africa.growtogether.platform.school.finance.discount.FinanceStudentDiscountDtos.CreateStudentDiscountRequest;
import static africa.growtogether.platform.school.finance.discount.FinanceStudentDiscountDtos.StudentDiscountRequestView;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(
        classMode =
                DirtiesContext.ClassMode.AFTER_CLASS
)
@SpringBootTest
class FinanceStudentDiscountPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_fin_b4_s2a"
                    )
                    .withUsername(
                            "growtogether"
                    )
                    .withPassword(
                            "growtogether"
                    );

    @DynamicPropertySource
    static void properties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "spring.datasource.url",
                postgres::getJdbcUrl
        );

        registry.add(
                "spring.datasource.username",
                postgres::getUsername
        );

        registry.add(
                "spring.datasource.password",
                postgres::getPassword
        );

        registry.add(
                "spring.flyway.enabled",
                () -> "true"
        );

        registry.add(
                "spring.data.redis.repositories.enabled",
                () -> "false"
        );
    }

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private FinanceStudentDiscountService service;

    @Test
    void studentDiscountRequestPersistsReadsListsAndRemainsTenantScoped() {
        UUID tenantA =
                createTenant(
                        unique(
                                "S2A_A"
                        )
                );

        UUID tenantB =
                createTenant(
                        unique(
                                "S2A_B"
                        )
                );

        UUID studentA =
                createStudent(
                        tenantA,
                        unique(
                                "STA"
                        )
                );

        UUID studentB =
                createStudent(
                        tenantB,
                        unique(
                                "STB"
                        )
                );

        UUID accountA =
                createFinancialAccount(
                        tenantA,
                        studentA,
                        unique(
                                "ACCA"
                        ),
                        "UGX",
                        "ACTIVE",
                        "ACTIVE"
                );

        UUID accountB =
                createFinancialAccount(
                        tenantB,
                        studentB,
                        unique(
                                "ACCB"
                        ),
                        "UGX",
                        "ACTIVE",
                        "ACTIVE"
                );

        UUID schemeA =
                createDiscountScheme(
                        tenantA,
                        unique(
                                "SCHA"
                        ),
                        true,
                        "ACTIVE"
                );

        UUID schemeB =
                createDiscountScheme(
                        tenantB,
                        unique(
                                "SCHB"
                        ),
                        true,
                        "ACTIVE"
                );

        UUID actor =
                UUID.randomUUID();

        String referenceB =
                unique(
                        "REQ_B"
                );

        String referenceA =
                unique(
                        "REQ_A"
                );

        StudentDiscountRequestView createdB =
                service.createStudentDiscountRequest(
                        tenantA,
                        request(
                                referenceB,
                                studentA,
                                accountA,
                                schemeA,
                                LocalDate.of(
                                        2026,
                                        1,
                                        1
                                ),
                                null
                        ),
                        actor
                );

        assertEquals(
                tenantA,
                createdB.tenantId()
        );

        assertEquals(
                referenceB,
                createdB.discountReference()
        );

        assertEquals(
                studentA,
                createdB.studentId()
        );

        assertEquals(
                accountA,
                createdB.studentFinancialAccountId()
        );

        assertEquals(
                schemeA,
                createdB.discountSchemeId()
        );

        assertEquals(
                "PENDING",
                createdB.discountStatus()
        );

        assertEquals(
                "ACTIVE",
                createdB.status()
        );

        assertNotNull(
                createdB.requestedAt()
        );

        assertEquals(
                actor,
                createdB.requestedBy()
        );

        assertNull(
                createdB.approvedDiscountValue()
        );

        assertNull(
                createdB.approvedDiscountAmount()
        );

        assertNull(
                createdB.approvedAt()
        );

        assertNull(
                createdB.approvedBy()
        );

        Map<String, Object> persisted =
                jdbc.queryForMap(
                        """
                        SELECT
                            discount_reference,
                            discount_status,
                            status,
                            requested_at,
                            requested_by,
                            approved_discount_value,
                            approved_discount_amount,
                            approved_at,
                            approved_by
                        FROM gts_student_fee_discount
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        tenantA,
                        createdB.id()
                );

        assertEquals(
                referenceB,
                persisted.get(
                        "discount_reference"
                )
        );

        assertEquals(
                "PENDING",
                persisted.get(
                        "discount_status"
                )
        );

        assertEquals(
                "ACTIVE",
                persisted.get(
                        "status"
                )
        );

        assertNotNull(
                persisted.get(
                        "requested_at"
                )
        );

        assertEquals(
                actor,
                persisted.get(
                        "requested_by"
                )
        );

        assertNull(
                persisted.get(
                        "approved_discount_value"
                )
        );

        assertNull(
                persisted.get(
                        "approved_discount_amount"
                )
        );

        assertNull(
                persisted.get(
                        "approved_at"
                )
        );

        assertNull(
                persisted.get(
                        "approved_by"
                )
        );

        StudentDiscountRequestView retrieved =
                service.getStudentDiscountRequest(
                        tenantA,
                        createdB.id()
                );

        assertEquals(
                createdB.id(),
                retrieved.id()
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.getStudentDiscountRequest(
                                tenantB,
                                createdB.id()
                        )
        );

        assertTrue(
                service.listStudentDiscountRequests(
                        tenantB
                ).isEmpty()
        );

        StudentDiscountRequestView createdA =
                service.createStudentDiscountRequest(
                        tenantA,
                        request(
                                referenceA,
                                studentA,
                                accountA,
                                schemeA,
                                LocalDate.of(
                                        2026,
                                        1,
                                        1
                                ),
                                null
                        ),
                        actor
                );

        List<StudentDiscountRequestView> tenantAList =
                service.listStudentDiscountRequests(
                        tenantA
                );

        assertEquals(
                2,
                tenantAList.size()
        );

        assertEquals(
                referenceA,
                tenantAList.get(
                        0
                ).discountReference()
        );

        assertEquals(
                referenceB,
                tenantAList.get(
                        1
                ).discountReference()
        );

        assertEquals(
                createdA.id(),
                tenantAList.get(
                        0
                ).id()
        );

        StudentDiscountRequestView tenantBSameReference =
                service.createStudentDiscountRequest(
                        tenantB,
                        request(
                                referenceB,
                                studentB,
                                accountB,
                                schemeB,
                                LocalDate.of(
                                        2026,
                                        1,
                                        1
                                ),
                                null
                        ),
                        UUID.randomUUID()
                );

        assertEquals(
                referenceB,
                tenantBSameReference.discountReference()
        );

        List<StudentDiscountRequestView> tenantBList =
                service.listStudentDiscountRequests(
                        tenantB
                );

        assertEquals(
                1,
                tenantBList.size()
        );

        assertEquals(
                tenantBSameReference.id(),
                tenantBList.get(
                        0
                ).id()
        );

        assertFalse(
                tenantBList.stream()
                        .anyMatch(
                                value ->
                                        value.id().equals(
                                                createdB.id()
                                        )
                        )
        );
    }

    @Test
    void studentDiscountRequestDependenciesRemainTenantSafe() {
        UUID tenantA =
                createTenant(
                        unique(
                                "S2A_DA"
                        )
                );

        UUID tenantB =
                createTenant(
                        unique(
                                "S2A_DB"
                        )
                );

        UUID studentA1 =
                createStudent(
                        tenantA,
                        unique(
                                "STA1"
                        )
                );

        UUID studentA2 =
                createStudent(
                        tenantA,
                        unique(
                                "STA2"
                        )
                );

        UUID studentB =
                createStudent(
                        tenantB,
                        unique(
                                "STB1"
                        )
                );

        UUID activeAccountA =
                createFinancialAccount(
                        tenantA,
                        studentA1,
                        unique(
                                "ACCA1"
                        ),
                        "UGX",
                        "ACTIVE",
                        "ACTIVE"
                );

        UUID mismatchAccountA =
                createFinancialAccount(
                        tenantA,
                        studentA2,
                        unique(
                                "ACCA2"
                        ),
                        "UGX",
                        "ACTIVE",
                        "ACTIVE"
                );

        UUID inactiveAccountA =
                createFinancialAccount(
                        tenantA,
                        studentA1,
                        unique(
                                "ACCA3"
                        ),
                        "USD",
                        "PENDING",
                        "ACTIVE"
                );

        UUID accountB =
                createFinancialAccount(
                        tenantB,
                        studentB,
                        unique(
                                "ACCB1"
                        ),
                        "UGX",
                        "ACTIVE",
                        "ACTIVE"
                );

        UUID activeSchemeA =
                createDiscountScheme(
                        tenantA,
                        unique(
                                "SCHA1"
                        ),
                        true,
                        "ACTIVE"
                );

        UUID inactiveFlagSchemeA =
                createDiscountScheme(
                        tenantA,
                        unique(
                                "SCHA2"
                        ),
                        false,
                        "ACTIVE"
                );

        UUID nonActiveStatusSchemeA =
                createDiscountScheme(
                        tenantA,
                        unique(
                                "SCHA3"
                        ),
                        true,
                        "INACTIVE"
                );

        UUID schemeB =
                createDiscountScheme(
                        tenantB,
                        unique(
                                "SCHB1"
                        ),
                        true,
                        "ACTIVE"
                );

        UUID actor =
                UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createStudentDiscountRequest(
                                tenantA,
                                request(
                                        unique(
                                                "XST"
                                        ),
                                        studentB,
                                        activeAccountA,
                                        activeSchemeA,
                                        LocalDate.of(
                                                2026,
                                                1,
                                                1
                                        ),
                                        null
                                ),
                                actor
                        )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createStudentDiscountRequest(
                                tenantA,
                                request(
                                        unique(
                                                "XAC"
                                        ),
                                        studentA1,
                                        accountB,
                                        activeSchemeA,
                                        LocalDate.of(
                                                2026,
                                                1,
                                                1
                                        ),
                                        null
                                ),
                                actor
                        )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createStudentDiscountRequest(
                                tenantA,
                                request(
                                        unique(
                                                "MIS"
                                        ),
                                        studentA1,
                                        mismatchAccountA,
                                        activeSchemeA,
                                        LocalDate.of(
                                                2026,
                                                1,
                                                1
                                        ),
                                        null
                                ),
                                actor
                        )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createStudentDiscountRequest(
                                tenantA,
                                request(
                                        unique(
                                                "IAC"
                                        ),
                                        studentA1,
                                        inactiveAccountA,
                                        activeSchemeA,
                                        LocalDate.of(
                                                2026,
                                                1,
                                                1
                                        ),
                                        null
                                ),
                                actor
                        )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createStudentDiscountRequest(
                                tenantA,
                                request(
                                        unique(
                                                "XSC"
                                        ),
                                        studentA1,
                                        activeAccountA,
                                        schemeB,
                                        LocalDate.of(
                                                2026,
                                                1,
                                                1
                                        ),
                                        null
                                ),
                                actor
                        )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createStudentDiscountRequest(
                                tenantA,
                                request(
                                        unique(
                                                "ISF"
                                        ),
                                        studentA1,
                                        activeAccountA,
                                        inactiveFlagSchemeA,
                                        LocalDate.of(
                                                2026,
                                                1,
                                                1
                                        ),
                                        null
                                ),
                                actor
                        )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createStudentDiscountRequest(
                                tenantA,
                                request(
                                        unique(
                                                "ISS"
                                        ),
                                        studentA1,
                                        activeAccountA,
                                        nonActiveStatusSchemeA,
                                        LocalDate.of(
                                                2026,
                                                1,
                                                1
                                        ),
                                        null
                                ),
                                actor
                        )
        );

        String duplicateReference =
                unique(
                        "DUP"
                );

        service.createStudentDiscountRequest(
                tenantA,
                request(
                        duplicateReference,
                        studentA1,
                        activeAccountA,
                        activeSchemeA,
                        LocalDate.of(
                                2026,
                                1,
                                1
                        ),
                        null
                ),
                actor
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createStudentDiscountRequest(
                                tenantA,
                                request(
                                        duplicateReference,
                                        studentA1,
                                        activeAccountA,
                                        activeSchemeA,
                                        LocalDate.of(
                                                2026,
                                                1,
                                                1
                                        ),
                                        null
                                ),
                                actor
                        )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.createStudentDiscountRequest(
                                tenantA,
                                request(
                                        unique(
                                                "DATE"
                                        ),
                                        studentA1,
                                        activeAccountA,
                                        activeSchemeA,
                                        LocalDate.of(
                                                2026,
                                                2,
                                                2
                                        ),
                                        LocalDate.of(
                                                2026,
                                                2,
                                                1
                                        )
                                ),
                                actor
                        )
        );
    }


    @Test
    void studentDiscountDecisionLifecyclePersistsAndRemainsTenantSafe() {
        UUID tenantA =
                createTenant(
                        unique(
                                "S2B_A"
                        )
                );

        UUID tenantB =
                createTenant(
                        unique(
                                "S2B_B"
                        )
                );

        UUID studentA =
                createStudent(
                        tenantA,
                        unique(
                                "S2B_STA"
                        )
                );

        UUID accountA =
                createFinancialAccount(
                        tenantA,
                        studentA,
                        unique(
                                "S2B_ACCA"
                        ),
                        "UGX",
                        "ACTIVE",
                        "ACTIVE"
                );

        UUID schemeA =
                createDiscountScheme(
                        tenantA,
                        unique(
                                "S2B_SCHA"
                        ),
                        true,
                        "ACTIVE"
                );

        UUID actorId =
                UUID.randomUUID();

        StudentDiscountRequestView approvalTarget =
                service.createStudentDiscountRequest(
                        tenantA,
                        request(
                                unique(
                                        "S2B_APR"
                                ),
                                studentA,
                                accountA,
                                schemeA,
                                LocalDate.of(
                                        2026,
                                        1,
                                        1
                                ),
                                null
                        ),
                        actorId
                );

        StudentDiscountRequestView rejectionTarget =
                service.createStudentDiscountRequest(
                        tenantA,
                        request(
                                unique(
                                        "S2B_REJ"
                                ),
                                studentA,
                                accountA,
                                schemeA,
                                LocalDate.of(
                                        2026,
                                        1,
                                        1
                                ),
                                null
                        ),
                        actorId
                );

        long invoiceCountBefore =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM gts_student_invoice",
                        Long.class
                );

        long invoiceLineCountBefore =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM gts_student_invoice_line",
                        Long.class
                );

        long adjustmentCountBefore =
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM gts_financial_adjustment",
                        Long.class
                );

        StudentDiscountRequestView approved =
                service.approveStudentDiscountRequest(
                        tenantA,
                        approvalTarget.id(),
                        actorId,
                        actorId.toString()
                );

        assertEquals(
                "APPROVED",
                approved.discountStatus()
        );

        assertEquals(
                "ACTIVE",
                approved.status()
        );

        assertNotNull(
                approved.approvedDiscountValue()
        );

        assertEquals(
                0,
                BigDecimal.TEN.compareTo(
                        approved.approvedDiscountValue()
                )
        );

        assertNull(
                approved.approvedDiscountAmount()
        );

        assertNotNull(
                approved.approvedAt()
        );

        assertEquals(
                actorId,
                approved.approvedBy()
        );

        assertEquals(
                approvalTarget.requestedAt(),
                approved.requestedAt()
        );

        assertEquals(
                approvalTarget.requestedBy(),
                approved.requestedBy()
        );

        assertEquals(
                approvalTarget.version() + 1,
                approved.version()
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.approveStudentDiscountRequest(
                                tenantA,
                                approvalTarget.id(),
                                actorId,
                                actorId.toString()
                        )
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.rejectStudentDiscountRequest(
                                tenantA,
                                approvalTarget.id(),
                                actorId,
                                actorId.toString()
                        )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.approveStudentDiscountRequest(
                                tenantB,
                                approvalTarget.id(),
                                UUID.randomUUID(),
                                UUID.randomUUID().toString()
                        )
        );

        StudentDiscountRequestView rejected =
                service.rejectStudentDiscountRequest(
                        tenantA,
                        rejectionTarget.id(),
                        actorId,
                        actorId.toString()
                );

        assertEquals(
                "REJECTED",
                rejected.discountStatus()
        );

        assertEquals(
                "ACTIVE",
                rejected.status()
        );

        assertNull(
                rejected.approvedDiscountValue()
        );

        assertNull(
                rejected.approvedDiscountAmount()
        );

        assertNull(
                rejected.approvedAt()
        );

        assertNull(
                rejected.approvedBy()
        );

        assertEquals(
                rejectionTarget.requestedAt(),
                rejected.requestedAt()
        );

        assertEquals(
                rejectionTarget.requestedBy(),
                rejected.requestedBy()
        );

        assertEquals(
                rejectionTarget.version() + 1,
                rejected.version()
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.rejectStudentDiscountRequest(
                                tenantA,
                                rejectionTarget.id(),
                                actorId,
                                actorId.toString()
                        )
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.approveStudentDiscountRequest(
                                tenantA,
                                rejectionTarget.id(),
                                actorId,
                                actorId.toString()
                        )
        );

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.rejectStudentDiscountRequest(
                                tenantB,
                                rejectionTarget.id(),
                                UUID.randomUUID(),
                                UUID.randomUUID().toString()
                        )
        );

        assertEquals(
                invoiceCountBefore,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM gts_student_invoice",
                        Long.class
                )
        );

        assertEquals(
                invoiceLineCountBefore,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM gts_student_invoice_line",
                        Long.class
                )
        );

        assertEquals(
                adjustmentCountBefore,
                jdbc.queryForObject(
                        "SELECT COUNT(*) FROM gts_financial_adjustment",
                        Long.class
                )
        );
    }

    private CreateStudentDiscountRequest request(
            String reference,
            UUID studentId,
            UUID accountId,
            UUID schemeId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        return new CreateStudentDiscountRequest(
                reference,
                studentId,
                accountId,
                schemeId,
                effectiveFrom,
                effectiveTo,
                null,
                null
        );
    }

    private UUID createTenant(
            String code
    ) {
        UUID organizationId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """,
                organizationId,
                code + "_ORG",
                code + " Organisation"
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
                    ?, ?, ?, ?, 'ACTIVE',
                    CURRENT_TIMESTAMP,
                    0
                )
                """,
                tenantId,
                organizationId,
                code,
                code + " School"
        );

        return tenantId;
    }

    private UUID createStudent(
            UUID tenantId,
            String token
    ) {
        UUID id =
                UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_student (
                    id,
                    tenant_id,
                    student_number,
                    permanent_learner_number,
                    first_name,
                    last_name,
                    date_of_birth,
                    student_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    'Student',
                    'Proof',
                    DATE '2015-01-01',
                    'ACTIVE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    'fin-b4-s2a-proof',
                    CURRENT_TIMESTAMP,
                    'fin-b4-s2a-proof',
                    0
                )
                """,
                id,
                tenantId,
                token,
                token + "_PLN"
        );

        return id;
    }

    private UUID createFinancialAccount(
            UUID tenantId,
            UUID studentId,
            String accountNumber,
            String currencyCode,
            String billingStatus,
            String status
    ) {
        UUID id =
                UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_student_financial_account (
                    id,
                    tenant_id,
                    account_number,
                    student_id,
                    student_enrollment_id,
                    currency_code,
                    opening_balance,
                    current_balance,
                    credit_balance,
                    billing_status,
                    opened_at,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, NULL, ?,
                    0, 0, 0,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    'fin-b4-s2a-proof',
                    CURRENT_TIMESTAMP,
                    'fin-b4-s2a-proof',
                    0
                )
                """,
                id,
                tenantId,
                accountNumber,
                studentId,
                currencyCode,
                billingStatus,
                status
        );

        return id;
    }

    private UUID createDiscountScheme(
            UUID tenantId,
            String schemeCode,
            boolean active,
            String status
    ) {
        UUID id =
                UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gts_fee_discount_scheme (
                    id,
                    tenant_id,
                    scheme_code,
                    scheme_name,
                    discount_type,
                    discount_value,
                    eligibility_rules,
                    effective_from,
                    approval_required,
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
                    'PERCENTAGE',
                    ?,
                    '{}'::jsonb,
                    DATE '2026-01-01',
                    true,
                    ?,
                    ?,
                    CURRENT_TIMESTAMP,
                    'fin-b4-s2a-proof',
                    CURRENT_TIMESTAMP,
                    'fin-b4-s2a-proof',
                    0
                )
                """,
                id,
                tenantId,
                schemeCode,
                schemeCode + " Scheme",
                BigDecimal.TEN,
                active,
                status
        );

        return id;
    }

    private static String unique(
            String prefix
    ) {
        return prefix
                + "_"
                + UUID.randomUUID()
                .toString()
                .replace(
                        "-",
                        ""
                )
                .substring(
                        0,
                        8
                );
    }
}
