package africa.growtogether.platform.school.academic.learner360.dashboard;


import java.time.Instant;
import java.util.UUID;


public class LearnerIntelligenceDashboardView {


    private UUID learnerId;

    private String achievementStatus;

    private String riskLevel;

    private Boolean supportRequired;

    private String recommendationSummary;

    private Instant calculatedAt;


    public LearnerIntelligenceDashboardView(
            UUID learnerId,
            String achievementStatus,
            String riskLevel,
            Boolean supportRequired,
            String recommendationSummary,
            Instant calculatedAt
    ) {

        this.learnerId = learnerId;
        this.achievementStatus = achievementStatus;
        this.riskLevel = riskLevel;
        this.supportRequired = supportRequired;
        this.recommendationSummary = recommendationSummary;
        this.calculatedAt = calculatedAt;

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
