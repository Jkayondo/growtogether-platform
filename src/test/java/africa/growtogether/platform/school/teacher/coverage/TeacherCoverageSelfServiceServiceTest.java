package africa.growtogether.platform.school.teacher.coverage;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.connect.ConnectTeacherAuthorizationService;
import africa.growtogether.platform.school.academic.coverage.TeacherCoverage;
import africa.growtogether.platform.school.academic.coverage.TeacherCoverageService;
import africa.growtogether.platform.school.academic.teaching.TeacherProfile;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignment;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignmentService;
import africa.growtogether.platform.school.profile.SchoolProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.access.AccessDeniedException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TeacherCoverageSelfServiceServiceTest {

    private static final UUID TENANT_ID =
            UUID.fromString(
                    "10000000-0000-0000-0000-000000000001"
            );

    private static final UUID TEACHER_ID =
            UUID.fromString(
                    "20000000-0000-0000-0000-000000000001"
            );

    private EnterpriseIdentityContext identity;
    private ConnectTeacherAuthorizationService teachers;
    private TeacherCoverageService coverage;
    private TeachingAssignmentService assignments;
    private SchoolProfileService schools;

    private TeacherCoverageSelfServiceService service;

    @BeforeEach
    void setUp() {

        identity =
                mock(
                        EnterpriseIdentityContext.class
                );

        teachers =
                mock(
                        ConnectTeacherAuthorizationService.class
                );

        coverage =
                mock(
                        TeacherCoverageService.class
                );

        assignments =
                mock(
                        TeachingAssignmentService.class
                );

        schools =
                mock(
                        SchoolProfileService.class
                );

        Clock clock =
                Clock.fixed(
                        Instant.parse(
                                "2026-09-20T21:30:00Z"
                        ),
                        ZoneOffset.UTC
                );

        service =
                new TeacherCoverageSelfServiceService(
                        identity,
                        teachers,
                        coverage,
                        assignments,
                        schools,
                        clock
                );

        TeacherProfile teacher =
                mock(
                        TeacherProfile.class
                );

        when(
                identity.requireTenantId()
        ).thenReturn(
                TENANT_ID
        );

        when(
                teachers.requireUniqueCurrentTeacherProfile()
        ).thenReturn(
                teacher
        );

        when(
                teacher.getId()
        ).thenReturn(
                TEACHER_ID
        );
    }

    @Test
    void currentCoverageUsesAuthenticatedIdentityAndBuildsFullSummary() {

        TeacherCoverage notStarted =
                coverageItem(
                        UUID.randomUUID(),
                        TEACHER_ID,
                        UUID.randomUUID(),
                        "NOT_STARTED"
                );

        TeacherCoverage inProgress =
                coverageItem(
                        UUID.randomUUID(),
                        TEACHER_ID,
                        UUID.randomUUID(),
                        "IN_PROGRESS"
                );

        TeacherCoverage completed =
                coverageItem(
                        UUID.randomUUID(),
                        TEACHER_ID,
                        UUID.randomUUID(),
                        "COMPLETED"
                );

        TeacherCoverage remediation =
                coverageItem(
                        UUID.randomUUID(),
                        TEACHER_ID,
                        UUID.randomUUID(),
                        "REQUIRES_REMEDIATION"
                );

        TeacherCoverage ahead =
                coverageItem(
                        UUID.randomUUID(),
                        TEACHER_ID,
                        UUID.randomUUID(),
                        "AHEAD_OF_SCHEDULE"
                );

        when(
                coverage.getTeacherCoverage(
                        TENANT_ID,
                        TEACHER_ID
                )
        ).thenReturn(
                List.of(
                        notStarted,
                        inProgress,
                        completed,
                        remediation,
                        ahead
                )
        );

        TeacherCoverageView view =
                service.currentCoverage();

        assertEquals(
                TEACHER_ID,
                view.teacherProfileId()
        );

        assertNull(
                view.teachingAssignmentId()
        );

        assertEquals(
                5,
                view.summary().total()
        );

        assertEquals(
                1,
                view.summary().notStarted()
        );

        assertEquals(
                1,
                view.summary().inProgress()
        );

        assertEquals(
                1,
                view.summary().completed()
        );

        assertEquals(
                1,
                view.summary().requiresRemediation()
        );

        assertEquals(
                1,
                view.summary().aheadOfSchedule()
        );

        verify(
                coverage
        ).getTeacherCoverage(
                TENANT_ID,
                TEACHER_ID
        );
    }

    @Test
    void assignmentCoverageRejectsAssignmentOwnedByAnotherTeacher() {

        UUID assignmentId =
                UUID.randomUUID();

        TeachingAssignment assignment =
                assignment(
                        UUID.randomUUID(),
                        "ACTIVE"
                );

        when(
                assignments.findById(
                        TENANT_ID,
                        assignmentId
                )
        ).thenReturn(
                assignment
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.assignmentCoverage(
                                assignmentId
                        )
        );

        verify(
                coverage,
                never()
        ).getAssignmentCoverage(
                any(),
                any()
        );
    }

    @Test
    void assignmentCoverageRejectsForeignCoverageInsideOwnedAssignment() {

        UUID assignmentId =
                UUID.randomUUID();

        TeachingAssignment ownedAssignment =
                assignment(
                        TEACHER_ID,
                        "ACTIVE"
                );

        when(
                assignments.findById(
                        TENANT_ID,
                        assignmentId
                )
        ).thenReturn(
                ownedAssignment
        );

        TeacherCoverage foreign =
                coverageItem(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        assignmentId,
                        "NOT_STARTED"
                );

        when(
                coverage.getAssignmentCoverage(
                        TENANT_ID,
                        assignmentId
                )
        ).thenReturn(
                List.of(
                        foreign
                )
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.assignmentCoverage(
                                assignmentId
                        )
        );
    }

    @Test
    void mutationRejectsCoverageNotOwnedByAuthenticatedTeacher() {

        UUID requestedCoverageId =
                UUID.randomUUID();

        TeacherCoverage differentCoverage =
                coverageItem(
                        UUID.randomUUID(),
                        TEACHER_ID,
                        UUID.randomUUID(),
                        "NOT_STARTED"
                );

        when(
                coverage.getTeacherCoverage(
                        TENANT_ID,
                        TEACHER_ID
                )
        ).thenReturn(
                List.of(
                        differentCoverage
                )
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.markInProgress(
                                requestedCoverageId
                        )
        );

        verify(
                assignments,
                never()
        ).findById(
                any(),
                any()
        );

        verify(
                coverage,
                never()
        ).save(
                any()
        );
    }

    @Test
    void inProgressMutationRequiresOwnedActiveAssignment() {

        UUID coverageId =
                UUID.randomUUID();

        UUID assignmentId =
                UUID.randomUUID();

        TeacherCoverage item =
                coverageItem(
                        coverageId,
                        TEACHER_ID,
                        assignmentId,
                        "NOT_STARTED"
                );

        when(
                coverage.getTeacherCoverage(
                        TENANT_ID,
                        TEACHER_ID
                )
        ).thenReturn(
                List.of(
                        item
                )
        );

        TeachingAssignment ownedAssignment =
                assignment(
                        TEACHER_ID,
                        "ACTIVE"
                );

        when(
                assignments.findById(
                        TENANT_ID,
                        assignmentId
                )
        ).thenReturn(
                ownedAssignment
        );

        when(
                coverage.save(
                        item
                )
        ).thenReturn(
                item
        );

        service.markInProgress(
                coverageId
        );

        verify(
                item
        ).markInProgress();

        verify(
                coverage
        ).save(
                item
        );
    }

    @Test
    void suspendedAssignmentBlocksCoverageMutation() {

        UUID coverageId =
                UUID.randomUUID();

        UUID assignmentId =
                UUID.randomUUID();

        TeacherCoverage item =
                coverageItem(
                        coverageId,
                        TEACHER_ID,
                        assignmentId,
                        "NOT_STARTED"
                );

        when(
                coverage.getTeacherCoverage(
                        TENANT_ID,
                        TEACHER_ID
                )
        ).thenReturn(
                List.of(
                        item
                )
        );

        TeachingAssignment suspendedAssignment =
                assignment(
                        TEACHER_ID,
                        "SUSPENDED"
                );

        when(
                assignments.findById(
                        TENANT_ID,
                        assignmentId
                )
        ).thenReturn(
                suspendedAssignment
        );

        assertThrows(
                AccessDeniedException.class,
                () ->
                        service.markInProgress(
                                coverageId
                        )
        );

        verify(
                item,
                never()
        ).markInProgress();

        verify(
                coverage,
                never()
        ).save(
                any()
        );
    }

    @Test
    void completionUsesSchoolLocalDateAndPreservesRemarks() {

        UUID coverageId =
                UUID.randomUUID();

        UUID assignmentId =
                UUID.randomUUID();

        TeacherCoverage item =
                coverageItem(
                        coverageId,
                        TEACHER_ID,
                        assignmentId,
                        "IN_PROGRESS"
                );

        when(
                coverage.getTeacherCoverage(
                        TENANT_ID,
                        TEACHER_ID
                )
        ).thenReturn(
                List.of(
                        item
                )
        );

        TeachingAssignment ownedAssignment =
                assignment(
                        TEACHER_ID,
                        "ACTIVE"
                );

        when(
                assignments.findById(
                        TENANT_ID,
                        assignmentId
                )
        ).thenReturn(
                ownedAssignment
        );

        when(
                schools.requireTimezone(
                        TENANT_ID
                )
        ).thenReturn(
                ZoneId.of(
                        "Africa/Kampala"
                )
        );

        when(
                coverage.save(
                        item
                )
        ).thenReturn(
                item
        );

        service.markCompleted(
                coverageId,
                "Topic completed"
        );

        verify(
                item
        ).markCompleted(
                LocalDate.of(
                        2026,
                        9,
                        21
                ),
                "Topic completed"
        );

        verify(
                coverage
        ).save(
                item
        );
    }

    private TeacherCoverage coverageItem(
            UUID id,
            UUID teacherId,
            UUID assignmentId,
            String status
    ) {

        TeacherCoverage item =
                mock(
                        TeacherCoverage.class
                );

        when(
                item.getId()
        ).thenReturn(
                id
        );

        when(
                item.getTeacherProfileId()
        ).thenReturn(
                teacherId
        );

        when(
                item.getTeachingAssignmentId()
        ).thenReturn(
                assignmentId
        );

        when(
                item.getCoverageStatus()
        ).thenReturn(
                status
        );

        return item;
    }

    private TeachingAssignment assignment(
            UUID teacherId,
            String status
    ) {

        TeachingAssignment assignment =
                mock(
                        TeachingAssignment.class
                );

        when(
                assignment.getTeacherProfileId()
        ).thenReturn(
                teacherId
        );

        when(
                assignment.getAssignmentStatus()
        ).thenReturn(
                status
        );

        return assignment;
    }
}
