package africa.growtogether.platform.school.academic.learner360.learner;


import java.time.Instant;
import java.util.UUID;


public class LearnerSelfView {


    private UUID learnerId;

    private String achievementStatus;

    private String growthLevel;

    private Boolean supportRequired;

    private String growthMessage;

    private Instant updatedAt;


    public LearnerSelfView(
            UUID learnerId,
            String achievementStatus,
            String growthLevel,
            Boolean supportRequired,
            String growthMessage,
            Instant updatedAt
    ) {

        this.learnerId = learnerId;
        this.achievementStatus = achievementStatus;
        this.growthLevel = growthLevel;
        this.supportRequired = supportRequired;
        this.growthMessage = growthMessage;
        this.updatedAt = updatedAt;

    }


    public UUID getLearnerId() {
        return learnerId;
    }


    public String getAchievementStatus() {
        return achievementStatus;
    }


    public String getGrowthLevel() {
        return growthLevel;
    }


    public Boolean getSupportRequired() {
        return supportRequired;
    }


    public String getGrowthMessage() {
        return growthMessage;
    }


    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
