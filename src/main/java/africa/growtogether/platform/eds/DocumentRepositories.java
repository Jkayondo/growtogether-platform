package africa.growtogether.platform.eds;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface DocumentVersionRepository
    extends JpaRepository<DocumentVersion, UUID> {

    List<DocumentVersion> findByDocumentIdAndTenantIdOrderByVersionNumberDesc(
        UUID documentId,
        UUID tenantId
    );

    boolean existsByTenantIdAndChecksum(
        UUID tenantId,
        String checksum
    );
}

interface DocumentLifecycleEventRepository
    extends JpaRepository<DocumentLifecycleEvent, UUID> {
}