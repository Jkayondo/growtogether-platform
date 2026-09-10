package africa.growtogether.platform.school.academic.progression;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;


@Service
@Transactional
public class PromotionRuleService {


    private final PromotionRuleRepository repository;


    public PromotionRuleService(
            PromotionRuleRepository repository
    ) {

        this.repository = repository;

    }


    public PromotionRule create(
            UUID tenantId,
            String ruleCode,
            String ruleName
    ) {


        PromotionRule rule =
                new PromotionRule(
                        tenantId,
                        ruleCode,
                        ruleName
                );


        return repository.save(rule);

    }


    @Transactional(readOnly = true)
    public PromotionRule findByCode(
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
                                "Promotion rule not found: " + ruleCode
                        )
                );

    }


    @Transactional(readOnly = true)
    public List<PromotionRule> findActiveRules(
            UUID tenantId
    ) {


        return repository
                .findByTenantIdAndRuleStatus(
                        tenantId,
                        "ACTIVE"
                );

    }


    public PromotionRule activate(
            UUID tenantId,
            String ruleCode
    ) {


        PromotionRule rule =
                findByCode(
                        tenantId,
                        ruleCode
                );


        rule.activate();


        return repository.save(rule);

    }


    public PromotionRule deactivate(
            UUID tenantId,
            String ruleCode
    ) {


        PromotionRule rule =
                findByCode(
                        tenantId,
                        ruleCode
                );


        rule.deactivate();


        return repository.save(rule);

    }

}
