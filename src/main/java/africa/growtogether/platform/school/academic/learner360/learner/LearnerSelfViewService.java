package africa.growtogether.platform.school.academic.learner360.learner;


import africa.growtogether.platform.school.academic.learner360.intelligence.LearnerIntelligenceSnapshot;
import africa.growtogether.platform.school.academic.learner360.intelligence.LearnerIntelligenceSnapshotRepository;

import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class LearnerSelfViewService {


    private final LearnerIntelligenceSnapshotRepository repository;


    public LearnerSelfViewService(
            LearnerIntelligenceSnapshotRepository repository
    ) {

        this.repository = repository;

    }


    public LearnerSelfView getLearnerView(
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


        return new LearnerSelfView(
                snapshot.getLearnerId(),
                snapshot.getAchievementStatus(),
                buildGrowthLevel(snapshot),
                snapshot.getSupportRequired(),
                buildGrowthMessage(snapshot),
                snapshot.getCalculatedAt()
        );

    }


    private String buildGrowthLevel(
            LearnerIntelligenceSnapshot snapshot
    ) {

        return snapshot.getRiskLevel();

    }


    private String buildGrowthMessage(
            LearnerIntelligenceSnapshot snapshot
    ) {


        if (Boolean.TRUE.equals(snapshot.getSupportRequired())) {

            return "Your learning journey is progressing. "
                    + "Keep practising and focus on areas where you can improve. "
                    + "Your teachers are supporting your growth.";

        }


        return "Great work! Continue building your skills "
                + "and maintaining your learning habits.";

    }

}
