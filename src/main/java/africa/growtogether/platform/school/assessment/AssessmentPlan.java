package africa.growtogether.platform.school.assessment;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "gts_assessment_plan")
public class AssessmentPlan
        extends AuditedTenantEntity {


    @Column(
            name = "plan_code",
            nullable = false,
            length = 100
    )
    private String planCode;


    @Column(
            name = "plan_name",
            nullable = false,
            length = 250
    )
    private String planName;


    @Column(
            name = "description",
            length = 1500
    )
    private String description;


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


    @Column(name = "academic_programme_id")
    private UUID academicProgrammeId;


    @Column(name = "study_track_id")
    private UUID studyTrackId;


    @Column(name = "curriculum_version_id")
    private UUID curriculumVersionId;


    @Column(
            name = "class_grade_id",
            nullable = false
    )
    private UUID classGradeId;


    @Column(name = "stream_id")
    private UUID streamId;


    @Column(name = "grading_scheme_id")
    private UUID gradingSchemeId;


    @Column(
            name = "effective_from",
            nullable = false
    )
    private LocalDate effectiveFrom;


    @Column(name = "effective_to")
    private LocalDate effectiveTo;


    @Column(name = "workflow_instance_id")
    private UUID workflowInstanceId;


    @Column(name = "approved_at")
    private Instant approvedAt;


    @Column(name = "approved_by")
    private UUID approvedBy;


    @Column(
            name = "plan_status",
            nullable = false,
            length = 30
    )
    private String planStatus = "DRAFT";


    protected AssessmentPlan() {
    }


    public AssessmentPlan(
            String planCode,
            String planName,
            String description,
            UUID academicYearId,
            UUID academicTermId,
            UUID campusId,
            UUID academicProgrammeId,
            UUID studyTrackId,
            UUID curriculumVersionId,
            UUID classGradeId,
            UUID streamId,
            UUID gradingSchemeId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            UUID workflowInstanceId
    ) {

        if (academicYearId == null) {
            throw new IllegalArgumentException(
                    "academicYearId must not be null"
            );
        }

        if (campusId == null) {
            throw new IllegalArgumentException(
                    "campusId must not be null"
            );
        }

        if (classGradeId == null) {
            throw new IllegalArgumentException(
                    "classGradeId must not be null"
            );
        }

        if (effectiveFrom == null) {
            throw new IllegalArgumentException(
                    "effectiveFrom must not be null"
            );
        }

        if (
                effectiveTo != null
                && effectiveTo.isBefore(effectiveFrom)
        ) {
            throw new IllegalArgumentException(
                    "effectiveTo must not be before effectiveFrom"
            );
        }

        this.planCode = requireText(
                planCode,
                "planCode"
        );

        this.planName = requireText(
                planName,
                "planName"
        );

        this.description = description;
        this.academicYearId = academicYearId;
        this.academicTermId = academicTermId;
        this.campusId = campusId;
        this.academicProgrammeId = academicProgrammeId;
        this.studyTrackId = studyTrackId;
        this.curriculumVersionId = curriculumVersionId;
        this.classGradeId = classGradeId;
        this.streamId = streamId;
        this.gradingSchemeId = gradingSchemeId;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.workflowInstanceId = workflowInstanceId;
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


    public String getPlanCode() {
        return planCode;
    }


    public String getPlanName() {
        return planName;
    }


    public String getDescription() {
        return description;
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


    public UUID getAcademicProgrammeId() {
        return academicProgrammeId;
    }


    public UUID getStudyTrackId() {
        return studyTrackId;
    }


    public UUID getCurriculumVersionId() {
        return curriculumVersionId;
    }


    public UUID getClassGradeId() {
        return classGradeId;
    }


    public UUID getStreamId() {
        return streamId;
    }


    public UUID getGradingSchemeId() {
        return gradingSchemeId;
    }


    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }


    public LocalDate getEffectiveTo() {
        return effectiveTo;
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


    public String getPlanStatus() {
        return planStatus;
    }
}
