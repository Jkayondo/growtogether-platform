package africa.growtogether.platform.school.academic.learner360.rules.risk;


import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class RiskAssessmentService {


    private final RiskAssessmentRuleRepository repository;


    public RiskAssessmentService(
            RiskAssessmentRuleRepository repository
    ) {

        this.repository = repository;

    }


    public RiskAssessmentRule assess(
            UUID tenantId,
            Double score
    ) {


        if (score == null) {

            throw new IllegalArgumentException(
                    "Score is required."
            );

        }


        List<RiskAssessmentRule> rules =
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
                                "No risk assessment rule matches score."
                        )
                );

    }

}
