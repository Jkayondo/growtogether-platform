package africa.growtogether.platform.school.academic.assessment;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "gts_assessment")
public class Assessment extends AuditedTenantEntity {


    @Column(
            name = "learning_outcome_id",
            nullable = false
    )
    private UUID learningOutcomeId;


    @Column(
            name = "assessment_code",
            nullable = false,
            length = 100
    )
    private String assessmentCode;


    @Column(
            name = "assessment_title",
            nullable = false,
            length = 300
    )
    private String assessmentTitle;


    @Column(
            name = "assessment_type",
            length = 50
    )
    private String assessmentType;


    @Column(
            name = "description",
            columnDefinition = "text"
    )
    private String description;


    @Column(
            name = "assessment_method",
            length = 100
    )
    private String assessmentMethod;


    @Column(
            name = "maximum_score"
    )
    private Double maximumScore;


    @Column(
            name = "passing_score"
    )
    private Double passingScore;


    @Column(
            name = "weight_percentage"
    )
    private Double weightPercentage;


    @Column(
            name = "assessment_date"
    )
    private LocalDate assessmentDate;


    protected Assessment() {
    }


    public Assessment(
            UUID learningOutcomeId,
            String assessmentCode,
            String assessmentTitle
    ) {

        this.learningOutcomeId = learningOutcomeId;
        this.assessmentCode = assessmentCode;
        this.assessmentTitle = assessmentTitle;
    }


    public UUID getLearningOutcomeId() {
        return learningOutcomeId;
    }


    public String getAssessmentCode() {
        return assessmentCode;
    }


    public String getAssessmentTitle() {
        return assessmentTitle;
    }


    public String getAssessmentType() {
        return assessmentType;
    }


    public String getAssessmentMethod() {
        return assessmentMethod;
    }


    public Double getMaximumScore() {
        return maximumScore;
    }


    public Double getPassingScore() {
        return passingScore;
    }


    public Double getWeightPercentage() {
        return weightPercentage;
    }


    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }


    public void archive() {

        this.setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.ARCHIVED
        );

    }

}
