package africa.growtogether.platform.school.grading;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface EducationGradingProfileRepository
        extends JpaRepository<EducationGradingProfile, UUID> {


    Optional<EducationGradingProfile>
    findByTenantIdAndProfileCode(
            UUID tenantId,
            String profileCode
    );


    List<EducationGradingProfile>
    findByTenantIdAndStatus(
            UUID tenantId,
            String status
    );


    List<EducationGradingProfile>
    findByTenantIdAndCountryCode(
            UUID tenantId,
            String countryCode
    );


    boolean existsByTenantIdAndProfileCode(
            UUID tenantId,
            String profileCode
    );

}
