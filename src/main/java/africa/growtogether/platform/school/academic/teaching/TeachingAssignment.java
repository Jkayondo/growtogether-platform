package africa.growtogether.platform.school.academic.teaching;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "gts_teaching_assignment")
public class TeachingAssignment extends AuditedTenantEntity {

    @Column(
            name = "assignment_reference",
            nullable = false,
            length = 100
    )
    private String assignmentReference;

    @Column(
            name = "teacher_profile_id",
            nullable = false
    )
    private UUID teacherProfileId;

    @Column(name = "ewf_assignment_id")
    private UUID ewfAssignmentId;

    @Column(
            name = "academic_year_id",
            nullable = false
    )
    private UUID academicYearId;

    @Column(name = "academic_term_id")
    private UUID academicTermId;

    @Column(
            name = "campus_id",
            nullable = false
    )
    private UUID campusId;

    @Column(
            name = "class_grade_id",
            nullable = false
    )
    private UUID classGradeId;

    @Column(name = "stream_id")
    private UUID streamId;

    @Column(
            name = "subject_id",
            nullable = false
    )
    private UUID subjectId;

    @Column(
            name = "assignment_type",
            nullable = false,
            length = 30
    )
    private String assignmentType = "PRIMARY_TEACHER";

    @Column(
            name = "weekly_periods",
            nullable = false
    )
    private int weeklyPeriods;

    @Column(
            name = "workload_percentage",
            precision = 5,
            scale = 2
    )
    private BigDecimal workloadPercentage;

    @Column(
            name = "effective_from",
            nullable = false
    )
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(
            name = "room_reference",
            length = 120
    )
    private String roomReference;

    @Column(name = "timetable_reference")
    private UUID timetableReference;

    @Column(name = "workflow_instance_id")
    private UUID workflowInstanceId;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approved_by")
    private UUID approvedBy;

    @Column(
            name = "assignment_status",
            nullable = false,
            length = 30
    )
    private String assignmentStatus = "PLANNED";

    protected TeachingAssignment() {
    }

    public TeachingAssignment(
            String assignmentReference,
            UUID teacherProfileId,
            UUID ewfAssignmentId,
            UUID academicYearId,
            UUID academicTermId,
            UUID campusId,
            UUID classGradeId,
            UUID streamId,
            UUID subjectId,
            String assignmentType,
            int weeklyPeriods,
            BigDecimal workloadPercentage,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            String roomReference,
            UUID timetableReference,
            UUID workflowInstanceId
    ) {
        this.assignmentReference = assignmentReference;
        this.teacherProfileId = teacherProfileId;
        this.ewfAssignmentId = ewfAssignmentId;
        this.academicYearId = academicYearId;
        this.academicTermId = academicTermId;
        this.campusId = campusId;
        this.classGradeId = classGradeId;
        this.streamId = streamId;
        this.subjectId = subjectId;

        if (assignmentType != null && !assignmentType.isBlank()) {
            this.assignmentType = assignmentType;
        }

        this.weeklyPeriods = weeklyPeriods;
        this.workloadPercentage = workloadPercentage;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.roomReference = roomReference;
        this.timetableReference = timetableReference;
        this.workflowInstanceId = workflowInstanceId;
    }

    public String getAssignmentReference() {
        return assignmentReference;
    }

    public UUID getTeacherProfileId() {
        return teacherProfileId;
    }

    public UUID getEwfAssignmentId() {
        return ewfAssignmentId;
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

    public UUID getSubjectId() {
        return subjectId;
    }

    public String getAssignmentType() {
        return assignmentType;
    }

    public int getWeeklyPeriods() {
        return weeklyPeriods;
    }

    public BigDecimal getWorkloadPercentage() {
        return workloadPercentage;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public String getRoomReference() {
        return roomReference;
    }

    public UUID getTimetableReference() {
        return timetableReference;
    }

    public UUID getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public UUID getApprovedBy() {
        return approvedBy;
    }

    public String getAssignmentStatus() {
        return assignmentStatus;
    }

    public void markPendingApproval() {
        assignmentStatus = "PENDING_APPROVAL";
    }

    public void activate(
            UUID approvedBy
    ) {
        this.approvedBy = approvedBy;
        this.approvedAt = Instant.now();
        this.assignmentStatus = "ACTIVE";
    }

    public void suspend() {
        assignmentStatus = "SUSPENDED";
    }

    public void complete() {
        assignmentStatus = "COMPLETED";
    }

    public void cancel() {
        assignmentStatus = "CANCELLED";
    }
}
