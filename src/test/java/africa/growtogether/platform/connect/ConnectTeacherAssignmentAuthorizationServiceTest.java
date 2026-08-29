package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;

import africa.growtogether.platform.school.academic.teaching.TeacherProfile;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignment;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignmentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConnectTeacherAssignmentAuthorizationServiceTest {

    @Mock
    private EnterpriseIdentityContext identity;

    @Mock
    private ConnectTeacherAuthorizationService teacherAuthorization;

    @Mock
    private TeachingAssignmentRepository assignments;

    private ConnectTeacherAssignmentAuthorizationService service;

    private UUID tenantId;
    private UUID authenticatedUserId;
    private UUID teacherProfileId;
    private UUID streamId;

    @BeforeEach
    void setUp() {

        service =
                new ConnectTeacherAssignmentAuthorizationService(
                        identity,
                        teacherAuthorization,
                        assignments
                );

        tenantId = UUID.randomUUID();
        authenticatedUserId = UUID.randomUUID();
        teacherProfileId = UUID.randomUUID();
        streamId = UUID.randomUUID();
    }

    @Test
    void activeEffectiveStreamAssignmentAllowsAccess() {

        stubCurrentTeacher();

        TeachingAssignment assignment =
                assignment(
                        streamId,
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(1),
                        true
                );

        when(
                assignments.findByTenantIdAndTeacherProfileId(
                        tenantId,
                        teacherProfileId
                )
        ).thenReturn(
                List.of(assignment)
        );

        assertSame(
                assignment,
                service.requireCurrentTeacherCanAccessStream(
                        streamId
                )
        );
    }

    @Test
    void plannedAssignmentIsDenied() {

        stubCurrentTeacher();

        TeachingAssignment assignment =
                assignment(
                        streamId,
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(1),
                        false
                );

        when(
                assignments.findByTenantIdAndTeacherProfileId(
                        tenantId,
                        teacherProfileId
                )
        ).thenReturn(
                List.of(assignment)
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.requireCurrentTeacherCanAccessStream(
                                streamId
                        )
        );
    }

    @Test
    void futureAssignmentIsDenied() {

        stubCurrentTeacher();

        TeachingAssignment assignment =
                assignment(
                        streamId,
                        LocalDate.now().plusDays(1),
                        LocalDate.now().plusDays(30),
                        true
                );

        when(
                assignments.findByTenantIdAndTeacherProfileId(
                        tenantId,
                        teacherProfileId
                )
        ).thenReturn(
                List.of(assignment)
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.requireCurrentTeacherCanAccessStream(
                                streamId
                        )
        );
    }

    @Test
    void expiredAssignmentIsDenied() {

        stubCurrentTeacher();

        TeachingAssignment assignment =
                assignment(
                        streamId,
                        LocalDate.now().minusDays(30),
                        LocalDate.now().minusDays(1),
                        true
                );

        when(
                assignments.findByTenantIdAndTeacherProfileId(
                        tenantId,
                        teacherProfileId
                )
        ).thenReturn(
                List.of(assignment)
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.requireCurrentTeacherCanAccessStream(
                                streamId
                        )
        );
    }

    @Test
    void assignmentToDifferentStreamIsDenied() {

        stubCurrentTeacher();

        TeachingAssignment assignment =
                assignment(
                        UUID.randomUUID(),
                        LocalDate.now().minusDays(1),
                        LocalDate.now().plusDays(1),
                        true
                );

        when(
                assignments.findByTenantIdAndTeacherProfileId(
                        tenantId,
                        teacherProfileId
                )
        ).thenReturn(
                List.of(assignment)
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.requireCurrentTeacherCanAccessStream(
                                streamId
                        )
        );
    }

    private void stubCurrentTeacher() {

        when(
                identity.requireUserId()
        ).thenReturn(
                authenticatedUserId
        );

        when(
                identity.requireTenantId()
        ).thenReturn(
                tenantId
        );

        TeacherProfile teacher =
                mock(TeacherProfile.class);

        when(
                teacher.getId()
        ).thenReturn(
                teacherProfileId
        );

        when(
                teacherAuthorization
                        .requireTeacherProfileForUser(
                                authenticatedUserId
                        )
        ).thenReturn(
                teacher
        );
    }

    private TeachingAssignment assignment(
            UUID assignmentStreamId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            boolean active
    ) {

        TeachingAssignment assignment =
                new TeachingAssignment(
                        "TA-" + UUID.randomUUID(),
                        teacherProfileId,
                        null,
                        UUID.randomUUID(),
                        null,
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        assignmentStreamId,
                        UUID.randomUUID(),
                        "PRIMARY_TEACHER",
                        5,
                        null,
                        effectiveFrom,
                        effectiveTo,
                        null,
                        null,
                        null
                );

        assignment.setTenantId(
                tenantId
        );

        if (active) {
            assignment.activate(
                    UUID.randomUUID()
            );
        }

        return assignment;
    }
}
