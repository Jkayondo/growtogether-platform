package africa.growtogether.platform.school.grading;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface CompetencyDescriptorRepository
        extends JpaRepository<CompetencyDescriptor, UUID> {


    Optional<CompetencyDescriptor>
    findByTenantIdAndGradingSchemeIdAndDescriptorCode(
            UUID tenantId,
            UUID gradingSchemeId,
            String descriptorCode
    );


    List<CompetencyDescriptor>
    findByTenantIdAndGradingSchemeIdAndStatus(
            UUID tenantId,
            UUID gradingSchemeId,
            String status
    );


    List<CompetencyDescriptor>
    findByTenantIdAndGradingSchemeIdOrderBySequenceNumberAsc(
            UUID tenantId,
            UUID gradingSchemeId
    );


    boolean existsByTenantIdAndGradingSchemeIdAndDescriptorCode(
            UUID tenantId,
            UUID gradingSchemeId,
            String descriptorCode
    );

}
