package africa.growtogether.platform.school.grading;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface GradeAggregationRuleRepository
        extends JpaRepository<GradeAggregationRule, UUID> {


    Optional<GradeAggregationRule>
    findByTenantIdAndRuleCode(
            UUID tenantId,
            String ruleCode
    );


    Optional<GradeAggregationRule>
    findByTenantIdAndGradingSchemeIdAndRuleCode(
            UUID tenantId,
            UUID gradingSchemeId,
            String ruleCode
    );


    List<GradeAggregationRule>
    findByTenantIdAndGradingSchemeId(
            UUID tenantId,
            UUID gradingSchemeId
    );


    List<GradeAggregationRule>
    findByTenantIdAndGradingSchemeIdAndStatus(
            UUID tenantId,
            UUID gradingSchemeId,
            String status
    );


    boolean existsByTenantIdAndGradingSchemeIdAndRuleCode(
            UUID tenantId,
            UUID gradingSchemeId,
            String ruleCode
    );

}
