package africa.growtogether.platform.school.academic.learner360.support;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "gts_learner_support_plan")
public class LearnerSupportPlan extends AuditedTenantEntity {


    @Column(
            name = "learner_id",
            nullable = false
    )
    private UUID learnerId;


    @Column(
            name = "learner_intelligence_snapshot_id"
    )
    private UUID learnerIntelligenceSnapshotId;


    @Column(
            name = "risk_level",
            length = 50
    )
    private String riskLevel;


    @Column(
            name = "support_reason",
            columnDefinition = "text"
    )
    private String supportReason;


    @Column(
            name = "support_strategy",
            columnDefinition = "text"
    )
    private String supportStrategy;


    @Column(
            name = "assigned_staff_id"
    )
    private UUID assignedStaffId;


    @Column(
            name = "review_date"
    )
    private LocalDate reviewDate;


    @Column(
            name = "support_status",
            length = 50
    )
    private String supportStatus;


    @Column(
            name = "created_at"
    )
    private Instant createdAt;


    protected LearnerSupportPlan() {
    }


    public LearnerSupportPlan(
            UUID learnerId,
            UUID learnerIntelligenceSnapshotId,
            String riskLevel,
            String supportReason,
            String supportStrategy,
            UUID assignedStaffId,
            LocalDate reviewDate
    ) {

        this.learnerId = learnerId;
        this.learnerIntelligenceSnapshotId = learnerIntelligenceSnapshotId;
        this.riskLevel = riskLevel;
        this.supportReason = supportReason;
        this.supportStrategy = supportStrategy;
        this.assignedStaffId = assignedStaffId;
        this.reviewDate = reviewDate;
        this.supportStatus = "ACTIVE";
        this.createdAt = Instant.now();

    }


    public UUID getLearnerId() {
        return learnerId;
    }


    public String getRiskLevel() {
        return riskLevel;
    }


    public String getSupportReason() {
        return supportReason;
    }


    public String getSupportStrategy() {
        return supportStrategy;
    }


    public UUID getAssignedStaffId() {
        return assignedStaffId;
    }


    public LocalDate getReviewDate() {
        return reviewDate;
    }


    public String getSupportStatus() {
        return supportStatus;
    }


    public void setSupportStatus(String supportStatus) {
    this.supportStatus = supportStatus;
    }
    

    public Instant getCreatedAt() {
        return createdAt;
    }

}
