package africa.growtogether.platform.school.grading;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class GradeDivisionRuleService {


    private final GradeDivisionRuleRepository repository;


    public GradeDivisionRuleService(
            GradeDivisionRuleRepository repository
    ) {

        this.repository = repository;

    }



    @Transactional(readOnly = true)
    public GradeDivisionRule findDivisionForAggregate(
            UUID tenantId,
            UUID gradingSchemeId,
            Integer aggregate
    ) {


        List<GradeDivisionRule> rules =
                repository
                        .findByTenantIdAndGradingSchemeIdOrderBySequenceNumberAsc(
                                tenantId,
                                gradingSchemeId
                        );


        return rules.stream()
                .filter(rule -> rule.matches(aggregate))
                .findFirst()
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "No division rule matches aggregate: "
                                        + aggregate
                        )
                );

    }



    @Transactional(readOnly = true)
    public List<GradeDivisionRule> findByScheme(
            UUID tenantId,
            UUID gradingSchemeId
    ) {

        return repository
                .findByTenantIdAndGradingSchemeIdOrderBySequenceNumberAsc(
                        tenantId,
                        gradingSchemeId
                );

    }



    public GradeDivisionRule archive(
            UUID tenantId,
            UUID gradingSchemeId,
            String divisionCode
    ) {

        GradeDivisionRule rule =
                repository
                        .findByTenantIdAndGradingSchemeIdAndDivisionCode(
                                tenantId,
                                gradingSchemeId,
                                divisionCode
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Division rule not found"
                                )
                        );


        rule.setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.ARCHIVED
        );


        return repository.save(rule);

    }

}
