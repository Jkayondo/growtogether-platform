package africa.growtogether.platform.school.academic.outcome;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "gts_learning_outcome")
public class LearningOutcome extends AuditedTenantEntity {


    @Column(
            name = "curriculum_version_id",
            nullable = false
    )
    private UUID curriculumVersionId;


    @Column(
            name = "class_grade_id",
            nullable = false
    )
    private UUID classGradeId;


    @Column(
            name = "subject_id",
            nullable = false
    )
    private UUID subjectId;


    @Column(
            name = "outcome_code",
            nullable = false,
            length = 100
    )
    private String outcomeCode;


    @Column(
            name = "outcome_title",
            nullable = false,
            length = 300
    )
    private String outcomeTitle;


    @Column(
            name = "description",
            columnDefinition = "text"
    )
    private String description;


    @Column(
            name = "outcome_type",
            length = 50
    )
    private String outcomeType;


    @Column(
            name = "competency_area",
            length = 200
    )
    private String competencyArea;


    @Column(
            name = "sequence_number",
            nullable = false
    )
    private Integer sequenceNumber;


    @Column(name = "effective_from")
    private LocalDate effectiveFrom;


    @Column(name = "effective_to")
    private LocalDate effectiveTo;


    protected LearningOutcome() {
    }


    public LearningOutcome(
            UUID curriculumVersionId,
            UUID classGradeId,
            UUID subjectId,
            String outcomeCode,
            String outcomeTitle,
            Integer sequenceNumber
    ) {

        this.curriculumVersionId = curriculumVersionId;
        this.classGradeId = classGradeId;
        this.subjectId = subjectId;
        this.outcomeCode = outcomeCode;
        this.outcomeTitle = outcomeTitle;
        this.sequenceNumber = sequenceNumber;
    }


    public UUID getCurriculumVersionId() {
        return curriculumVersionId;
    }


    public UUID getClassGradeId() {
        return classGradeId;
    }


    public UUID getSubjectId() {
        return subjectId;
    }


    public String getOutcomeCode() {
        return outcomeCode;
    }


    public String getOutcomeTitle() {
        return outcomeTitle;
    }


    public String getDescription() {
        return description;
    }


    public String getOutcomeType() {
        return outcomeType;
    }


    public String getCompetencyArea() {
        return competencyArea;
    }


    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void archive() {

        this.setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.ARCHIVED
        );

}

}
