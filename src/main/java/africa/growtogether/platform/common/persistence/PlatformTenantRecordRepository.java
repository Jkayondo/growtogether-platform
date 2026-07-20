package africa.growtogether.platform.common.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlatformTenantRecordRepository extends JpaRepository<PlatformTenantRecord, UUID> {
    Optional<PlatformTenantRecord> findByTenantIdAndRecordKey(UUID tenantId, String recordKey);
}
