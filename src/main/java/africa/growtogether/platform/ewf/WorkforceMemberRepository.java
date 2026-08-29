package africa.growtogether.platform.ewf;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkforceMemberRepository
        extends JpaRepository<WorkforceMember, UUID> {

    Optional<WorkforceMember> findByTenantIdAndId(
            UUID tenantId,
            UUID id
    );

    Optional<WorkforceMember> findByTenantIdAndWorkforceNumber(
            UUID tenantId,
            String workforceNumber
    );

    Optional<WorkforceMember> findByTenantIdAndEmployeeNumber(
            UUID tenantId,
            String employeeNumber
    );

    List<WorkforceMember> findAllByTenantIdAndEiamUserId(
            UUID tenantId,
            UUID eiamUserId
    );

    List<WorkforceMember> findByTenantIdAndEmail(
            UUID tenantId,
            String email
    );

    List<WorkforceMember> findByTenantIdAndWorkforceCategory(
            UUID tenantId,
            String workforceCategory
    );

    List<WorkforceMember> findByTenantIdAndWorkforceStatus(
            UUID tenantId,
            String workforceStatus
    );
}
