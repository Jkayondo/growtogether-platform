package africa.growtogether.platform.school.academic.learner360.support;

import africa.growtogether.platform.common.persistence.AuditedTenantEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "gts_learner_support_review")
public class LearnerSupportReview extends AuditedTenantEntity {

    @Column(name = "support_plan_id", nullable = false)
    private UUID supportPlanId;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "reviewer_staff_id")
    private UUID reviewerStaffId;

    @Column(name = "review_outcome", columnDefinition = "TEXT")
    private String reviewOutcome;

    @Column(name = "observations", columnDefinition = "TEXT")
    private String observations;

    @Column(name = "next_review_date")
    private LocalDate nextReviewDate;


    protected LearnerSupportReview() {
    }


    public LearnerSupportReview(
            UUID supportPlanId,
            LocalDate reviewDate,
            UUID reviewerStaffId,
            String reviewOutcome,
            String observations,
            LocalDate nextReviewDate
    ) {
        this.supportPlanId = supportPlanId;
        this.reviewDate = reviewDate;
        this.reviewerStaffId = reviewerStaffId;
        this.reviewOutcome = reviewOutcome;
        this.observations = observations;
        this.nextReviewDate = nextReviewDate;
    }


    public UUID getSupportPlanId() {
        return supportPlanId;
    }


    public LocalDate getReviewDate() {
        return reviewDate;
    }


    public UUID getReviewerStaffId() {
        return reviewerStaffId;
    }


    public String getReviewOutcome() {
        return reviewOutcome;
    }


    public String getObservations() {
        return observations;
    }


    public LocalDate getNextReviewDate() {
        return nextReviewDate;
    }
}
