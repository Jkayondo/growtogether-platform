package africa.growtogether.platform.school.grading;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface GradingSchemeRepository
        extends JpaRepository<GradingScheme, UUID> {


    Optional<GradingScheme>
    findByTenantIdAndSchemeCode(
            UUID tenantId,
            String schemeCode
    );


    List<GradingScheme>
    findByTenantIdAndStatus(
            UUID tenantId,
            String status
    );


    boolean existsByTenantIdAndSchemeCode(
            UUID tenantId,
            String schemeCode
    );

}
