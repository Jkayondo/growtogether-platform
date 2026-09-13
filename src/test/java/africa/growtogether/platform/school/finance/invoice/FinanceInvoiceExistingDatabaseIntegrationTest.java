package africa.growtogether.platform.school.finance.invoice;

import static africa.growtogether.platform.school.finance.assignment.FinanceFeeAssignmentDtos.*;
import static africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.*;
import static africa.growtogether.platform.school.finance.invoice.FinanceInvoiceDtos.*;
import static org.junit.jupiter.api.Assertions.*;

import africa.growtogether.platform.school.finance.assignment.FinanceFeeAssignmentService;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SpringBootTest
class FinanceInvoiceExistingDatabaseIntegrationTest {

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    FinanceFoundationService foundationService;

    @Autowired
    FinanceFeeAssignmentService assignmentService;

    @Autowired
    FinanceInvoiceService invoiceService;

    @Test
    void draftInvoicePersistsLinesIsTenantSafeAndRejectsDuplicateNumber() {

        String suffix =
                UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase();

        RootFixture root =
                createRootFixture(
                        "A" + suffix
                );

        UUID otherTenantId =
                createOtherTenant(
                        "B" + suffix
                );

        UUID tenantId =
                root.tenantId();

        UUID academicYearId =
                root.academicYearId();

        UUID studentId =
                root.studentId();

        UUID actorId =
                UUID.randomUUID();

        String actor =
                actorId.toString();

        LocalDate effectiveFrom =
                LocalDate.now();

        FeeCategoryView category =
                foundationService.createFeeCategory(
                        tenantId,
                        new CreateFeeCategoryRequest(
                                "FINB3_CAT_" + suffix,
                                "FIN-B3 Tuition",
                                "FIN-B3 draft invoice PostgreSQL proof",
                                "TUITION",
                                null,
                                false,
                                true,
                                true
                        ),
                        actor
                );

        FeeItemView item =
                foundationService.createFeeItem(
                        tenantId,
                        new CreateFeeItemRequest(
                                category.id(),
                                "FINB3_ITEM_" + suffix,
                                "FIN-B3 Term Tuition",
                                null,
                                "UGX",
                                new BigDecimal(
                                        "500000.00"
                                ),
                                "TERM",
                                false,
                                true,
                                false,
                                null
                        ),
                        actor
                );

        FeeStructureView structure =
                foundationService.createFeeStructure(
                        tenantId,
                        new CreateFeeStructureRequest(
                                "FINB3_FS_" + suffix,
                                "FIN-B3 Fee Structure",
                                null,
                                academicYearId,
                                null,
                                null,
                                null,
                                null,
                                null,
                                null,
                                "UGX",
                                effectiveFrom,
                                null
                        ),
                        actor
                );

        foundationService.addFeeStructureItem(
                tenantId,
                structure.id(),
                new AddFeeStructureItemRequest(
                        item.id(),
                        new BigDecimal(
                                "500000.00"
                        ),
                        BigDecimal.ONE,
                        true,
                        false,
                        null,
                        1
                ),
                actor
        );

        foundationService.approveFeeStructure(
                tenantId,
                structure.id(),
                actorId,
                actor
        );

        FeeStructureView activeStructure =
                foundationService.activateFeeStructure(
                        tenantId,
                        structure.id(),
                        actor
                );

        assertEquals(
                "ACTIVE",
                activeStructure.structureStatus()
        );

        StudentFinancialAccountView account =
                foundationService.openStudentAccount(
                        tenantId,
                        new OpenStudentFinancialAccountRequest(
                                "FINB3_ACC_" + suffix,
                                studentId,
                                null,
                                "UGX",
                                BigDecimal.ZERO,
                                null
                        ),
                        actor
                );

        StudentFeeAssignmentView assignment =
                assignmentService.assignStudentFee(
                        tenantId,
                        new AssignStudentFeeRequest(
                                "fin b3 assignment " + suffix,
                                studentId,
                                null,
                                activeStructure.id(),
                                effectiveFrom,
                                null,
                                null
                        ),
                        actorId,
                        actor
                );

        assertEquals(
                tenantId,
                assignment.tenantId()
        );

        assertEquals(
                account.id(),
                assignment.studentFinancialAccountId()
        );

        String invoiceNumber =
                "FINB3-INV-" + suffix;

        StudentInvoiceView invoice =
                invoiceService.createDraftInvoice(
                        tenantId,
                        new CreateDraftInvoiceRequest(
                                invoiceNumber,
                                assignment.id(),
                                effectiveFrom,
                                null
                        ),
                        actor
                );

        assertNotNull(
                invoice.id()
        );

        assertEquals(
                tenantId,
                invoice.tenantId()
        );

        assertEquals(
                invoiceNumber,
                invoice.invoiceNumber()
        );

        assertEquals(
                studentId,
                invoice.studentId()
        );

        assertEquals(
                account.id(),
                invoice.studentFinancialAccountId()
        );

        assertEquals(
                activeStructure.id(),
                invoice.feeStructureId()
        );

        assertEquals(
                "DRAFT",
                invoice.invoiceStatus()
        );

        assertEquals(
                "ACTIVE",
                invoice.status()
        );

        assertFalse(
                invoice.lines().isEmpty(),
                "Draft invoice must contain generated fee-structure lines."
        );

        BigDecimal lineNetTotal =
                invoice.lines()
                        .stream()
                        .map(
                                StudentInvoiceLineView::netAmount
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        assertEquals(
                0,
                invoice.subtotalAmount()
                        .compareTo(
                                lineNetTotal
                        )
        );

        assertEquals(
                0,
                invoice.totalAmount()
                        .compareTo(
                                lineNetTotal
                        )
        );

        assertEquals(
                0,
                invoice.outstandingAmount()
                        .compareTo(
                                invoice.totalAmount()
                        )
        );

        assertEquals(
                0,
                invoice.discountAmount()
                        .compareTo(
                                BigDecimal.ZERO
                        )
        );

        assertEquals(
                0,
                invoice.taxAmount()
                        .compareTo(
                                BigDecimal.ZERO
                        )
        );

        assertEquals(
                0,
                invoice.paidAmount()
                        .compareTo(
                                BigDecimal.ZERO
                        )
        );

        assertTrue(
                invoiceService.findStudentInvoice(
                        tenantId,
                        invoice.id()
                ).isPresent()
        );

        assertTrue(
                invoiceService.findStudentInvoice(
                        otherTenantId,
                        invoice.id()
                ).isEmpty(),
                "Cross-tenant invoice lookup must not expose the invoice."
        );

        assertTrue(
                invoiceService.listStudentInvoices(
                        tenantId,
                        studentId
                )
                        .stream()
                        .anyMatch(
                                candidate ->
                                        candidate.id()
                                                .equals(
                                                        invoice.id()
                                                )
                        )
        );

        Map<String, Object> persisted =
                jdbc.queryForMap(
                        """
                        SELECT
                            invoice_status,
                            status,
                            created_by,
                            updated_by,
                            version,
                            subtotal_amount,
                            total_amount,
                            paid_amount,
                            outstanding_amount
                        FROM gts_student_invoice
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        tenantId,
                        invoice.id()
                );

        assertEquals(
                "DRAFT",
                persisted.get(
                        "invoice_status"
                )
        );

        assertEquals(
                "ACTIVE",
                persisted.get(
                        "status"
                )
        );

        assertEquals(
                actor,
                persisted.get(
                        "created_by"
                )
        );

        assertEquals(
                actor,
                persisted.get(
                        "updated_by"
                )
        );

        assertEquals(
                0L,
                ((Number) persisted.get("version"))
                        .longValue()
        );

        Integer persistedLineCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_student_invoice_line
                        WHERE tenant_id = ?
                          AND invoice_id = ?
                        """,
                        Integer.class,
                        tenantId,
                        invoice.id()
                );

        assertEquals(
                invoice.lines().size(),
                persistedLineCount
        );

        Integer provenanceLineCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_student_invoice_line
                        WHERE tenant_id = ?
                          AND invoice_id = ?
                          AND fee_item_id IS NOT NULL
                          AND fee_structure_item_id IS NOT NULL
                          AND line_status = 'ACTIVE'
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId,
                        invoice.id()
                );

        assertEquals(
                invoice.lines().size(),
                provenanceLineCount
        );

        Map<String, Object> headerBeforeIssuance =
                jdbc.queryForMap(
                        """
                        SELECT
                            invoice_date,
                            due_date,
                            currency_code,
                            subtotal_amount,
                            discount_amount,
                            tax_amount,
                            total_amount,
                            paid_amount,
                            outstanding_amount,
                            invoice_status,
                            issued_at,
                            issued_by,
                            status,
                            updated_by,
                            version
                        FROM gts_student_invoice
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        tenantId,
                        invoice.id()
                );

        var lineStateBeforeIssuance =
                jdbc.queryForList(
                        """
                        SELECT
                            id,
                            fee_item_id,
                            fee_structure_item_id,
                            line_description,
                            quantity,
                            unit_amount,
                            gross_amount,
                            discount_amount,
                            tax_amount,
                            net_amount,
                            due_date,
                            line_status,
                            status,
                            updated_at,
                            updated_by,
                            version
                        FROM gts_student_invoice_line
                        WHERE tenant_id = ?
                          AND invoice_id = ?
                        ORDER BY id
                        """,
                        tenantId,
                        invoice.id()
                );

        assertEquals(
                "DRAFT",
                headerBeforeIssuance.get(
                        "invoice_status"
                )
        );

        assertEquals(
                0L,
                ((Number) headerBeforeIssuance.get(
                        "version"
                )).longValue()
        );

        assertEquals(
                null,
                headerBeforeIssuance.get(
                        "issued_at"
                )
        );

        assertEquals(
                null,
                headerBeforeIssuance.get(
                        "issued_by"
                )
        );

        IllegalArgumentException crossTenantIssue =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                invoiceService.issueStudentInvoice(
                                        otherTenantId,
                                        invoice.id(),
                                        actorId,
                                        actor
                                )
                );

        assertEquals(
                "Student invoice is not available in this tenant.",
                crossTenantIssue.getMessage()
        );

        Long versionAfterCrossTenantAttempt =
                jdbc.queryForObject(
                        """
                        SELECT version
                        FROM gts_student_invoice
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        Long.class,
                        tenantId,
                        invoice.id()
                );

        assertEquals(
                0L,
                versionAfterCrossTenantAttempt
        );

        StudentInvoiceView issued =
                invoiceService.issueStudentInvoice(
                        tenantId,
                        invoice.id(),
                        actorId,
                        actor
                );

        assertEquals(
                invoice.id(),
                issued.id()
        );

        assertEquals(
                tenantId,
                issued.tenantId()
        );

        assertEquals(
                "ISSUED",
                issued.invoiceStatus()
        );

        assertEquals(
                "ACTIVE",
                issued.status()
        );

        assertNotNull(
                issued.issuedAt()
        );

        assertEquals(
                actorId,
                issued.issuedBy()
        );

        assertEquals(
                0,
                issued.subtotalAmount()
                        .compareTo(
                                invoice.subtotalAmount()
                        )
        );

        assertEquals(
                0,
                issued.discountAmount()
                        .compareTo(
                                invoice.discountAmount()
                        )
        );

        assertEquals(
                0,
                issued.taxAmount()
                        .compareTo(
                                invoice.taxAmount()
                        )
        );

        assertEquals(
                0,
                issued.totalAmount()
                        .compareTo(
                                invoice.totalAmount()
                        )
        );

        assertEquals(
                0,
                issued.paidAmount()
                        .compareTo(
                                invoice.paidAmount()
                        )
        );

        assertEquals(
                0,
                issued.outstandingAmount()
                        .compareTo(
                                invoice.outstandingAmount()
                        )
        );

        Map<String, Object> headerAfterIssuance =
                jdbc.queryForMap(
                        """
                        SELECT
                            invoice_date,
                            due_date,
                            currency_code,
                            subtotal_amount,
                            discount_amount,
                            tax_amount,
                            total_amount,
                            paid_amount,
                            outstanding_amount,
                            invoice_status,
                            issued_at,
                            issued_by,
                            status,
                            updated_by,
                            version
                        FROM gts_student_invoice
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        tenantId,
                        invoice.id()
                );

        assertEquals(
                "ISSUED",
                headerAfterIssuance.get(
                        "invoice_status"
                )
        );

        assertEquals(
                "ACTIVE",
                headerAfterIssuance.get(
                        "status"
                )
        );

        assertNotNull(
                headerAfterIssuance.get(
                        "issued_at"
                )
        );

        assertEquals(
                actorId,
                headerAfterIssuance.get(
                        "issued_by"
                )
        );

        assertEquals(
                actor,
                headerAfterIssuance.get(
                        "updated_by"
                )
        );

        assertEquals(
                1L,
                ((Number) headerAfterIssuance.get(
                        "version"
                )).longValue()
        );

        for (String immutableHeaderField : new String[] {
                "invoice_date",
                "due_date",
                "currency_code",
                "subtotal_amount",
                "discount_amount",
                "tax_amount",
                "total_amount",
                "paid_amount",
                "outstanding_amount"
        }) {
            assertEquals(
                    headerBeforeIssuance.get(
                            immutableHeaderField
                    ),
                    headerAfterIssuance.get(
                            immutableHeaderField
                    ),
                    "Issuance must not mutate "
                            + immutableHeaderField
            );
        }

        var lineStateAfterIssuance =
                jdbc.queryForList(
                        """
                        SELECT
                            id,
                            fee_item_id,
                            fee_structure_item_id,
                            line_description,
                            quantity,
                            unit_amount,
                            gross_amount,
                            discount_amount,
                            tax_amount,
                            net_amount,
                            due_date,
                            line_status,
                            status,
                            updated_at,
                            updated_by,
                            version
                        FROM gts_student_invoice_line
                        WHERE tenant_id = ?
                          AND invoice_id = ?
                        ORDER BY id
                        """,
                        tenantId,
                        invoice.id()
                );

        assertEquals(
                lineStateBeforeIssuance,
                lineStateAfterIssuance,
                "Invoice issuance must not mutate invoice lines."
        );

        IllegalStateException repeatIssue =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                invoiceService.issueStudentInvoice(
                                        tenantId,
                                        invoice.id(),
                                        actorId,
                                        actor
                                )
                );

        assertEquals(
                "Only an active draft invoice may be issued.",
                repeatIssue.getMessage()
        );

        Map<String, Object> afterRepeatAttempt =
                jdbc.queryForMap(
                        """
                        SELECT
                            invoice_status,
                            issued_at,
                            issued_by,
                            status,
                            updated_by,
                            version
                        FROM gts_student_invoice
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        tenantId,
                        invoice.id()
                );

        assertEquals(
                "ISSUED",
                afterRepeatAttempt.get(
                        "invoice_status"
                )
        );

        assertNotNull(
                afterRepeatAttempt.get(
                        "issued_at"
                )
        );

        assertEquals(
                actorId,
                afterRepeatAttempt.get(
                        "issued_by"
                )
        );

        assertEquals(
                "ACTIVE",
                afterRepeatAttempt.get(
                        "status"
                )
        );

        assertEquals(
                actor,
                afterRepeatAttempt.get(
                        "updated_by"
                )
        );

        assertEquals(
                1L,
                ((Number) afterRepeatAttempt.get(
                        "version"
                )).longValue()
        );

        IllegalStateException duplicate =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                invoiceService.createDraftInvoice(
                                        tenantId,
                                        new CreateDraftInvoiceRequest(
                                                invoiceNumber,
                                                assignment.id(),
                                                effectiveFrom,
                                                null
                                        ),
                                        actor
                                )
                );

