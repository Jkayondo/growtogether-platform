package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import africa.growtogether.platform.ewf.WorkforceMember;
import africa.growtogether.platform.ewf.WorkforceMemberRepository;

import africa.growtogether.platform.school.academic.teaching.TeacherProfile;
import africa.growtogether.platform.school.academic.teaching.TeacherProfileRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ConnectTeacherAuthorizationService {

    private final EnterpriseIdentityContext identity;
    private final WorkforceMemberRepository workforceMembers;
    private final TeacherProfileRepository teacherProfiles;

    public ConnectTeacherAuthorizationService(
            EnterpriseIdentityContext identity,
            WorkforceMemberRepository workforceMembers,
            TeacherProfileRepository teacherProfiles
    ) {
        this.identity = identity;
        this.workforceMembers = workforceMembers;
        this.teacherProfiles = teacherProfiles;
    }

    @Transactional(readOnly = true)
    public TeacherProfile requireCurrentTeacherProfile() {

        UUID authenticatedUserId =
                identity.requireUserId();

        return requireTeacherProfileForUser(
                authenticatedUserId
        );
    }

    @Transactional(readOnly = true)
    public TeacherProfile requireTeacherProfileForUser(UUID userId) {
        return findActiveTeacherProfilesForUser(userId)
                .stream()
                .findFirst()
                .orElseThrow(ConnectTeacherAuthorizationService::denied);
    }

    @Transactional(readOnly = true)
    public TeacherProfile requireUniqueCurrentTeacherProfile() {
        List<TeacherProfile> matches =
                findActiveTeacherProfilesForUser(identity.requireUserId());

        if (matches.size() != 1) {
            throw new AccessDeniedException(
                    "Exactly one active teacher profile is required for this user and tenant"
            );
        }

        return matches.get(0);
    }

    private List<TeacherProfile> findActiveTeacherProfilesForUser(UUID userId) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "userId must not be null"
            );
        }

        UUID tenantId =
                identity.requireTenantId();

        List<WorkforceMember> linkedWorkforceMembers =
                workforceMembers
                        .findAllByTenantIdAndEiamUserId(
                                tenantId,
                                userId
                        );

        if (linkedWorkforceMembers.isEmpty()) {
            throw denied();
        }

        return linkedWorkforceMembers
                .stream()
                .filter(
                        member ->
                                "ACTIVE".equals(
                                        member.getWorkforceStatus()
                                )
                )
                .filter(
                        member ->
                                member.getStatus()
                                        == EntityStatus.ACTIVE
                )
                .map(
                        member ->
                                teacherProfiles
                                        .findByTenantIdAndWorkforceMemberId(
                                                tenantId,
                                                member.getId()
                                        )
                )
                .flatMap(
                        java.util.Optional::stream
                )
                .filter(
                        teacher ->
                                "ACTIVE".equals(
                                        teacher.getTeachingStatus()
                                )
                )
                .filter(
                        teacher ->
                                teacher.getStatus()
                                        == EntityStatus.ACTIVE
                )
                .toList();
    }

    private static AccessDeniedException denied() {

        return new AccessDeniedException(
                "User is not an active authorised teacher for this tenant"
        );
    }
}
