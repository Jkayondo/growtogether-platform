package africa.growtogether.platform.school.academic.learner360;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "gts_learner_360_profile")
public class Learner360Profile extends AuditedTenantEntity {


    @Column(
            name = "learner_id",
            nullable = false
    )
    private UUID learnerId;


    @Column(
            name = "academic_profile_id",
            nullable = false
    )
    private UUID academicProfileId;


    @Column(
            name = "current_class_grade_id"
    )
    private UUID currentClassGradeId;


    @Column(
            name = "current_curriculum_version_id"
    )
    private UUID currentCurriculumVersionId;


    @Column(
            name = "overall_performance_level",
            length = 50
    )
    private String overallPerformanceLevel;


    @Column(
            name = "competency_progress_level",
            length = 50
    )
    private String competencyProgressLevel;


    @Column(
            name = "learning_risk_level",
            length = 50
    )
    private String learningRiskLevel;


    @Column(
            name = "growth_summary",
            columnDefinition = "text"
    )
    private String growthSummary;


    protected Learner360Profile() {
    }


    public Learner360Profile(
            UUID learnerId,
            UUID academicProfileId
    ) {

        this.learnerId = learnerId;
        this.academicProfileId = academicProfileId;

        this.overallPerformanceLevel = "UNKNOWN";
        this.competencyProgressLevel = "UNKNOWN";
        this.learningRiskLevel = "UNKNOWN";
    }


    public UUID getLearnerId() {
        return learnerId;
    }


    public UUID getAcademicProfileId() {
        return academicProfileId;
    }


    public UUID getCurrentClassGradeId() {
        return currentClassGradeId;
    }


    public UUID getCurrentCurriculumVersionId() {
        return currentCurriculumVersionId;
    }


    public String getOverallPerformanceLevel() {
        return overallPerformanceLevel;
    }


    public String getCompetencyProgressLevel() {
        return competencyProgressLevel;
    }


    public String getLearningRiskLevel() {
        return learningRiskLevel;
    }


    public String getGrowthSummary() {
        return growthSummary;
    }

    public void updateLearningRiskLevel(
            String riskLevel
    ) {

        this.learningRiskLevel = riskLevel;

    }


    public void updateGrowthSummary(
            String summary
    ) {

         this.growthSummary = summary;

     }    

     public void archive() {

        this.setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.ARCHIVED
        );

    }

}
