package africa.growtogether.platform.school.academic.learner360.parent;


import africa.growtogether.platform.school.academic.learner360.intelligence.LearnerIntelligenceSnapshot;
import africa.growtogether.platform.school.academic.learner360.intelligence.LearnerIntelligenceSnapshotRepository;

import org.springframework.stereotype.Service;


import java.util.UUID;


@Service
public class ParentIntelligenceService {


    private final LearnerIntelligenceSnapshotRepository repository;


    public ParentIntelligenceService(
            LearnerIntelligenceSnapshotRepository repository
    ) {

        this.repository = repository;

    }


    public ParentIntelligenceView getParentView(
            UUID tenantId,
            UUID learnerId
    ) {


        LearnerIntelligenceSnapshot snapshot =
                repository
                        .findByTenantIdAndLearnerId(
                                tenantId,
                                learnerId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Learner intelligence not found"
                                        )
                        );


        return new ParentIntelligenceView(
                snapshot.getLearnerId(),
                buildProgressStatus(snapshot),
                buildAttentionLevel(snapshot),
                snapshot.getSupportRequired(),
                buildParentMessage(snapshot),
                snapshot.getCalculatedAt()
        );

    }


    private String buildProgressStatus(
            LearnerIntelligenceSnapshot snapshot
    ) {

        return snapshot.getAchievementStatus();

    }


    private String buildAttentionLevel(
            LearnerIntelligenceSnapshot snapshot
    ) {

        return snapshot.getRiskLevel();

    }


    private String buildParentMessage(
            LearnerIntelligenceSnapshot snapshot
    ) {


        if (Boolean.TRUE.equals(snapshot.getSupportRequired())) {

            return "Your child may benefit from additional learning support. "
                    + "Please work together with the school to encourage progress.";

        }


        return "Your child is progressing well. "
                + "Continue encouraging regular learning habits.";

    }

}
