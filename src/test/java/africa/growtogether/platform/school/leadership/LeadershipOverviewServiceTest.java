package africa.growtogether.platform.school.leadership;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.school.academic.calendar.AcademicCalendarEventService;
import africa.growtogether.platform.school.academic.coverage.TeacherCoverage;
import africa.growtogether.platform.school.academic.coverage.TeacherCoverageRepository;
import africa.growtogether.platform.school.parent.dashboard.ParentEngagementDashboard;
import africa.growtogether.platform.school.parent.dashboard.ParentEngagementDashboardService;
import africa.growtogether.platform.school.student.Student;
import africa.growtogether.platform.school.student.StudentRepository;
import africa.growtogether.platform.school.enrollment.StudentEnrollment;
import africa.growtogether.platform.school.enrollment.StudentEnrollmentRepository;
import africa.growtogether.platform.school.academic.teaching.TeacherProfile;
import africa.growtogether.platform.school.academic.teaching.TeacherProfileRepository;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignment;
import africa.growtogether.platform.school.academic.teaching.TeachingAssignmentRepository;
import africa.growtogether.platform.common.persistence.EntityStatus;
import africa.growtogether.platform.school.visitor.service.VisitorCheckInService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeadershipOverviewServiceTest {

    @Test
    void overviewIsTenantCheckedAndDoesNotInventUnavailableDomainValues() {
        EnterpriseIdentityContext identity =
                mock(EnterpriseIdentityContext.class);
        VisitorCheckInService visitors =
                mock(VisitorCheckInService.class);
        AcademicCalendarEventService calendar =
                mock(AcademicCalendarEventService.class);

        TeacherCoverageRepository coverage =
                mock(TeacherCoverageRepository.class);

        ParentEngagementDashboardService parentEngagement =
                mock(ParentEngagementDashboardService.class);

        StudentRepository students =
                mock(StudentRepository.class);

        StudentEnrollmentRepository enrollments =
                mock(StudentEnrollmentRepository.class);

        TeacherProfileRepository teacherProfiles =
                mock(TeacherProfileRepository.class);

        TeachingAssignmentRepository teachingAssignments =
                mock(TeachingAssignmentRepository.class);

        UUID tenantId = UUID.randomUUID();

        when(visitors.activeVisitors(tenantId))
                .thenReturn(List.of());

        when(calendar.findUpcomingEvents(any(), any()))
                .thenReturn(List.of());

        when(
                coverage.findByTenantIdAndCoverageStatus(
                        tenantId,
                        "NOT_STARTED"
                )
        ).thenReturn(
                List.of(mock(TeacherCoverage.class))
        );

        when(
                coverage.findByTenantIdAndCoverageStatus(
                        tenantId,
                        "IN_PROGRESS"
                )
        ).thenReturn(
                List.of(mock(TeacherCoverage.class))
        );

        when(
                coverage.findByTenantIdAndCoverageStatus(
                        tenantId,
                        "COMPLETED"
                )
        ).thenReturn(
                List.of(
                        mock(TeacherCoverage.class),
                        mock(TeacherCoverage.class)
                )
        );

        when(
                coverage.findByTenantIdAndCoverageStatus(
                        tenantId,
                        "REQUIRES_REMEDIATION"
                )
        ).thenReturn(
                List.of(
                        mock(TeacherCoverage.class),
                        mock(TeacherCoverage.class)
                )
        );

        when(
                coverage.findByTenantIdAndCoverageStatus(
                        tenantId,
                        "AHEAD_OF_SCHEDULE"
                )
        ).thenReturn(
                List.of(mock(TeacherCoverage.class))
        );

        when(parentEngagement.loadDashboard(tenantId))
                .thenReturn(
                        new ParentEngagementDashboard(
                                20,
                                18,
                                12,
                                7
                        )
                );

        Student activeStudent1 = mock(Student.class);
        Student activeStudent2 = mock(Student.class);
        Student activeStudent3 = mock(Student.class);

        when(
                students.findByTenantIdAndStatus(
                        tenantId,
                        EntityStatus.ACTIVE
                )
        ).thenReturn(
                List.of(
                        activeStudent1,
                        activeStudent2,
                        activeStudent3
                )
        );

        StudentEnrollment activeEnrollment1 =
                mock(StudentEnrollment.class);

        StudentEnrollment activeEnrollment2 =
                mock(StudentEnrollment.class);

        StudentEnrollment inactiveEnrollment =
                mock(StudentEnrollment.class);

        when(activeEnrollment1.getStatus())
                .thenReturn(EntityStatus.ACTIVE);

        when(activeEnrollment2.getStatus())
                .thenReturn(EntityStatus.ACTIVE);

        when(inactiveEnrollment.getStatus())
                .thenReturn(EntityStatus.INACTIVE);

        when(
                enrollments.findByTenantIdAndEnrollmentStatus(
                        tenantId,
                        "ACTIVE"
                )
        ).thenReturn(
                List.of(
                        activeEnrollment1,
                        activeEnrollment2,
                        inactiveEnrollment
                )
        );

        TeacherProfile activeTeacher1 =
                mock(TeacherProfile.class);

        TeacherProfile activeTeacher2 =
                mock(TeacherProfile.class);

        TeacherProfile inactiveTeacher =
                mock(TeacherProfile.class);

        when(activeTeacher1.getStatus())
                .thenReturn(EntityStatus.ACTIVE);

        when(activeTeacher2.getStatus())
                .thenReturn(EntityStatus.ACTIVE);

        when(inactiveTeacher.getStatus())
                .thenReturn(EntityStatus.INACTIVE);

        when(
                teacherProfiles.findByTenantIdAndTeachingStatus(
                        tenantId,
                        "ACTIVE"
                )
        ).thenReturn(
                List.of(
                        activeTeacher1,
                        activeTeacher2,
                        inactiveTeacher
                )
        );

        TeachingAssignment activeAssignment1 =
                mock(TeachingAssignment.class);

        TeachingAssignment activeAssignment2 =
                mock(TeachingAssignment.class);

        TeachingAssignment inactiveAssignment =
                mock(TeachingAssignment.class);

        when(activeAssignment1.getStatus())
                .thenReturn(EntityStatus.ACTIVE);

        when(activeAssignment2.getStatus())
                .thenReturn(EntityStatus.ACTIVE);

        when(inactiveAssignment.getStatus())
                .thenReturn(EntityStatus.INACTIVE);

        when(
                teachingAssignments
                        .findByTenantIdAndAssignmentStatus(
                                tenantId,
                                "ACTIVE"
                        )
        ).thenReturn(
                List.of(
                        activeAssignment1,
                        activeAssignment2,
                        inactiveAssignment
                )
        );

        LeadershipOverviewService service =
                new LeadershipOverviewService(
                        identity,
                        visitors,
                        calendar,
                        coverage,
                        parentEngagement,
                        students,
                        enrollments,
                        teacherProfiles,
                        teachingAssignments
                );

        LeadershipOverviewResponse response =
                service.overview(tenantId);

        verify(identity).requireTenant(tenantId);

        assertEquals(tenantId, response.tenantId());
        assertEquals(0, response.activeVisitors());
        assertEquals(0, response.upcomingEvents());

        assertEquals(7, response.coverage().totalItems());
        assertEquals(
                2,
                response.coverage().requiresRemediation()
        );
        assertEquals(
                2,
                response.coverage().completed()
        );

        assertEquals(
                20,
                response.parentEngagement().totalNotifications()
        );
        assertEquals(
                7,
                response.parentEngagement()
                        .acknowledgedNotifications()
        );

        assertEquals(
                3,
                response.learners().activeLearnerRecords()
        );

        assertEquals(
                2,
                response.learners().activeEnrollments()
        );

        assertEquals(
                2,
                response.teachers().activeTeacherProfiles()
        );

        assertEquals(
                2,
                response.teachers().activeTeachingAssignments()
        );

        assertTrue(
                response.capabilities().stream().anyMatch(
                        capability ->
                                capability.code().equals("LEARNERS")
                                        && capability.status()
                                        .equals("AVAILABLE")
                )
        );

        assertTrue(
                response.capabilities().stream().anyMatch(
                        capability ->
                                capability.code().equals("TEACHERS")
                                        && capability.status()
                                        .equals("AVAILABLE")
                )
        );

        assertTrue(
                response.capabilities().stream().anyMatch(
                        capability ->
                                capability.code().equals("ATTENDANCE")
                                        && capability.status()
                                        .equals("PENDING_AGGREGATION")
                )
        );

        assertTrue(
                response.capabilities().stream().anyMatch(
                        capability ->
                                capability.code().equals("FINANCE")
                                        && capability.status()
                                        .equals("PENDING_AGGREGATION")
                )
        );

        assertTrue(
                response.capabilities().stream().anyMatch(
                        capability ->
                                capability.code()
                                        .equals("CURRICULUM_COVERAGE")
                                        && capability.status()
                                        .equals("AVAILABLE")
                )
        );

        assertTrue(
                response.capabilities().stream().anyMatch(
                        capability ->
                                capability.code()
                                        .equals("PARENT_ENGAGEMENT")
                                        && capability.status()
                                        .equals("AVAILABLE")
                )
        );

        assertTrue(
                response.capabilities().stream().anyMatch(
                        capability ->
                                capability.code().equals("SAFETY_VISITORS")
                                        && capability.status()
                                        .equals("AVAILABLE")
                )
        );
    }
}
