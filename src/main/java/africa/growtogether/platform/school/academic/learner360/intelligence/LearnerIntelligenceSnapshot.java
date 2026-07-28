package africa.growtogether.platform.school.academic.learner360.intelligence;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "gts_learner_intelligence_snapshot")
public class LearnerIntelligenceSnapshot
        extends AuditedTenantEntity {


    @Column(
            name = "learner_id",
            nullable = false
    )
    private UUID learnerId;


    @Column(
            name = "achievement_status",
            length = 50
    )
    private String achievementStatus;


    @Column(
            name = "risk_level",
            length = 50
    )
    private String riskLevel;


    @Column(
            name = "support_required"
    )
    private Boolean supportRequired;


    @Column(
            name = "recommendation_summary",
            columnDefinition = "text"
    )
    private String recommendationSummary;


    @Column(
            name = "calculated_at"
    )
    private Instant calculatedAt;


    protected LearnerIntelligenceSnapshot() {

    }


    public LearnerIntelligenceSnapshot(
            UUID learnerId
    ) {

        this.learnerId = learnerId;
        this.calculatedAt = Instant.now();

    }


    public void updateIntelligence(
            String achievementStatus,
            String riskLevel,
            Boolean supportRequired,
            String recommendationSummary
    ) {

        this.achievementStatus = achievementStatus;
        this.riskLevel = riskLevel;
        this.supportRequired = supportRequired;
        this.recommendationSummary = recommendationSummary;
        this.calculatedAt = Instant.now();

    }


    public UUID getLearnerId() {

        return learnerId;

    }


    public String getAchievementStatus() {

        return achievementStatus;

    }


    public String getRiskLevel() {

        return riskLevel;

    }


    public Boolean getSupportRequired() {

        return supportRequired;

    }


    public String getRecommendationSummary() {

        return recommendationSummary;

    }


    public Instant getCalculatedAt() {

        return calculatedAt;

    }

}
