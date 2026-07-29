package africa.growtogether.platform.school.academic.learner360.support;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "gts_learner_support_recommendation")
public class LearnerSupportRecommendation extends AuditedTenantEntity {


    @Column(
            name = "learner_id",
            nullable = false
    )
    private UUID learnerId;


    @Column(
            name = "intelligence_snapshot_id"
    )
    private UUID intelligenceSnapshotId;


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


    @Column(
            name = "workflow_instance_id"
    )
    private UUID workflowInstanceId;


    @Column(
            name = "recommendation_status",
            length = 50
    )
    private String recommendationStatus;


    @Column(
            name = "reviewed_by"
    )
    private String reviewedBy;


    @Column(
            name = "reviewed_at"
    )
    private Instant reviewedAt;


    protected LearnerSupportRecommendation() {
    }


    public LearnerSupportRecommendation(
            UUID learnerId,
            UUID intelligenceSnapshotId,
            String riskLevel,
            String recommendationText
    ) {

        this.learnerId = learnerId;
        this.intelligenceSnapshotId = intelligenceSnapshotId;
        this.riskLevel = riskLevel;
        this.recommendationText = recommendationText;
        this.recommendationStatus = "PENDING_REVIEW";

    }


    public void startWorkflow(
            UUID workflowInstanceId
    ) {

        this.workflowInstanceId = workflowInstanceId;
        this.recommendationStatus = "WORKFLOW_STARTED";

    }


    public void approve(
            String reviewedBy
    ) {

        this.recommendationStatus = "APPROVED";
        this.reviewedBy = reviewedBy;
        this.reviewedAt = Instant.now();

    }


    public void reject(
            String reviewedBy
    ) {

        this.recommendationStatus = "REJECTED";
        this.reviewedBy = reviewedBy;
        this.reviewedAt = Instant.now();

    }


    public UUID getLearnerId() {
        return learnerId;
    }


    public UUID getIntelligenceSnapshotId() {
        return intelligenceSnapshotId;
    }


    public String getRiskLevel() {
        return riskLevel;
    }


    public String getRecommendationText() {
        return recommendationText;
    }


    public UUID getWorkflowInstanceId() {
        return workflowInstanceId;
    }


    public String getRecommendationStatus() {
        return recommendationStatus;
    }


    public String getReviewedBy() {
        return reviewedBy;
    }


    public Instant getReviewedAt() {
        return reviewedAt;
    }

}
