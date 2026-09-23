package africa.growtogether.platform.school.leadership;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.school.academic.calendar.AcademicCalendarEventService;
import africa.growtogether.platform.school.academic.coverage.TeacherCoverage;
import africa.growtogether.platform.school.academic.coverage.TeacherCoverageRepository;
import africa.growtogether.platform.school.parent.dashboard.ParentEngagementDashboard;
import africa.growtogether.platform.school.parent.dashboard.ParentEngagementDashboardService;
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

        LeadershipOverviewService service =
                new LeadershipOverviewService(
                        identity,
                        visitors,
                        calendar,
                        coverage,
                        parentEngagement
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
