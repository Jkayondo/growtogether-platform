package africa.growtogether.platform.school.integration.teacher;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.ewf.*;
import africa.growtogether.platform.school.academic.teaching.*;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeacherAiAccessGuardTest {
    private final UUID tenant = UUID.randomUUID();
    private final UUID user = UUID.randomUUID();
    private final UUID teacher = UUID.randomUUID();
    private final UUID memberId = UUID.randomUUID();
    private final UUID assignmentId = UUID.randomUUID();
    private final EnterpriseIdentityContext identity = mock(EnterpriseIdentityContext.class);
    private final TeacherProfileRepository profiles = mock(TeacherProfileRepository.class);
    private final WorkforceMemberRepository workforce = mock(WorkforceMemberRepository.class);
    private final TeachingAssignmentRepository assignments = mock(TeachingAssignmentRepository.class);
    private final TeacherProfile profile = mock(TeacherProfile.class);
    private final WorkforceMember member = mock(WorkforceMember.class);
    private final TeachingAssignment assignment = mock(TeachingAssignment.class);
    private final TeacherAiAccessGuard guard =
            new TeacherAiAccessGuard(identity, profiles, workforce, assignments);

    private void ownedContext() {
        when(identity.requireUserId()).thenReturn(user);
        when(profiles.findByTenantIdAndId(tenant, teacher)).thenReturn(Optional.of(profile));
        when(profile.getStatus()).thenReturn(EntityStatus.ACTIVE);
        when(profile.getTeachingStatus()).thenReturn("ACTIVE");
        when(profile.getWorkforceMemberId()).thenReturn(memberId);
        when(workforce.findByTenantIdAndId(tenant, memberId)).thenReturn(Optional.of(member));
        when(member.getStatus()).thenReturn(EntityStatus.ACTIVE);
        when(member.getWorkforceStatus()).thenReturn("ACTIVE");
        when(member.getEiamUserId()).thenReturn(user);
        when(assignments.findByTenantIdAndId(tenant, assignmentId))
                .thenReturn(Optional.of(assignment));
        when(assignment.getStatus()).thenReturn(EntityStatus.ACTIVE);
        when(assignment.getAssignmentStatus()).thenReturn("ACTIVE");
        when(assignment.getTeacherProfileId()).thenReturn(teacher);
    }

    private void denied() {
        assertThrows(AccessDeniedException.class,
                () -> guard.requireOwnedAssignment(tenant, teacher, assignmentId));
    }

    @Test void allowsOwnedActiveAssignment() {
        ownedContext();
        assertSame(assignment, guard.requireOwnedAssignment(tenant, teacher, assignmentId));
        verify(identity).requireTenant(tenant);
    }

    @Test void rejectsCrossTenantBeforeRepositoryAccess() {
        doThrow(new AccessDeniedException("denied")).when(identity).requireTenant(tenant);
        denied();
        verifyNoInteractions(profiles, workforce, assignments);
    }

    @Test void rejectsUnauthenticatedBeforeRepositoryAccess() {
        var previous = org.springframework.security.core.context.SecurityContextHolder.getContext();
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        try {
            var realIdentityGuard = new TeacherAiAccessGuard(
                    new EnterpriseIdentityContext(), profiles, workforce, assignments);
            assertThrows(AccessDeniedException.class,
                    () -> realIdentityGuard.requireOwnedAssignment(
                            tenant, teacher, assignmentId));
            verifyNoInteractions(profiles, workforce, assignments);
        } finally {
            org.springframework.security.core.context.SecurityContextHolder.setContext(previous);
        }
    }

    @Test void rejectsMissingProfile() {
        when(identity.requireUserId()).thenReturn(user);
        when(profiles.findByTenantIdAndId(tenant, teacher)).thenReturn(Optional.empty());
        denied();
        verifyNoInteractions(workforce, assignments);
    }

    @Test void rejectsAnotherUsersProfile() {
        ownedContext();
        when(member.getEiamUserId()).thenReturn(UUID.randomUUID());
        denied();
        verifyNoInteractions(assignments);
    }

    @Test void rejectsUnlinkedWorkforceMember() {
        ownedContext();
        when(member.getEiamUserId()).thenReturn(null);
        denied();
        verifyNoInteractions(assignments);
    }

    @Test void rejectsInactiveProfile() {
        ownedContext();
        when(profile.getStatus()).thenReturn(EntityStatus.INACTIVE);
        denied();
        verifyNoInteractions(workforce, assignments);
    }

    @Test void rejectsInactiveWorkforceMember() {
        ownedContext();
        when(member.getWorkforceStatus()).thenReturn("INACTIVE");
        denied();
        verifyNoInteractions(assignments);
    }

    @Test void rejectsAnotherTeachersAssignment() {
        ownedContext();
        when(assignment.getTeacherProfileId()).thenReturn(UUID.randomUUID());
        denied();
    }

    @Test void rejectsSuspendedAssignment() {
        ownedContext();
        when(assignment.getAssignmentStatus()).thenReturn("SUSPENDED");
        denied();
    }

    @Test void rejectsMissingAssignment() {
        ownedContext();
        when(assignments.findByTenantIdAndId(tenant, assignmentId))
                .thenReturn(Optional.empty());
        denied();
    }
}
