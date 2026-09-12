package africa.growtogether.platform.school.finance.assignment;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class FinanceFeeAssignmentDtos {

    private FinanceFeeAssignmentDtos() {
    }


    public record AssignStudentFeeRequest(
            String assignmentReference,
            UUID studentId,
            UUID studentEnrollmentId,
            UUID feeStructureId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            UUID workflowInstanceId
    ) {
    }


    public record StudentFeeAssignmentView(
            UUID id,
            UUID tenantId,
            String assignmentReference,
            UUID studentFinancialAccountId,
            UUID studentId,
            UUID studentEnrollmentId,
            UUID feeStructureId,
            Instant assignedAt,
            UUID assignedBy,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            UUID workflowInstanceId,
            String assignmentStatus,
            String status
    ) {
    }
}
