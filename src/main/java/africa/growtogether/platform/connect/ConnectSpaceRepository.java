package africa.growtogether.platform.connect;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectSpaceRepository
        extends JpaRepository<ConnectSpace, UUID> {

    Optional<ConnectSpace> findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    List<ConnectSpace> findAllByTenantId(
            UUID tenantId
    );

    Optional<ConnectSpace>
    findByTenantIdAndSpaceTypeAndContextTypeAndContextReference(
            UUID tenantId,
            ConnectSpaceType spaceType,
            String contextType,
            String contextReference
    );
}
