package africa.growtogether.platform.school.finance.assignment;


import africa.growtogether.platform.school.finance.DisposableExistingDatabaseTestSupport;
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
class FinanceFeeAssignmentExistingDatabaseIntegrationTest extends DisposableExistingDatabaseTestSupport {

    @Autowired
    private JdbcTemplate jdbc;


    @Autowired
    private FinanceFoundationService foundationService;


    @Autowired
    private FinanceFeeAssignmentService assignmentService;


    @Test
    void learnerFeeAssignmentPersistsAndRemainsTenantScoped() {

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
    }
}
