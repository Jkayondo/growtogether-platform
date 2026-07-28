package africa.growtogether.platform.school.academic.learner360.rules;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "gts_performance_rule")
public class PerformanceRule extends AuditedTenantEntity {


    @Column(
            name = "rule_name",
            nullable = false
    )
    private String ruleName;


    @Column(
            name = "minimum_score"
    )
    private Double minimumScore;


    @Column(
            name = "maximum_score"
    )
    private Double maximumScore;


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


    protected PerformanceRule() {

    }


    public PerformanceRule(
            String ruleName,
            Double minimumScore,
            Double maximumScore,
            String achievementStatus,
            String riskLevel
    ) {

        this.ruleName = ruleName;
        this.minimumScore = minimumScore;
        this.maximumScore = maximumScore;
        this.achievementStatus = achievementStatus;
        this.riskLevel = riskLevel;

    }


    public boolean appliesTo(
            Double score
    ) {

        return score >= minimumScore
                &&
                score <= maximumScore;

    }


    public String getAchievementStatus() {

        return achievementStatus;

    }


    public String getRiskLevel() {

        return riskLevel;

    }

}
