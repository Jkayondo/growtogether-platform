package africa.growtogether.platform.school.finance.assignment;

import static africa.growtogether.platform.school.finance.assignment.FinanceFeeAssignmentDtos.*;
import static africa.growtogether.platform.school.finance.foundation.FinanceFoundationDtos.*;

import africa.growtogether.platform.school.finance.foundation.FinanceFoundationJdbcRepository;
import africa.growtogether.platform.school.finance.foundation.FinanceFoundationService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class FinanceFeeAssignmentService {

    private final FinanceFeeAssignmentJdbcRepository repository;
    private final FinanceFoundationService foundationService;
    private final FinanceFoundationJdbcRepository foundationRepository;


    public FinanceFeeAssignmentService(
            FinanceFeeAssignmentJdbcRepository repository,
            FinanceFoundationService foundationService,
            FinanceFoundationJdbcRepository foundationRepository
    ) {
        this.repository = repository;
        this.foundationService = foundationService;
        this.foundationRepository = foundationRepository;
    }


    @Transactional
    public StudentFeeAssignmentView assignStudentFee(
            UUID tenantId,
            AssignStudentFeeRequest request,
            UUID assignedBy,
            String actor
    ) {

        requireTenant(
                tenantId
        );

        requireRequest(
                request
        );

        requireActor(
                actor
        );


        if (assignedBy == null) {
            throw new IllegalArgumentException(
                    "assignedBy must not be null"
            );
        }


        if (request.studentId() == null) {
            throw new IllegalArgumentException(
                    "studentId must not be null"
            );
        }


        if (request.feeStructureId() == null) {
            throw new IllegalArgumentException(
                    "feeStructureId must not be null"
            );
        }


        if (request.effectiveFrom() == null) {
            throw new IllegalArgumentException(
                    "effectiveFrom must not be null"
            );
        }


        if (
                request.effectiveTo() != null
                        && request.effectiveTo()
                        .isBefore(
                                request.effectiveFrom()
                        )
        ) {
            throw new IllegalArgumentException(
                    "effectiveTo must not be before effectiveFrom."
            );
        }


        String assignmentReference =
                normalizeCode(
                        request.assignmentReference(),
                        "assignmentReference"
                );


        if (
                !foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        request.studentId()
                )
        ) {
            throw new IllegalArgumentException(
                    "studentId is not available in this tenant."
            );
        }


        if (
                request.studentEnrollmentId() != null
                        && !foundationRepository
                        .enrollmentBelongsToStudent(
                                tenantId,
                                request.studentEnrollmentId(),
                                request.studentId()
                        )
        ) {
            throw new IllegalArgumentException(
                    "Student enrollment is not available for this student in this tenant."
            );
        }


        FeeStructureView structure =
                foundationService
                        .listFeeStructures(
                                tenantId
                        )
                        .stream()
                        .filter(
                                candidate ->
                                        request.feeStructureId()
                                                .equals(
                                                        candidate.id()
                                                )
                        )
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Fee structure is not available in this tenant."
                                        )
                        );


        if (
                !"ACTIVE".equals(
                        structure.structureStatus()
                )
                        || !"ACTIVE".equals(
                        structure.status()
                )
        ) {
            throw new IllegalStateException(
                    "Only an active fee structure may be assigned."
            );
        }


        validateStructureDates(
                structure,
                request.effectiveFrom(),
                request.effectiveTo()
        );


        StudentFinancialAccountView account =
                foundationService
                        .findStudentAccount(
                                tenantId,
                                request.studentId(),
                                structure.currencyCode()
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "The student requires an active financial account in the fee structure currency before assignment."
                                        )
                        );


        if (
                !"ACTIVE".equals(
                        account.billingStatus()
                )
                        || !"ACTIVE".equals(
                        account.status()
                )
        ) {
            throw new IllegalStateException(
                    "The student financial account is not active."
            );
        }


        if (
                repository.existsAssignmentReference(
                        tenantId,
                        assignmentReference
                )
        ) {
            throw new IllegalStateException(
                    "Fee assignment reference already exists."
            );
        }


        if (
                repository.existsAssignmentScope(
                        tenantId,
                        request.studentId(),
                        request.feeStructureId(),
                        request.effectiveFrom()
                )
        ) {
            throw new IllegalStateException(
                    "This fee structure is already assigned to the student for the same effective date."
            );
        }


        AssignStudentFeeRequest normalized =
                new AssignStudentFeeRequest(
                        assignmentReference,
                        request.studentId(),
                        request.studentEnrollmentId(),
                        request.feeStructureId(),
                        request.effectiveFrom(),
                        request.effectiveTo(),
                        request.workflowInstanceId()
                );


        return repository.createStudentFeeAssignment(
                tenantId,
                normalized,
                account.id(),
                assignedBy,
                actor
        );
    }


    @Transactional(readOnly = true)
    public Optional<StudentFeeAssignmentView> findStudentFeeAssignment(
            UUID tenantId,
            UUID assignmentId
    ) {

        requireTenant(
                tenantId
        );


        if (assignmentId == null) {
            throw new IllegalArgumentException(
                    "assignmentId must not be null"
            );
        }


        return repository.findStudentFeeAssignment(
                tenantId,
                assignmentId
        );
    }


    @Transactional(readOnly = true)
    public List<StudentFeeAssignmentView> listStudentFeeAssignments(
            UUID tenantId,
            UUID studentId
    ) {

        requireTenant(
                tenantId
        );


        if (studentId == null) {
            throw new IllegalArgumentException(
                    "studentId must not be null"
            );
        }


        if (
                !foundationRepository.existsTenantReference(
                        "gts_student",
                        tenantId,
                        studentId
                )
        ) {
            throw new IllegalArgumentException(
                    "studentId is not available in this tenant."
            );
        }


        return repository.listStudentFeeAssignments(
                tenantId,
                studentId
        );
    }


    private void validateStructureDates(
            FeeStructureView structure,
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {

        if (
                structure.effectiveFrom() != null
                        && effectiveFrom.isBefore(
                        structure.effectiveFrom()
                )
        ) {
            throw new IllegalArgumentException(
                    "Fee assignment cannot begin before the fee structure effective date."
            );
        }


        if (
                structure.effectiveTo() != null
                        && effectiveFrom.isAfter(
                        structure.effectiveTo()
                )
        ) {
            throw new IllegalArgumentException(
                    "Fee assignment cannot begin after the fee structure expiry date."
            );
        }


        if (
                effectiveTo != null
                        && structure.effectiveTo() != null
                        && effectiveTo.isAfter(
                        structure.effectiveTo()
                )
        ) {
            throw new IllegalArgumentException(
                    "Fee assignment cannot extend beyond the fee structure expiry date."
            );
        }
    }


    private void requireTenant(
            UUID tenantId
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }
    }


    private void requireActor(
            String actor
    ) {

        if (
                actor == null
                        || actor.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "actor must not be blank"
            );
        }
    }


    private void requireRequest(
            Object request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "request must not be null"
            );
        }
    }


    private String normalizeCode(
            String value,
            String field
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            throw new IllegalArgumentException(
                    field + " must not be blank"
            );
        }


        String normalized =
                value.trim()
                        .toUpperCase(
                                Locale.ROOT
                        )
                        .replaceAll(
                                "[^A-Z0-9]+",
                                "_"
                        )
                        .replaceAll(
                                "^_+|_+$",
                                ""
                        );


        if (normalized.isBlank()) {
            throw new IllegalArgumentException(
                    field + " must contain letters or numbers"
            );
        }


        if (normalized.length() > 100) {
            throw new IllegalArgumentException(
                    field + " must not exceed 100 characters"
            );
        }


        return normalized;
    }

