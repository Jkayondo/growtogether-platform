package africa.growtogether.platform.school.grading;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class GradeAggregationRuleService {


    private final GradeAggregationRuleRepository repository;


    public GradeAggregationRuleService(
            GradeAggregationRuleRepository repository
    ) {

        this.repository = repository;

    }



    public GradeAggregationRule create(
            UUID tenantId,
            GradeAggregationRule rule
    ) {


        if (tenantId == null) {

            throw new IllegalArgumentException(
                    "tenantId must not be null"
            );

        }


        if (
                rule.getRuleCode() == null
                || rule.getRuleCode().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "Rule code is required"
            );

        }


        if (
                repository.existsByTenantIdAndGradingSchemeIdAndRuleCode(
                        tenantId,
                        rule.getGradingSchemeId(),
                        rule.getRuleCode()
                )
        ) {

            throw new IllegalArgumentException(
                    "Aggregation rule already exists"
            );

        }


        rule.setTenantId(
                tenantId
        );


        return repository.save(
                rule
        );

    }



    @Transactional(readOnly = true)
    public GradeAggregationRule findByCode(
            UUID tenantId,
            String ruleCode
    ) {

        return repository
                .findByTenantIdAndRuleCode(
                        tenantId,
                        ruleCode
                )
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "Aggregation rule not found"
                        )
                );

    }



    @Transactional(readOnly = true)
    public List<GradeAggregationRule> findByScheme(
            UUID tenantId,
            UUID gradingSchemeId
    ) {

        return repository
                .findByTenantIdAndGradingSchemeId(
                        tenantId,
                        gradingSchemeId
                );

    }



    public GradeAggregationRule archive(
            UUID tenantId,
            String ruleCode
    ) {

        GradeAggregationRule rule =
                findByCode(
                        tenantId,
                        ruleCode
                );


        rule.setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.ARCHIVED
        );


        return repository.save(
                rule
        );

    }



    public GradeAggregationRule activate(
            UUID tenantId,
            String ruleCode
    ) {

        GradeAggregationRule rule =
                findByCode(
                        tenantId,
                        ruleCode
                );


        rule.setStatus(
                africa.growtogether.platform.common.persistence.EntityStatus.ACTIVE
        );


        return repository.save(
                rule
        );

    }

}
