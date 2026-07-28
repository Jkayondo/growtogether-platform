package africa.growtogether.platform.school.academic.learner360.rules.risk;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "gts_risk_assessment_rule")
public class RiskAssessmentRule extends AuditedTenantEntity {


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
            name = "risk_level",
            nullable = false,
            length = 50
    )
    private String riskLevel;


    @Column(
            name = "support_required"
    )
    private Boolean supportRequired;


    protected RiskAssessmentRule() {

    }


    public RiskAssessmentRule(
            String ruleName,
            Double minimumScore,
            Double maximumScore,
            String riskLevel,
            Boolean supportRequired
    ) {

        this.ruleName = ruleName;
        this.minimumScore = minimumScore;
        this.maximumScore = maximumScore;
        this.riskLevel = riskLevel;
        this.supportRequired = supportRequired;

    }


    public boolean appliesTo(
            Double score
    ) {

        return score >= minimumScore
                &&
                score <= maximumScore;

    }


    public String getRiskLevel() {

        return riskLevel;

    }


    public Boolean getSupportRequired() {

        return supportRequired;

    }

}
