package africa.growtogether.platform.eiam.membership;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationInvitationRepository extends JpaRepository<OrganizationInvitation, UUID> {
    Optional<OrganizationInvitation> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<OrganizationInvitation> findByTenantIdAndTokenHash(UUID tenantId, String tokenHash);
    Optional<OrganizationInvitation> findFirstByTenantIdAndEmailIgnoreCaseAndInvitationStatus(UUID tenantId, String email, InvitationStatus status);
    List<OrganizationInvitation> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
