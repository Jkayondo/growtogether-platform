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



    @Test
    void approvedPercentageDiscountAppliesAtomicallyToDraftBilling() {

        UUID tenantId =
                createTenant(
                        unique(
                                "S3_T"
                        )
                );

        UUID studentId =
                createStudent(
                        tenantId,
                        unique(
                                "S3_ST"
                        )
                );

        UUID accountId =
                createFinancialAccount(
                        tenantId,
                        studentId,
                        unique(
                                "S3_ACC"
                        ),
                        "UGX",
                        "ACTIVE",
                        "ACTIVE"
                );

        UUID schemeId =
                createDiscountScheme(
                        tenantId,
                        unique(
                                "S3_SCH"
                        ),
                        true,
                        "ACTIVE"
                );

        UUID actorId =
                UUID.randomUUID();

        StudentDiscountRequestView created =
                service.createStudentDiscountRequest(
                        tenantId,
                        request(
                                unique(
                                        "S3_DISC"
                                ),
                                studentId,
                                accountId,
                                schemeId,
                                LocalDate.of(
                                        2026,
                                        1,
                                        1
                                ),
                                null
                        ),
                        actorId
                );

        StudentDiscountRequestView approved =
                service.approveStudentDiscountRequest(
                        tenantId,
                        created.id(),
                        actorId,
                        actorId.toString()
                );

        UUID invoiceId =
                createS3DraftInvoice(
                        tenantId,
                        accountId,
                        studentId,
                        "UGX",
                        new BigDecimal(
                                "100.00"
                        )
                );

        UUID lineId =
                createS3InvoiceLine(
                        tenantId,
                        invoiceId,
                        new BigDecimal(
                                "100.00"
                        )
                );

        StudentDiscountRequestView applied =
                service.applyStudentDiscount(
                        tenantId,
                        approved.id(),
                        new FinanceStudentDiscountDtos.ApplyStudentDiscountRequest(
                                invoiceId
                        ),
                        actorId,
                        actorId.toString()
                );

        assertEquals(
                "ACTIVE",
                applied.discountStatus()
        );

        assertEquals(
                0,
                new BigDecimal(
                        "10.00"
                ).compareTo(
                        applied.approvedDiscountAmount()
                )
        );

        Map<String, Object> line =
                jdbc.queryForMap(
                        """
                        SELECT
                            discount_amount,
                            net_amount,
                            version
                        FROM gts_student_invoice_line
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        tenantId,
                        lineId
                );

        assertEquals(
                0,
                new BigDecimal(
                        "10.00"
                ).compareTo(
                        (BigDecimal) line.get(
                                "discount_amount"
                        )
                )
        );

        assertEquals(
                0,
                new BigDecimal(
                        "90.00"
                ).compareTo(
                        (BigDecimal) line.get(
                                "net_amount"
                        )
                )
        );

        Map<String, Object> invoice =
                jdbc.queryForMap(
                        """
                        SELECT
                            discount_amount,
                            total_amount,
                            outstanding_amount,
                            invoice_status,
                            version
                        FROM gts_student_invoice
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        tenantId,
                        invoiceId
                );

        assertEquals(
                "DRAFT",
                invoice.get(
                        "invoice_status"
                )
        );

        assertEquals(
                0,
                new BigDecimal(
                        "10.00"
                ).compareTo(
                        (BigDecimal) invoice.get(
                                "discount_amount"
                        )
                )
        );

        assertEquals(
                0,
                new BigDecimal(
                        "90.00"
                ).compareTo(
                        (BigDecimal) invoice.get(
                                "total_amount"
                        )
                )
        );

        assertEquals(
                0,
                new BigDecimal(
                        "90.00"
                ).compareTo(
                        (BigDecimal) invoice.get(
                                "outstanding_amount"
                        )
                )
        );

        Map<String, Object> adjustment =
                jdbc.queryForMap(
                        """
                        SELECT
                            adjustment_reference,
                            student_financial_account_id,
                            invoice_id,
                            invoice_line_id,
                            adjustment_type,
                            adjustment_amount,
                            adjustment_status,
                            requested_by,
                            applied_by,
                            applied_at,
                            status
                        FROM gts_financial_adjustment
                        WHERE tenant_id = ?
                          AND adjustment_reference = ?
                        """,
                        tenantId,
                        "SFD-" + approved.id()
                );

        assertEquals(
                accountId,
                adjustment.get(
                        "student_financial_account_id"
                )
        );

        assertEquals(
                invoiceId,
                adjustment.get(
                        "invoice_id"
                )
        );

        assertNull(
                adjustment.get(
                        "invoice_line_id"
                )
        );

        assertEquals(
                "OTHER",
                adjustment.get(
                        "adjustment_type"
                )
        );

        assertEquals(
                "APPLIED",
                adjustment.get(
                        "adjustment_status"
                )
        );

        assertEquals(
                "ACTIVE",
                adjustment.get(
                        "status"
                )
        );

        assertEquals(
                0,
                new BigDecimal(
                        "10.00"
                ).compareTo(
                        (BigDecimal) adjustment.get(
                                "adjustment_amount"
                        )
                )
        );

        assertEquals(
                actorId,
                adjustment.get(
                        "requested_by"
                )
        );

        assertEquals(
                actorId,
                adjustment.get(
                        "applied_by"
                )
        );

        assertNotNull(
                adjustment.get(
                        "applied_at"
                )
        );

        long adjustmentCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_financial_adjustment
                        WHERE tenant_id = ?
                          AND adjustment_reference = ?
                        """,
                        Long.class,
                        tenantId,
                        "SFD-" + approved.id()
                );

        assertEquals(
                1L,
                adjustmentCount
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        service.applyStudentDiscount(
                                tenantId,
                                approved.id(),
                                new FinanceStudentDiscountDtos.ApplyStudentDiscountRequest(
                                        invoiceId
                                ),
                                actorId,
                                actorId.toString()
                        )
        );

        assertEquals(
                1L,
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_financial_adjustment
                        WHERE tenant_id = ?
                          AND adjustment_reference = ?
                        """,
                        Long.class,
                        tenantId,
                        "SFD-" + approved.id()
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

    private UUID createS3DraftInvoice(
            UUID tenantId,
            UUID accountId,
            UUID studentId,
            String currencyCode,
            BigDecimal amount
    ) {

        return jdbc.queryForObject(
                """
                INSERT INTO gts_student_invoice (
                    tenant_id,
                    invoice_number,
                    student_financial_account_id,
                    student_id,
                    invoice_date,
                    currency_code,
                    subtotal_amount,
                    discount_amount,
                    tax_amount,
                    total_amount,
                    paid_amount,
                    outstanding_amount,
                    invoice_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    DATE '2026-01-15',
                    ?,
                    ?,
                    0,
                    0,
                    ?,
                    0,
                    ?,
                    'DRAFT',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    'fin-b4-s3-proof',
                    CURRENT_TIMESTAMP,
                    'fin-b4-s3-proof',
                    0
                )
                RETURNING id
                """,
                UUID.class,
                tenantId,
                unique(
                        "S3_INV"
                ),
                accountId,
                studentId,
                currencyCode,
                amount,
                amount,
                amount
        );
    }

    private UUID createS3InvoiceLine(
            UUID tenantId,
            UUID invoiceId,
            BigDecimal amount
    ) {

        return jdbc.queryForObject(
                """
                INSERT INTO gts_student_invoice_line (
                    tenant_id,
                    invoice_id,
                    fee_item_id,
                    fee_structure_item_id,
                    line_description,
                    quantity,
                    unit_amount,
                    gross_amount,
                    discount_amount,
                    tax_amount,
                    net_amount,
                    line_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?,
                    NULL,
                    NULL,
                    'FIN-B4-S3 Proof Line',
                    1,
                    ?,
                    ?,
                    0,
                    0,
                    ?,
                    'ACTIVE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    'fin-b4-s3-proof',
                    CURRENT_TIMESTAMP,
                    'fin-b4-s3-proof',
                    0
                )
                RETURNING id
                """,
                UUID.class,
                tenantId,
                invoiceId,
                amount,
                amount,
                amount
        );
    }

}
