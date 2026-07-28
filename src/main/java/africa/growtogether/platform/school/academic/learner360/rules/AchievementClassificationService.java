package africa.growtogether.platform.school.academic.learner360.rules;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class AchievementClassificationService {


    private final PerformanceRuleRepository repository;


    public AchievementClassificationService(
            PerformanceRuleRepository repository
    ) {

        this.repository = repository;
    }



    public PerformanceRule classify(
            UUID tenantId,
            Double score
    ) {


        if (score == null) {

            throw new IllegalArgumentException(
                    "Score is required."
            );

        }


        List<PerformanceRule> rules =
                repository
                        .findByTenantIdOrderByMinimumScoreAsc(
                                tenantId
                        );


        return rules.stream()
                .filter(
                        rule -> rule.appliesTo(score)
                )
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No performance rule matches score."
                        )
                );

    }

}
