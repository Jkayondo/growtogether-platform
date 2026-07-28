package africa.growtogether.platform.school.academic.learner360.aggregation;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "gts_learner_360_summary")
public class Learner360Summary extends AuditedTenantEntity {


    @Column(
            name = "learner_id",
            nullable = false
    )
    private UUID learnerId;


    @Column(
            name = "learner_360_profile_id",
            nullable = false
    )
    private UUID learner360ProfileId;


    @Column(
            name = "overall_score"
    )
    private Double overallScore;


    @Column(
            name = "competency_completion_percentage"
    )
    private Double competencyCompletionPercentage;


    @Column(
            name = "assessment_count"
    )
    private Integer assessmentCount;


    @Column(
            name = "support_required"
    )
    private Boolean supportRequired;


    @Column(
            name = "risk_level",
            length = 50
    )
    private String riskLevel;


    @Column(
            name = "recommendation_summary",
            columnDefinition = "text"
    )
    private String recommendationSummary;


    @Column(
            name = "calculated_at"
    )
    private Instant calculatedAt;


    protected Learner360Summary() {

    }


    public Learner360Summary(
            UUID learnerId,
            UUID learner360ProfileId
    ) {

        this.learnerId = learnerId;
        this.learner360ProfileId = learner360ProfileId;
        this.calculatedAt = Instant.now();

    }


    public UUID getLearnerId() {

        return learnerId;

    }


    public UUID getLearner360ProfileId() {

        return learner360ProfileId;

    }


    public Double getOverallScore() {

        return overallScore;

    }


    public Double getCompetencyCompletionPercentage() {

        return competencyCompletionPercentage;

    }


    public Integer getAssessmentCount() {

        return assessmentCount;

    }


    public Boolean getSupportRequired() {

        return supportRequired;

    }


    public String getRiskLevel() {

        return riskLevel;

    }


    public String getRecommendationSummary() {

        return recommendationSummary;

    }


    public Instant getCalculatedAt() {

        return calculatedAt;

    }


    public void updatePerformance(
            Double score,
            Integer count
    ) {

        this.overallScore = score;
        this.assessmentCount = count;

    }


    public void updateCompetencyCompletion(
            Double percentage
    ) {

        this.competencyCompletionPercentage = percentage;

    }


    public void calculateRisk() {

        if (overallScore == null) {

            this.riskLevel = "UNKNOWN";
            this.supportRequired = true;
            return;

        }


        if (overallScore < 50) {

            this.riskLevel = "HIGH";
            this.supportRequired = true;

        } else if (overallScore < 80) {

            this.riskLevel = "MEDIUM";
            this.supportRequired = true;

        } else {

            this.riskLevel = "LOW";
            this.supportRequired = false;

        }

    }


    public void updateRecommendationSummary(
            String summary
    ) {

        this.recommendationSummary = summary;

    }


    public void setCalculatedAt(
            Instant calculatedAt
    ) {

        this.calculatedAt = calculatedAt;

    }

}