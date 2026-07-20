package africa.growtogether.platform.eds;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository
    extends JpaRepository<Document, UUID> {

    Optional<Document> findByIdAndTenantId(
        UUID id,
        UUID tenantId
    );
}