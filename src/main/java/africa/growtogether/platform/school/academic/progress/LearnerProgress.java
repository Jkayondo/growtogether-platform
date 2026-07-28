package africa.growtogether.platform.school.academic.progress;


import africa.growtogether.platform.common.persistence.AuditedTenantEntity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table(name = "gts_learner_progress")
public class LearnerProgress extends AuditedTenantEntity {


    @Column(
            name = "learner_id",
            nullable = false
    )
    private UUID learnerId;


    @Column(
            name = "assessment_id",
            nullable = false
    )
    private UUID assessmentId;


    @Column(
            name = "learning_outcome_id",
            nullable = false
    )
    private UUID learningOutcomeId;


    @Column(
            name = "score"
    )
    private Double score;


    @Column(
            name = "maximum_score"
    )
    private Double maximumScore;


    @Column(
            name = "percentage_score"
    )
    private Double percentageScore;


    @Column(
            name = "achievement_status",
            length = 50
    )
    private String achievementStatus;


    @Column(
            name = "teacher_comment",
            columnDefinition = "text"
    )
    private String teacherComment;


    @Column(
            name = "assessment_date"
    )
    private LocalDate assessmentDate;


    protected LearnerProgress() {
    }


    public LearnerProgress(
            UUID learnerId,
            UUID assessmentId,
            UUID learningOutcomeId
    ) {

        this.learnerId = learnerId;
        this.assessmentId = assessmentId;
        this.learningOutcomeId = learningOutcomeId;
    }


    public UUID getLearnerId() {
        return learnerId;
    }


    public UUID getAssessmentId() {
        return assessmentId;
    }


    public UUID getLearningOutcomeId() {
        return learningOutcomeId;
    }


    public Double getScore() {
        return score;
    }


    public Double getMaximumScore() {
        return maximumScore;
    }


    public Double getPercentageScore() {
        return percentageScore;
    }


    public String getAchievementStatus() {
        return achievementStatus;
    }


    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }


    public void archive() {

        this.setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.ARCHIVED
        );

    }

}
