package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import africa.growtogether.platform.ewf.WorkforceMember;
import africa.growtogether.platform.ewf.WorkforceMemberRepository;

import africa.growtogether.platform.school.academic.teaching.TeacherProfile;
import africa.growtogether.platform.school.academic.teaching.TeacherProfileRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectTeacherAuthorizationServiceTest {

    @Mock
    private EnterpriseIdentityContext identity;

    @Mock
    private WorkforceMemberRepository workforceMembers;

    @Mock
    private TeacherProfileRepository teacherProfiles;

    private ConnectTeacherAuthorizationService service;

    private UUID tenantId;
    private UUID authenticatedUserId;
    private UUID workforceMemberId;

    @BeforeEach
    void setUp() {

        service =
                new ConnectTeacherAuthorizationService(
                        identity,
                        workforceMembers,
                        teacherProfiles
                );

        tenantId =
                UUID.randomUUID();

        authenticatedUserId =
                UUID.randomUUID();

        workforceMemberId =
                UUID.randomUUID();
    }

    @Test
    void activeWorkforceTeacherIsAuthorized() {

        stubTenant();

        WorkforceMember workforceMember =
                activeWorkforceMember();

        TeacherProfile teacher =
                activeTeacherProfile();

        when(
                workforceMembers
                        .findAllByTenantIdAndEiamUserId(
                                tenantId,
                                authenticatedUserId
                        )
        ).thenReturn(
                List.of(workforceMember)
        );

        when(
                teacherProfiles
                        .findByTenantIdAndWorkforceMemberId(
                                tenantId,
                                workforceMemberId
                        )
        ).thenReturn(
                Optional.of(teacher)
        );

        assertSame(
                teacher,
                service.requireTeacherProfileForUser(
                        authenticatedUserId
                )
        );
    }

    @Test
    void userWithoutWorkforceIdentityIsDenied() {

        stubTenant();

        when(
                workforceMembers
                        .findAllByTenantIdAndEiamUserId(
                                tenantId,
                                authenticatedUserId
                        )
        ).thenReturn(
                List.of()
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.requireTeacherProfileForUser(
                                authenticatedUserId
                        )
        );

        verifyNoInteractions(
                teacherProfiles
        );
    }

    @Test
    void inactiveWorkforceMemberIsDenied() {

        stubTenant();

        WorkforceMember workforceMember =
                mock(WorkforceMember.class);

        when(
                workforceMember.getWorkforceStatus()
        ).thenReturn(
                "INACTIVE"
        );

        when(
                workforceMembers
                        .findAllByTenantIdAndEiamUserId(
                                tenantId,
                                authenticatedUserId
                        )
        ).thenReturn(
                List.of(workforceMember)
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.requireTeacherProfileForUser(
                                authenticatedUserId
                        )
        );

        verifyNoInteractions(
                teacherProfiles
        );
    }

    @Test
    void inactiveTeacherProfileIsDenied() {

        stubTenant();

        WorkforceMember workforceMember =
                activeWorkforceMember();

        TeacherProfile teacher =
                mock(TeacherProfile.class);

        when(
                teacher.getTeachingStatus()
        ).thenReturn(
                "INACTIVE"
        );

        when(
                workforceMembers
                        .findAllByTenantIdAndEiamUserId(
                                tenantId,
                                authenticatedUserId
                        )
        ).thenReturn(
                List.of(workforceMember)
        );

        when(
                teacherProfiles
                        .findByTenantIdAndWorkforceMemberId(
                                tenantId,
                                workforceMemberId
                        )
        ).thenReturn(
                Optional.of(teacher)
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.requireTeacherProfileForUser(
                                authenticatedUserId
                        )
        );
    }

    @Test
    void currentTeacherResolutionUsesAuthenticatedUser() {

        when(
                identity.requireUserId()
        ).thenReturn(
                authenticatedUserId
        );

        stubTenant();

        WorkforceMember workforceMember =
                activeWorkforceMember();

        TeacherProfile teacher =
                activeTeacherProfile();

        when(
                workforceMembers
                        .findAllByTenantIdAndEiamUserId(
                                tenantId,
                                authenticatedUserId
                        )
        ).thenReturn(
                List.of(workforceMember)
        );

        when(
                teacherProfiles
                        .findByTenantIdAndWorkforceMemberId(
                                tenantId,
                                workforceMemberId
                        )
        ).thenReturn(
                Optional.of(teacher)
        );

        assertSame(
                teacher,
                service.requireCurrentTeacherProfile()
        );

        verify(
                identity
        ).requireUserId();
    }

    private void stubTenant() {

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenantId
        );
    }

    private WorkforceMember activeWorkforceMember() {

        WorkforceMember member =
                mock(WorkforceMember.class);

        when(
                member.getId()
        ).thenReturn(
                workforceMemberId
        );

        when(
                member.getWorkforceStatus()
        ).thenReturn(
                "ACTIVE"
        );

        when(
                member.getStatus()
        ).thenReturn(
                EntityStatus.ACTIVE
        );

        return member;
    }

    private TeacherProfile activeTeacherProfile() {

        TeacherProfile teacher =
                mock(TeacherProfile.class);

        when(
                teacher.getTeachingStatus()
        ).thenReturn(
                "ACTIVE"
        );

        when(
                teacher.getStatus()
        ).thenReturn(
                EntityStatus.ACTIVE
        );

        return teacher;
    }
}
