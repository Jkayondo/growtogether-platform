package africa.growtogether.platform.school.academic.learner360.intelligence;


import africa.growtogether.platform.school.academic.learner360.rules.AchievementClassificationService;
import africa.growtogether.platform.school.academic.learner360.rules.PerformanceRule;
import africa.growtogether.platform.school.academic.learner360.rules.risk.RiskAssessmentRule;
import africa.growtogether.platform.school.academic.learner360.rules.risk.RiskAssessmentService;
import africa.growtogether.platform.school.academic.learner360.rules.recommendation.RecommendationGenerationService;
import africa.growtogether.platform.school.academic.learner360.rules.recommendation.RecommendationRule;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
public class LearnerIntelligenceProcessingService {


    private final AchievementClassificationService achievementService;

    private final RiskAssessmentService riskService;

    private final RecommendationGenerationService recommendationService;

    private final LearnerIntelligenceSnapshotRepository repository;


    public LearnerIntelligenceProcessingService(
            AchievementClassificationService achievementService,
            RiskAssessmentService riskService,
            RecommendationGenerationService recommendationService,
            LearnerIntelligenceSnapshotRepository repository
    ) {

        this.achievementService = achievementService;
        this.riskService = riskService;
        this.recommendationService = recommendationService;
        this.repository = repository;

    }


    @Transactional
    public LearnerIntelligenceSnapshot process(
            UUID tenantId,
            UUID learnerId,
            Double score
    ) {


        PerformanceRule achievementRule =
                achievementService.classify(
                        tenantId,
                        score
                );


        RiskAssessmentRule riskRule =
                riskService.assess(
                        tenantId,
                        score
                );


        RecommendationRule recommendationRule =
                recommendationService.generate(
                        tenantId,
                        achievementRule.getAchievementStatus(),
                        riskRule.getRiskLevel()
                );


        LearnerIntelligenceSnapshot snapshot =
                repository
                        .findByTenantIdAndLearnerId(
                                tenantId,
                                learnerId
                        )
                        .orElseGet(
                                () ->
                                        new LearnerIntelligenceSnapshot(
                                                learnerId
                                        )
                        );


        snapshot.updateIntelligence(
                achievementRule.getAchievementStatus(),
                riskRule.getRiskLevel(),
                riskRule.getSupportRequired(),
                recommendationRule.getRecommendationText()
        );


        return repository.save(
                snapshot
        );

    }

}
