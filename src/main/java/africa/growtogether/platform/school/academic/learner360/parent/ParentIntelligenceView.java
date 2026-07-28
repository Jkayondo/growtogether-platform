package africa.growtogether.platform.school.academic.learner360.parent;


import java.time.Instant;
import java.util.UUID;


public class ParentIntelligenceView {


    private UUID learnerId;

    private String progressStatus;

    private String attentionLevel;

    private Boolean supportRequired;

    private String parentMessage;

    private Instant updatedAt;


    public ParentIntelligenceView(
            UUID learnerId,
            String progressStatus,
            String attentionLevel,
            Boolean supportRequired,
            String parentMessage,
            Instant updatedAt
    ) {

        this.learnerId = learnerId;
        this.progressStatus = progressStatus;
        this.attentionLevel = attentionLevel;
        this.supportRequired = supportRequired;
        this.parentMessage = parentMessage;
        this.updatedAt = updatedAt;

    }


    public UUID getLearnerId() {
        return learnerId;
    }


    public String getProgressStatus() {
        return progressStatus;
    }


    public String getAttentionLevel() {
        return attentionLevel;
    }


    public Boolean getSupportRequired() {
        return supportRequired;
    }


    public String getParentMessage() {
        return parentMessage;
    }


    public Instant getUpdatedAt() {
        return updatedAt;
    }

}
