package africa.growtogether.platform.school.academic.learner360.rules.recommendation;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class RecommendationGenerationService {


    private final RecommendationRuleRepository repository;


    public RecommendationGenerationService(
            RecommendationRuleRepository repository
    ) {

        this.repository = repository;

    }


    public RecommendationRule generate(
            UUID tenantId,
            String achievementStatus,
            String riskLevel
    ) {


        if (achievementStatus == null || riskLevel == null) {

            throw new IllegalArgumentException(
                    "Achievement status and risk level are required."
            );

        }


        List<RecommendationRule> rules =
                repository.findByTenantIdOrderByRiskLevelAsc(
                        tenantId
                );


        return rules.stream()
                .filter(
                        rule ->
                                rule.appliesTo(
                                        achievementStatus,
                                        riskLevel
                                )
                )
                .findFirst()
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "No recommendation rule matches learner profile."
                                )
                );

    }

}
