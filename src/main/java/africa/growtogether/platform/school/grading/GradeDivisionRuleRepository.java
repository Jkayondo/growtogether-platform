package africa.growtogether.platform.school.grading;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface GradeDivisionRuleRepository
        extends JpaRepository<GradeDivisionRule, UUID> {


    Optional<GradeDivisionRule>
    findByTenantIdAndGradingSchemeIdAndDivisionCode(
            UUID tenantId,
            UUID gradingSchemeId,
            String divisionCode
    );


    List<GradeDivisionRule>
    findByTenantIdAndGradingSchemeIdAndStatus(
            UUID tenantId,
            UUID gradingSchemeId,
            String status
    );


    List<GradeDivisionRule>
    findByTenantIdAndGradingSchemeIdOrderBySequenceNumberAsc(
            UUID tenantId,
            UUID gradingSchemeId
    );


    boolean existsByTenantIdAndGradingSchemeIdAndDivisionCode(
            UUID tenantId,
            UUID gradingSchemeId,
            String divisionCode
    );

}
