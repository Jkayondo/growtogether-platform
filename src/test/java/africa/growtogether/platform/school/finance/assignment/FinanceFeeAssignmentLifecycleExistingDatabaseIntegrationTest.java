package africa.growtogether.platform.school.finance.assignment;

import static africa.growtogether.platform.school.finance.assignment.FinanceFeeAssignmentDtos.*;
import static africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.*;
import static org.junit.jupiter.api.Assertions.*;

import africa.growtogether.platform.school.finance.foundation.FinanceFoundationService;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;


@SpringBootTest
class FinanceFeeAssignmentLifecycleExistingDatabaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;


    @Autowired
    private FinanceFoundationService foundationService;


    @Autowired
    private FinanceFeeAssignmentService assignmentService;


    @Test
    void learnerFeeAssignmentLifecycleIsTenantSafeAndAudited() {

        Map<String, Object> fixture =
                jdbc.queryForMap(
                        """
                        SELECT
                            sp.tenant_id,
                            ay.id AS academic_year_id,
                            s.id AS student_id
                        FROM gts_school_profile sp
                        JOIN gts_academic_year ay
                          ON ay.tenant_id = sp.tenant_id
                        JOIN gts_student s
                          ON s.tenant_id = sp.tenant_id
                        ORDER BY
                            sp.tenant_id,
                            ay.start_date DESC,
                            s.id
                        LIMIT 1
                        """
                );


        UUID tenantId =
                (UUID) fixture.get(
                        "tenant_id"
                );

        UUID academicYearId =
                (UUID) fixture.get(
                        "academic_year_id"
                );

        UUID studentId =
                (UUID) fixture.get(
                        "student_id"
                );


        UUID otherTenantId =
                jdbc.queryForObject(
                        """
                        SELECT id
                        FROM eiam_tenant
                        WHERE id <> ?
                        ORDER BY id
                        LIMIT 1
                        """,
                        UUID.class,
                        tenantId
                );


        assertNotNull(
                otherTenantId
        );


        String suffix =
                UUID.randomUUID()
                        .toString()
                        .replace(
                                "-",
                                ""
                        )
                        .substring(
                                0,
                                8
                        )
                        .toUpperCase();


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
                                "FINB2_CAT_" + suffix,
                                "FIN-B2 Tuition",
                                "FIN-B2 assignment persistence proof",
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
                                "FINB2_ITEM_" + suffix,
                                "FIN-B2 Term Tuition",
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
                                "FINB2_FS_" + suffix,
                                "FIN-B2 Fee Structure",
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


        FeeStructureView active =
                foundationService.activateFeeStructure(
                        tenantId,
                        structure.id(),
                        actor
                );


        assertEquals(
                "ACTIVE",
                active.structureStatus()
        );


        StudentFinancialAccountView account =
                foundationService.openStudentAccount(
                        tenantId,
                        new OpenStudentFinancialAccountRequest(
                                "FINB2_ACC_" + suffix,
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
                                " fin b2 assignment "
                                        + suffix,
                                studentId,
                                null,
                                structure.id(),
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
                "FIN_B2_ASSIGNMENT_"
                        + suffix,
                assignment.assignmentReference()
        );

        assertEquals(
                account.id(),
                assignment.studentFinancialAccountId()
        );

        assertEquals(
                studentId,
                assignment.studentId()
        );

        assertEquals(
                structure.id(),
                assignment.feeStructureId()
        );

        assertEquals(
                actorId,
                assignment.assignedBy()
        );

        assertEquals(
                "ACTIVE",
                assignment.assignmentStatus()
        );

        assertEquals(
                "ACTIVE",
                assignment.status()
        );


        assertEquals(
                assignment.id(),
                assignmentService
                        .findStudentFeeAssignment(
                                tenantId,
                                assignment.id()
                        )
                        .orElseThrow()
                        .id()
        );


        assertTrue(
                assignmentService
                        .findStudentFeeAssignment(
                                otherTenantId,
                                assignment.id()
                        )
                        .isEmpty()
        );


        assertEquals(
                1,
                assignmentService
                        .listStudentFeeAssignments(
                                tenantId,
                                studentId
                        )
                        .stream()
                        .filter(
                                value ->
                                        value.id().equals(
                                                assignment.id()
                                        )
                        )
                        .count()
        );


        Integer persisted =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_student_fee_assignment
                        WHERE tenant_id = ?
                          AND id = ?
                          AND student_financial_account_id = ?
                          AND student_id = ?
                          AND fee_structure_id = ?
                          AND assignment_status = 'ACTIVE'
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId,
                        assignment.id(),
                        account.id(),
                        studentId,
                        structure.id()
                );


        assertEquals(
                1,
                persisted
        );


        assertThrows(
                IllegalStateException.class,
                () ->
                        assignmentService.assignStudentFee(
                                tenantId,
                                new AssignStudentFeeRequest(
                                        "FINB2_DIFFERENT_"
                                                + suffix,
                                        studentId,
                                        null,
                                        structure.id(),
                                        effectiveFrom,
                                        null,
                                        null
                                ),
                                UUID.randomUUID(),
                                UUID.randomUUID()
                                        .toString()
                        )
        );


        Integer scopeCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_student_fee_assignment
                        WHERE tenant_id = ?
                          AND student_id = ?
                          AND fee_structure_id = ?
                          AND effective_from = ?
                        """,
                        Integer.class,
                        tenantId,
                        studentId,
                        structure.id(),
                        effectiveFrom
                );


        assertEquals(
                1,
                scopeCount
        );


        /*
         * FIN-B2-S2 lifecycle proof:
         *
         * ACTIVE -> SUSPENDED
         * SUSPENDED -> ACTIVE
         * ACTIVE -> CANCELLED
         * CANCELLED -> ARCHIVED
         *
         * A second assignment proves:
         *
         * ACTIVE -> COMPLETED
         *
         * Tenant-qualified transition rejection,
         * audit actor preservation and monotonic
         * version increments are verified directly
         * against PostgreSQL.
         */

        assertThrows(
                IllegalStateException.class,
                () ->
                        assignmentService.suspendStudentFeeAssignment(
                                otherTenantId,
                                assignment.id(),
                                actor
                        )
        );

        StudentFeeAssignmentView suspended =
                assignmentService.suspendStudentFeeAssignment(
                        tenantId,
                        assignment.id(),
                        actor
                );

        assertEquals(
                "SUSPENDED",
                suspended.assignmentStatus()
        );

        assertEquals(
                "ACTIVE",
                suspended.status()
        );

        StudentFeeAssignmentView reactivated =
                assignmentService.activateStudentFeeAssignment(
                        tenantId,
                        assignment.id(),
                        actor
                );

        assertEquals(
                "ACTIVE",
                reactivated.assignmentStatus()
        );

        assertEquals(
                "ACTIVE",
                reactivated.status()
        );

        StudentFeeAssignmentView cancelled =
                assignmentService.cancelStudentFeeAssignment(
                        tenantId,
                        assignment.id(),
                        actor
                );

        assertEquals(
                "CANCELLED",
                cancelled.assignmentStatus()
        );

        assertEquals(
                "INACTIVE",
                cancelled.status()
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        assignmentService.suspendStudentFeeAssignment(
                                tenantId,
                                assignment.id(),
                                actor
                        )
        );

        StudentFeeAssignmentView archived =
                assignmentService.archiveStudentFeeAssignment(
                        tenantId,
                        assignment.id(),
                        actor
                );

        assertEquals(
                "ARCHIVED",
                archived.assignmentStatus()
        );

        assertEquals(
                "ARCHIVED",
                archived.status()
        );

        java.util.Map<String, Object> archivedAudit =
                jdbc.queryForMap(
                        """
                        SELECT
                            assignment_status,
                            status,
                            version,
                            updated_by
                        FROM gts_student_fee_assignment
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        tenantId,
                        assignment.id()
                );

        assertEquals(
                "ARCHIVED",
                archivedAudit.get(
                        "assignment_status"
                )
        );

        assertEquals(
                "ARCHIVED",
                archivedAudit.get(
                        "status"
                )
        );

        assertEquals(
                4L,
                ((Number) archivedAudit.get(
                        "version"
                )).longValue()
        );

        assertEquals(
                actor,
                archivedAudit.get(
                        "updated_by"
                )
        );

        LocalDate completionEffectiveFrom =
                effectiveFrom.plusDays(
                        1
                );

        StudentFeeAssignmentView completionAssignment =
                assignmentService.assignStudentFee(
                        tenantId,
                        new AssignStudentFeeRequest(
                                "FIN_B2_COMPLETE_"
                                        + suffix,
                                studentId,
                                null,
                                structure.id(),
                                completionEffectiveFrom,
                                null,
                                null
                        ),
                        actorId,
                        actor
                );

        StudentFeeAssignmentView completed =
                assignmentService.completeStudentFeeAssignment(
                        tenantId,
                        completionAssignment.id(),
                        actor
                );

        assertEquals(
                "COMPLETED",
                completed.assignmentStatus()
        );

        assertEquals(
                "INACTIVE",
                completed.status()
        );

        java.util.Map<String, Object> completedAudit =
                jdbc.queryForMap(
                        """
                        SELECT
                            assignment_status,
                            status,
                            version,
                            updated_by
                        FROM gts_student_fee_assignment
                        WHERE tenant_id = ?
                          AND id = ?
                        """,
                        tenantId,
                        completionAssignment.id()
                );

        assertEquals(
                "COMPLETED",
                completedAudit.get(
                        "assignment_status"
                )
        );

        assertEquals(
                "INACTIVE",
                completedAudit.get(
                        "status"
                )
        );

        assertEquals(
                1L,
                ((Number) completedAudit.get(
                        "version"
                )).longValue()
        );

        assertEquals(
                actor,
                completedAudit.get(
                        "updated_by"
                )
        );

        assertThrows(
                IllegalStateException.class,
                () ->
                        assignmentService.cancelStudentFeeAssignment(
                                tenantId,
                                completionAssignment.id(),
                                actor
                        )
        );
}
}
