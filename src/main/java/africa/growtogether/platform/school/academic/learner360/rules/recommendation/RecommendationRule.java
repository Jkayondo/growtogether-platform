package africa.growtogether.platform.school.academic.learner360.rules.recommendation;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;


import java.util.UUID;


@Entity
@Table(name = "gts_recommendation_rule")
public class RecommendationRule extends AuditedTenantEntity {


    @Column(
            name = "rule_name",
            nullable = false
    )
    private String ruleName;


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
            name = "recommendation_text",
            columnDefinition = "text"
    )
    private String recommendationText;


    protected RecommendationRule() {

    }


    public RecommendationRule(
            String ruleName,
            String achievementStatus,
            String riskLevel,
            String recommendationText
    ) {

        this.ruleName = ruleName;
        this.achievementStatus = achievementStatus;
        this.riskLevel = riskLevel;
        this.recommendationText = recommendationText;

    }


    public boolean appliesTo(
            String achievementStatus,
            String riskLevel
    ) {

        return this.achievementStatus.equals(
                achievementStatus
        )
        &&
        this.riskLevel.equals(
                riskLevel
        );

    }


    public String getRecommendationText() {

        return recommendationText;

    }

}