        assertEquals(
                "Invoice number already exists in this tenant.",
                duplicate.getMessage()
        );

        Integer persistedInvoiceCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_student_invoice
                        WHERE tenant_id = ?
                          AND invoice_number = ?
                        """,
                        Integer.class,
                        tenantId,
                        invoiceNumber
                );

        assertEquals(
                1,
                persistedInvoiceCount
        );
    }

    private RootFixture createRootFixture(
            String suffix
    ) {

        UUID organizationId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID studentId =
                UUID.randomUUID();

        String auditUser =
                "fin-b3-s1-postgresql-proof";

        jdbc.update(
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (
                    ?, ?, ?,
                    CURRENT_TIMESTAMP
                )
                """,
                organizationId,
                "ORG-" + suffix + "-" + shortId(
                        organizationId
                ),
                "FIN-B3 Proof Organisation " + suffix
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
                "TEN-" + suffix + "-" + shortId(
                        tenantId
                ),
                "FIN-B3 Proof Tenant " + suffix
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
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                schoolProfileId,
                tenantId,
                "SCH-" + suffix + "-" + shortId(
                        schoolProfileId
                ),
                "FIN-B3 Proof School " + suffix,
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
                    DATE '2026-01-01',
                    DATE '2026-12-31',
                    TRUE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                academicYearId,
                tenantId,
                "2026-" + suffix + "-" + shortId(
                        academicYearId
                ),
                "Academic Year 2026 " + suffix,
                auditUser,
                auditUser
        );

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
                    'Test',
                    ?,
                    DATE '2015-01-01',
                    'ACTIVE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP, ?,
                    CURRENT_TIMESTAMP, ?,
                    0
                )
                """,
                studentId,
                tenantId,
                "STU-" + suffix + "-" + shortId(
                        studentId
                ),
                "PLN-" + studentId,
                "Learner-" + suffix,
                auditUser,
                auditUser
        );

        return new RootFixture(
                tenantId,
                academicYearId,
                studentId
        );
    }

    private UUID createOtherTenant(
            String suffix
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
                VALUES (
                    ?, ?, ?,
                    CURRENT_TIMESTAMP
                )
                """,
                organizationId,
                "ORG-" + suffix + "-" + shortId(
                        organizationId
                ),
                "FIN-B3 Other Organisation " + suffix
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
                "TEN-" + suffix + "-" + shortId(
                        tenantId
                ),
                "FIN-B3 Other Tenant " + suffix
        );

        return tenantId;
    }

    private static String shortId(
            UUID value
    ) {

        return value
                .toString()
                .substring(
                        0,
                        8
                );
    }

    private record RootFixture(
            UUID tenantId,
            UUID academicYearId,
            UUID studentId
    ) {
    }
}
