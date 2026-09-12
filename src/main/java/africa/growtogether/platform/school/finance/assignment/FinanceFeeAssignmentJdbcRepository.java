package africa.growtogether.platform.school.finance.assignment;

import static africa.growtogether.platform.school.finance.assignment.FinanceFeeAssignmentDtos.*;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FinanceFeeAssignmentJdbcRepository {

    private final JdbcTemplate jdbc;


    public FinanceFeeAssignmentJdbcRepository(
            JdbcTemplate jdbc
    ) {
        this.jdbc = jdbc;
    }


    @Transactional
    public StudentFeeAssignmentView createStudentFeeAssignment(
            UUID tenantId,
            AssignStudentFeeRequest request,
            UUID studentFinancialAccountId,
            UUID assignedBy,
            String actor
    ) {

        UUID id =
                UUID.randomUUID();


        jdbc.update(
                """
                INSERT INTO gts_student_fee_assignment (
                    id,
                    tenant_id,
                    assignment_reference,
                    student_financial_account_id,
                    student_id,
                    student_enrollment_id,
                    fee_structure_id,
                    assigned_at,
                    assigned_by,
                    effective_from,
                    effective_to,
                    workflow_instance_id,
                    assignment_status,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?,
                    CURRENT_TIMESTAMP,
                    ?, ?, ?, ?,
                    'ACTIVE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                id,
                tenantId,
                request.assignmentReference(),
                studentFinancialAccountId,
                request.studentId(),
                request.studentEnrollmentId(),
                request.feeStructureId(),
                assignedBy,
                request.effectiveFrom(),
                request.effectiveTo(),
                request.workflowInstanceId(),
                actor,
                actor
        );


        return findStudentFeeAssignment(
                tenantId,
                id
        ).orElseThrow();
    }


    @Transactional(readOnly = true)
    public Optional<StudentFeeAssignmentView> findStudentFeeAssignment(
            UUID tenantId,
            UUID id
    ) {

        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    assignment_reference,
                    student_financial_account_id,
                    student_id,
                    student_enrollment_id,
                    fee_structure_id,
                    assigned_at,
                    assigned_by,
                    effective_from,
                    effective_to,
                    workflow_instance_id,
                    assignment_status,
                    status
                FROM gts_student_fee_assignment
                WHERE tenant_id = ?
                  AND id = ?
                """,
                this::mapAssignment,
                tenantId,
                id
        ).stream().findFirst();
    }


    @Transactional(readOnly = true)
    public List<StudentFeeAssignmentView> listStudentFeeAssignments(
            UUID tenantId,
            UUID studentId
    ) {

        return jdbc.query(
                """
                SELECT
                    id,
                    tenant_id,
                    assignment_reference,
                    student_financial_account_id,
                    student_id,
                    student_enrollment_id,
                    fee_structure_id,
                    assigned_at,
                    assigned_by,
                    effective_from,
                    effective_to,
                    workflow_instance_id,
                    assignment_status,
                    status
                FROM gts_student_fee_assignment
                WHERE tenant_id = ?
                  AND student_id = ?
                ORDER BY effective_from DESC,
                         assigned_at DESC,
                         id
                """,
                this::mapAssignment,
                tenantId,
                studentId
        );
    }


    @Transactional(readOnly = true)
    public boolean existsAssignmentReference(
            UUID tenantId,
            String assignmentReference
    ) {

        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        """
                        SELECT EXISTS (
                            SELECT 1
                            FROM gts_student_fee_assignment
                            WHERE tenant_id = ?
                              AND assignment_reference = ?
                        )
                        """,
                        Boolean.class,
                        tenantId,
                        assignmentReference
                )
        );
    }


    @Transactional(readOnly = true)
    public boolean existsAssignmentScope(
            UUID tenantId,
            UUID studentId,
            UUID feeStructureId,
            LocalDate effectiveFrom
    ) {

        return Boolean.TRUE.equals(
                jdbc.queryForObject(
                        """
                        SELECT EXISTS (
                            SELECT 1
                            FROM gts_student_fee_assignment
                            WHERE tenant_id = ?
                              AND student_id = ?
                              AND fee_structure_id = ?
                              AND effective_from = ?
                        )
                        """,
                        Boolean.class,
                        tenantId,
                        studentId,
                        feeStructureId,
                        effectiveFrom
                )
        );
    }


    private StudentFeeAssignmentView mapAssignment(
            ResultSet rs,
            int rowNum
    ) throws SQLException {

        java.sql.Timestamp assignedAt =
                rs.getTimestamp(
                        "assigned_at"
                );


        return new StudentFeeAssignmentView(
                rs.getObject(
                        "id",
                        UUID.class
                ),
                rs.getObject(
                        "tenant_id",
                        UUID.class
                ),
                rs.getString(
                        "assignment_reference"
                ),
                rs.getObject(
                        "student_financial_account_id",
                        UUID.class
                ),
                rs.getObject(
                        "student_id",
                        UUID.class
                ),
                rs.getObject(
                        "student_enrollment_id",
                        UUID.class
                ),
                rs.getObject(
                        "fee_structure_id",
                        UUID.class
                ),
                assignedAt == null
                        ? null
                        : assignedAt.toInstant(),
                rs.getObject(
                        "assigned_by",
                        UUID.class
                ),
                rs.getObject(
                        "effective_from",
                        LocalDate.class
                ),
                rs.getObject(
                        "effective_to",
                        LocalDate.class
                ),
                rs.getObject(
                        "workflow_instance_id",
                        UUID.class
                ),
                rs.getString(
                        "assignment_status"
                ),
                rs.getString(
                        "status"
                )
        );
    }
}
