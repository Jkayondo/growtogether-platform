package africa.growtogether.platform.school.integration.teacher;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.ewf.WorkforceMemberRepository;
import africa.growtogether.platform.school.academic.teaching.*;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Establishes ownership only; EAIF permissions and governance remain mandatory. */
@Service
public class TeacherAiAccessGuard {
    private final EnterpriseIdentityContext identity;
    private final TeacherProfileRepository profiles;
    private final WorkforceMemberRepository workforce;
    private final TeachingAssignmentRepository assignments;

    public TeacherAiAccessGuard(EnterpriseIdentityContext identity,
            TeacherProfileRepository profiles, WorkforceMemberRepository workforce,
            TeachingAssignmentRepository assignments) {
        this.identity = identity;
        this.profiles = profiles;
        this.workforce = workforce;
        this.assignments = assignments;
    }

    @Transactional(readOnly = true)
    public TeachingAssignment requireOwnedAssignment(UUID tenantId,
            UUID teacherProfileId, UUID assignmentId) {
        identity.requireTenant(tenantId);
        UUID userId = identity.requireUserId();
        if (teacherProfileId == null || assignmentId == null) throw denied();

        var profile = profiles.findByTenantIdAndId(tenantId, teacherProfileId)
                .orElseThrow(TeacherAiAccessGuard::denied);
        if (profile.getStatus() != EntityStatus.ACTIVE
                || !"ACTIVE".equals(profile.getTeachingStatus())
                || profile.getWorkforceMemberId() == null) throw denied();

        var member = workforce.findByTenantIdAndId(
                tenantId, profile.getWorkforceMemberId())
                .orElseThrow(TeacherAiAccessGuard::denied);
        if (member.getStatus() != EntityStatus.ACTIVE
                || !"ACTIVE".equals(member.getWorkforceStatus())
                || !userId.equals(member.getEiamUserId())) throw denied();

        var assignment = assignments.findByTenantIdAndId(tenantId, assignmentId)
                .orElseThrow(TeacherAiAccessGuard::denied);
        if (assignment.getStatus() != EntityStatus.ACTIVE
                || !"ACTIVE".equals(assignment.getAssignmentStatus())
                || !teacherProfileId.equals(assignment.getTeacherProfileId()))
            throw denied();

        return assignment;
    }

    private static AccessDeniedException denied() {
        return new AccessDeniedException("Teaching context access denied.");
    }
}
