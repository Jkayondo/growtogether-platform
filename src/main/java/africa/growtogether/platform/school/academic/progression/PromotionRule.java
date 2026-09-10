package africa.growtogether.platform.school.academic.progression;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;


@Entity
@Table(name = "gts_promotion_rule")
public class PromotionRule extends AuditedTenantEntity {


    @Column(
            name = "rule_code",
            nullable = false,
            length = 80
    )
    private String ruleCode;


    @Column(
            name = "rule_name",
            nullable = false,
            length = 200
    )
    private String ruleName;


    @Column(
            name = "description",
            length = 1000
    )
    private String description;


    @Column(
            name = "education_level_id"
    )
    private UUID educationLevelId;


    @Column(
            name = "from_class_grade_id"
    )
    private UUID fromClassGradeId;


    @Column(
            name = "to_class_grade_id"
    )
    private UUID toClassGradeId;


    @Column(
            name = "minimum_average_score"
    )
    private BigDecimal minimumAverageScore;


    @Column(
            name = "minimum_subject_score"
    )
    private BigDecimal minimumSubjectScore;


    @Column(
            name = "allow_conditional_promotion",
            nullable = false
    )
    private boolean allowConditionalPromotion = false;


    @Column(
            name = "allow_repeat",
            nullable = false
    )
    private boolean allowRepeat = true;


    @Column(
            name = "rule_status",
            nullable = false,
            length = 30
    )
    private String ruleStatus = "ACTIVE";


    protected PromotionRule() {
    }


    public PromotionRule(
            UUID tenantId,
            String ruleCode,
            String ruleName
    ) {

        setTenantId(tenantId);

        this.ruleCode = ruleCode;
        this.ruleName = ruleName;

    }


    public void activate() {

        this.ruleStatus = "ACTIVE";

    }


    public void deactivate() {

        this.ruleStatus = "INACTIVE";

    }


    public String getRuleCode() {

        return ruleCode;

    }


    public String getRuleName() {

        return ruleName;

    }


    public String getRuleStatus() {

        return ruleStatus;

    }


    public BigDecimal getMinimumAverageScore() {

        return minimumAverageScore;

    }


    public BigDecimal getMinimumSubjectScore() {

        return minimumSubjectScore;

    }


    public boolean isAllowConditionalPromotion() {

        return allowConditionalPromotion;

    }


    public boolean isAllowRepeat() {

        return allowRepeat;

    }

}
