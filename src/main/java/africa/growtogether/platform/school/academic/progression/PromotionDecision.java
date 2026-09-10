package africa.growtogether.platform.school.academic.progression;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.util.UUID;


@Entity
@Table(name = "gts_promotion_decision")
public class PromotionDecision extends AuditedTenantEntity {


    @Column(
            name = "learner_id",
            nullable = false
    )
    private UUID learnerId;


    @Column(
            name = "academic_year_id"
    )
    private UUID academicYearId;


    @Column(
            name = "current_class_grade_id"
    )
    private UUID currentClassGradeId;


    @Column(
            name = "promoted_class_grade_id"
    )
    private UUID promotedClassGradeId;


    @Column(
            name = "promotion_rule_id"
    )
    private UUID promotionRuleId;


    @Column(
            name = "decision_status",
            nullable = false,
            length = 40
    )
    private String decisionStatus = "PENDING_REVIEW";


    @Column(
            name = "decision_reason",
            length = 1000
    )
    private String decisionReason;


    @Column(
            name = "reviewed_by",
            length = 150
    )
    private String reviewedBy;


    protected PromotionDecision() {
    }


    public PromotionDecision(
            UUID tenantId,
            UUID learnerId,
            UUID promotionRuleId
    ) {

        setTenantId(tenantId);

        this.learnerId = learnerId;
        this.promotionRuleId = promotionRuleId;

    }


    public void recommend() {

        this.decisionStatus = "RECOMMENDED";

    }


    public void approve() {

        this.decisionStatus = "APPROVED";

    }


    public void promote() {

        this.decisionStatus = "PROMOTED";

    }


    public void repeat() {

        this.decisionStatus = "REPEATED";

    }


    public void reject() {

        this.decisionStatus = "REJECTED";

    }


    public UUID getLearnerId() {

        return learnerId;

    }


    public UUID getPromotionRuleId() {

        return promotionRuleId;

    }


    public String getDecisionStatus() {

        return decisionStatus;

    }


    public String getDecisionReason() {

        return decisionReason;

    }

}
