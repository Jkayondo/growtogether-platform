package africa.growtogether.platform.school.grading;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class DivisionClassificationEngineService {


    private final GradeDivisionRuleRepository repository;


    public DivisionClassificationEngineService(
            GradeDivisionRuleRepository repository
    ) {

        this.repository = repository;

    }



    @Transactional(readOnly = true)
    public DivisionClassificationResult classify(

            UUID tenantId,

            UUID gradingSchemeId,

            BigDecimal aggregate

    ) {


        List<GradeDivisionRule> rules =
                repository
                        .findByTenantIdAndGradingSchemeIdAndStatus(
                                tenantId,
                                gradingSchemeId,
                                "ACTIVE"
                        );


        GradeDivisionRule matchedRule =
                rules.stream()
                        .filter(rule -> rule.matches(aggregate.intValue()))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "No division rule matches aggregate: "
                                                        + aggregate
                                        )
                        );


        return new DivisionClassificationResult(

                aggregate,

                matchedRule.getDivisionCode(),

                matchedRule.getDivisionName(),

                matchedRule.getDescription()

        );

    }

}
