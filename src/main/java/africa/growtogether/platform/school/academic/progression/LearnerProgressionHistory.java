package africa.growtogether.platform.school.academic.progression;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(
        name = "gts_learner_progression_history"
)
public class LearnerProgressionHistory {


    @Id
    private UUID id;


    @Column(
            name = "tenant_id",
            nullable = false
    )
    private UUID tenantId;


    @Column(
            name = "learner_id",
            nullable = false
    )
    private UUID learnerId;


    private UUID academicYearId;


    private UUID previousClassGradeId;


    private UUID newClassGradeId;


    private UUID promotionDecisionId;


    @Column(
            nullable = false
    )
    private String progressionType;


    @Column(
            nullable = false
    )
    private String progressionStatus;


    @Column(
            nullable = false
    )
    private LocalDate effectiveDate;


    private String remarks;


    @Column(nullable = false)
    private java.time.Instant createdAt;


    @Column(nullable = false)
    private String createdBy;


    @Column(nullable = false)
    private java.time.Instant updatedAt;


    @Column(nullable = false)
    private String updatedBy;


    @Version
    private Long version;


    @Column(nullable = false)
    private String status;


    protected LearnerProgressionHistory() {
    }


    public LearnerProgressionHistory(
            UUID tenantId,
            UUID learnerId,
            UUID promotionDecisionId,
            String progressionType,
            LocalDate effectiveDate
    ) {

        this.id = UUID.randomUUID();
        this.tenantId = tenantId;
        this.learnerId = learnerId;
        this.promotionDecisionId = promotionDecisionId;
        this.progressionType = progressionType;
        this.progressionStatus = "CONFIRMED";
        this.effectiveDate = effectiveDate;

        this.createdAt = java.time.Instant.now();
        this.updatedAt = this.createdAt;

        this.createdBy = "system";
        this.updatedBy = "system";

        this.status = "ACTIVE";    }


    public void archive() {

        this.progressionStatus = "ARCHIVED";
        this.status = "ARCHIVED";
        this.updatedAt = java.time.Instant.now();
        this.updatedBy = "system";

    }



    public UUID getId() {
        return id;
    }


    public UUID getLearnerId() {
        return learnerId;
    }


    public String getProgressionType() {
        return progressionType;
    }


    public String getProgressionStatus() {
        return progressionStatus;
    }


    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }


    public UUID getPromotionDecisionId() {
        return promotionDecisionId;
    }
}
