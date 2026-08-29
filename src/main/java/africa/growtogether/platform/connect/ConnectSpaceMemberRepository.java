package africa.growtogether.platform.connect;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectSpaceMemberRepository
        extends JpaRepository<ConnectSpaceMember, UUID> {

    Optional<ConnectSpaceMember>
    findByIdAndTenantId(
            UUID id,
            UUID tenantId
    );

    Optional<ConnectSpaceMember>
    findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
            UUID tenantId,
            UUID spaceId,
            UUID userId,
            ConnectMembershipStatus membershipStatus
    );

    boolean
    existsByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
            UUID tenantId,
            UUID spaceId,
            UUID userId,
            ConnectMembershipStatus membershipStatus
    );

    List<ConnectSpaceMember>
    findAllByTenantIdAndSpaceIdAndMembershipStatus(
            UUID tenantId,
            UUID spaceId,
            ConnectMembershipStatus membershipStatus
    );

    List<ConnectSpaceMember>
    findAllByTenantIdAndUserIdAndMembershipStatus(
            UUID tenantId,
            UUID userId,
            ConnectMembershipStatus membershipStatus
    );
}
