package africa.growtogether.platform.school.assessment.marking;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface MarkSheetRepository
        extends JpaRepository<MarkSheet, UUID> {


    Optional<MarkSheet> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );


    Optional<MarkSheet> findByTenantIdAndMarkSheetReference(
            UUID tenantId,
            String reference
    );


    boolean existsByTenantIdAndMarkSheetReference(
            UUID tenantId,
            String reference
    );
}
