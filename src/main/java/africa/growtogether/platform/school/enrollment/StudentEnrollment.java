package africa.growtogether.platform.school.enrollment;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "gts_student_enrollment")
public class StudentEnrollment extends AuditedTenantEntity {

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    @Column(name = "academic_term_id")
    private UUID academicTermId;

    @Column(name = "campus_id", nullable = false)
    private UUID campusId;

    @Column(name = "class_grade_id", nullable = false)
    private UUID classGradeId;

    @Column(name = "stream_id")
    private UUID streamId;

    @Column(name = "enrollment_number", nullable = false, length = 100)
    private String enrollmentNumber;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "enrollment_type", nullable = false, length = 30)
    private String enrollmentType = "NEW";

    @Column(name = "enrollment_status", nullable = false, length = 30)
    private String enrollmentStatus = "ACTIVE";

    @Column(name = "previous_enrollment_id")
    private UUID previousEnrollmentId;

    @Column(name = "workflow_instance_id")
    private UUID workflowInstanceId;

    @Column(name = "enrolled_by")
    private UUID enrolledBy;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "exit_date")
    private LocalDate exitDate;

    @Column(name = "exit_reason", length = 1000)
    private String exitReason;

    protected StudentEnrollment() {
    }

    public StudentEnrollment(
            UUID studentId,
            UUID academicYearId,
            UUID academicTermId,
            UUID campusId,
            UUID classGradeId,
            UUID streamId,
            String enrollmentNumber,
            LocalDate enrollmentDate,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String enrollmentType,
            UUID previousEnrollmentId,
            UUID workflowInstanceId,
            UUID enrolledBy
    ) {

        this.studentId =
                requireNonNull(
                        studentId,
                        "studentId"
                );

        this.academicYearId =
                requireNonNull(
                        academicYearId,
                        "academicYearId"
                );

        this.academicTermId =
                academicTermId;

        this.campusId =
                requireNonNull(
                        campusId,
                        "campusId"
                );

        this.classGradeId =
                requireNonNull(
                        classGradeId,
                        "classGradeId"
                );

        this.streamId =
                streamId;

        this.enrollmentNumber =
                requireText(
                        enrollmentNumber,
                        "enrollmentNumber"
                );

        this.enrollmentDate =
                requireNonNull(
                        enrollmentDate,
                        "enrollmentDate"
                );

        this.effectiveFrom =
                requireNonNull(
                        effectiveFrom,
                        "effectiveFrom"
                );

        if (
                effectiveTo != null
                && effectiveTo.isBefore(effectiveFrom)
        ) {
            throw new IllegalArgumentException(
                    "effectiveTo must not be before effectiveFrom"
            );
        }

        this.effectiveTo =
                effectiveTo;

        this.enrollmentType =
                enrollmentType == null
                        || enrollmentType.isBlank()
                        ? "NEW"
                        : enrollmentType.trim();

        this.previousEnrollmentId =
                previousEnrollmentId;

        this.workflowInstanceId =
                workflowInstanceId;

        this.enrolledBy =
                enrolledBy;
    }

    public void markPending() {
        this.enrollmentStatus = "PENDING";
    }

    public void activate(
            UUID approvedBy
    ) {

        this.enrollmentStatus = "ACTIVE";
        this.approvedBy = approvedBy;
        this.approvedAt = Instant.now();
    }

    public void suspend() {
        this.enrollmentStatus = "SUSPENDED";
    }

    public void complete(
            LocalDate effectiveTo
    ) {

        if (
                effectiveTo != null
                && effectiveTo.isBefore(this.effectiveFrom)
        ) {
            throw new IllegalArgumentException(
                    "effectiveTo must not be before effectiveFrom"
            );
        }

        this.effectiveTo = effectiveTo;
        this.enrollmentStatus = "COMPLETED";
    }

    public void withdraw(
            LocalDate exitDate,
            String exitReason
    ) {

        validateExitDate(
                exitDate
        );

        this.exitDate = exitDate;
        this.exitReason = exitReason;
        this.enrollmentStatus = "WITHDRAWN";
    }

    public void transfer(
            LocalDate exitDate,
            String exitReason
    ) {

        validateExitDate(
                exitDate
        );

        this.exitDate = exitDate;
        this.exitReason = exitReason;
        this.enrollmentStatus = "TRANSFERRED";
    }

    public void cancel() {
        this.enrollmentStatus = "CANCELLED";
    }

    private void validateExitDate(
            LocalDate exitDate
    ) {

        if (
                exitDate != null
                && exitDate.isBefore(this.enrollmentDate)
        ) {
            throw new IllegalArgumentException(
                    "exitDate must not be before enrollmentDate"
            );
        }
    }

    private String requireText(
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

        return value.trim();
    }

    private <T> T requireNonNull(
            T value,
            String field
    ) {

        if (value == null) {
            throw new IllegalArgumentException(
                    field + " must not be null"
            );
        }

        return value;
    }

    public UUID getStudentId() {
        return studentId;
    }

    public UUID getAcademicYearId() {
        return academicYearId;
    }

    public UUID getAcademicTermId() {
        return academicTermId;
    }

    public UUID getCampusId() {
        return campusId;
    }

    public UUID getClassGradeId() {
        return classGradeId;
    }

    public UUID getStreamId() {
        return streamId;
    }

    public String getEnrollmentNumber() {
        return enrollmentNumber;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public String getEnrollmentType() {
        return enrollmentType;
    }

    public String getEnrollmentStatus() {
        return enrollmentStatus;
    }

    public UUID getPreviousEnrollmentId() {
        return previousEnrollmentId;
    }

    public UUID getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public UUID getEnrolledBy() {
        return enrolledBy;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public LocalDate getExitDate() {
        return exitDate;
    }

    public String getExitReason() {
        return exitReason;
    }
}
