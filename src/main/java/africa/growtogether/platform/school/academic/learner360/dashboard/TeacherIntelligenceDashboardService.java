package africa.growtogether.platform.school.academic.learner360.dashboard;


import africa.growtogether.platform.school.academic.learner360.intelligence.LearnerIntelligenceSnapshot;
import africa.growtogether.platform.school.academic.learner360.intelligence.LearnerIntelligenceSnapshotRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class TeacherIntelligenceDashboardService {


    private final LearnerIntelligenceSnapshotRepository repository;


    public TeacherIntelligenceDashboardService(
            LearnerIntelligenceSnapshotRepository repository
    ) {

        this.repository = repository;

    }


    public LearnerIntelligenceDashboardView getLearnerView(
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


        return mapToView(snapshot);

    }


    private LearnerIntelligenceDashboardView mapToView(
            LearnerIntelligenceSnapshot snapshot
    ) {


        return new LearnerIntelligenceDashboardView(
                snapshot.getLearnerId(),
                snapshot.getAchievementStatus(),
                snapshot.getRiskLevel(),
                snapshot.getSupportRequired(),
                snapshot.getRecommendationSummary(),
                snapshot.getCalculatedAt()
        );

    }

}
