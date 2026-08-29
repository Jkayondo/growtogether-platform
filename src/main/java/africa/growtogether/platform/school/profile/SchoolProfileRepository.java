package africa.growtogether.platform.school.profile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SchoolProfileRepository
        extends JpaRepository<SchoolProfile, UUID> {

    Optional<SchoolProfile> findByTenantId(
            UUID tenantId
    );

    Optional<SchoolProfile> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    boolean existsByTenantId(
            UUID tenantId
    );
}
