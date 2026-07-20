package africa.growtogether.platform.eiam.membership;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRoleRepository extends JpaRepository<InvitationRole, UUID> {
    List<InvitationRole> findAllByTenantIdAndInvitationId(UUID tenantId, UUID invitationId);
    void deleteAllByTenantIdAndInvitationId(UUID tenantId, UUID invitationId);
}