public StudentFeeAssignmentView suspendStudentFeeAssignment(
            UUID tenantId,
            UUID assignmentId,
            String actor
    ) {

        requireLifecycleInput(
                tenantId,
                assignmentId,
                actor
        );

        return repository.suspendStudentFeeAssignment(
                tenantId,
                assignmentId,
                actor
        ).orElseThrow(
                () ->
                        new IllegalStateException(
                                "Only an active fee assignment may be suspended."
                        )
        );
    }

    public StudentFeeAssignmentView activateStudentFeeAssignment(
            UUID tenantId,
            UUID assignmentId,
            String actor
    ) {

        requireLifecycleInput(
                tenantId,
                assignmentId,
                actor
        );

        return repository.activateStudentFeeAssignment(
                tenantId,
                assignmentId,
                actor
        ).orElseThrow(
                () ->
                        new IllegalStateException(
                                "Only a pending or suspended fee assignment may be activated."
                        )
        );
    }

    public StudentFeeAssignmentView completeStudentFeeAssignment(
            UUID tenantId,
            UUID assignmentId,
            String actor
    ) {

        requireLifecycleInput(
                tenantId,
                assignmentId,
                actor
        );

        return repository.completeStudentFeeAssignment(
                tenantId,
                assignmentId,
                actor
        ).orElseThrow(
                () ->
                        new IllegalStateException(
                                "Only an active or suspended fee assignment may be completed."
                        )
        );
    }

    public StudentFeeAssignmentView cancelStudentFeeAssignment(
            UUID tenantId,
            UUID assignmentId,
            String actor
    ) {

        requireLifecycleInput(
                tenantId,
                assignmentId,
                actor
        );

        return repository.cancelStudentFeeAssignment(
                tenantId,
                assignmentId,
                actor
        ).orElseThrow(
                () ->
                        new IllegalStateException(
                                "Only a pending, active or suspended fee assignment may be cancelled."
                        )
        );
    }

    public StudentFeeAssignmentView archiveStudentFeeAssignment(
            UUID tenantId,
            UUID assignmentId,
            String actor
    ) {

        requireLifecycleInput(
                tenantId,
                assignmentId,
                actor
        );

        return repository.archiveStudentFeeAssignment(
                tenantId,
                assignmentId,
                actor
        ).orElseThrow(
                () ->
                        new IllegalStateException(
                                "Only a completed or cancelled fee assignment may be archived."
                        )
        );
    }

    private void requireLifecycleInput(
            UUID tenantId,
            UUID assignmentId,
            String actor
    ) {

        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );
        }

        if (assignmentId == null) {
            throw new IllegalArgumentException(
                    "assignmentId must not be null"
            );
        }

        if (
                actor == null
                        || actor.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "actor must not be blank"
            );
        }
    }
}
