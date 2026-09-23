package africa.growtogether.platform.school.leadership;

import africa.growtogether.platform.common.security.EnterpriseIdentityContext;
import africa.growtogether.platform.school.academic.calendar.AcademicCalendarEventService;
import africa.growtogether.platform.school.academic.coverage.TeacherCoverageRepository;
import africa.growtogether.platform.school.parent.dashboard.ParentEngagementDashboard;
import africa.growtogether.platform.school.parent.dashboard.ParentEngagementDashboardService;
import africa.growtogether.platform.school.visitor.service.VisitorCheckInService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class LeadershipOverviewService {

    private static final long UPCOMING_EVENT_WINDOW_DAYS = 30L;

    private final EnterpriseIdentityContext identity;
    private final VisitorCheckInService visitorCheckInService;
    private final AcademicCalendarEventService calendarEventService;
    private final TeacherCoverageRepository coverageRepository;
    private final ParentEngagementDashboardService parentEngagementService;

    public LeadershipOverviewService(
            EnterpriseIdentityContext identity,
            VisitorCheckInService visitorCheckInService,
            AcademicCalendarEventService calendarEventService,
            TeacherCoverageRepository coverageRepository,
            ParentEngagementDashboardService parentEngagementService
    ) {
        this.identity = identity;
        this.visitorCheckInService = visitorCheckInService;
        this.calendarEventService = calendarEventService;
        this.coverageRepository = coverageRepository;
        this.parentEngagementService = parentEngagementService;
    }

    @Transactional(readOnly = true)
    public LeadershipOverviewResponse overview(UUID tenantId) {
        identity.requireTenant(tenantId);

        Instant asOf = Instant.now();
        Instant eventWindowEnd =
                asOf.plus(UPCOMING_EVENT_WINDOW_DAYS, ChronoUnit.DAYS);

        int activeVisitors =
                visitorCheckInService.activeVisitors(tenantId).size();

        int upcomingEvents =
                calendarEventService
                        .findUpcomingEvents(asOf, eventWindowEnd)
                        .size();

        long coverageNotStarted =
                coverageCount(tenantId, "NOT_STARTED");

        long coverageInProgress =
                coverageCount(tenantId, "IN_PROGRESS");

        long coverageCompleted =
                coverageCount(tenantId, "COMPLETED");

        long coverageRequiresRemediation =
                coverageCount(tenantId, "REQUIRES_REMEDIATION");

        long coverageAhead =
                coverageCount(tenantId, "AHEAD_OF_SCHEDULE");

        LeadershipOverviewResponse.CoverageSummary coverage =
                new LeadershipOverviewResponse.CoverageSummary(
                        coverageNotStarted
                                + coverageInProgress
                                + coverageCompleted
                                + coverageRequiresRemediation
                                + coverageAhead,
                        coverageNotStarted,
                        coverageInProgress,
                        coverageCompleted,
                        coverageRequiresRemediation,
                        coverageAhead
                );

        ParentEngagementDashboard parent =
                parentEngagementService.loadDashboard(tenantId);

        LeadershipOverviewResponse.ParentEngagementSummary
                parentEngagement =
                new LeadershipOverviewResponse.ParentEngagementSummary(
                        parent.getTotalNotifications(),
                        parent.getDeliveredNotifications(),
                        parent.getViewedNotifications(),
                        parent.getAcknowledgedNotifications()
                );

        return new LeadershipOverviewResponse(
                tenantId,
                asOf,
                eventWindowEnd,
                activeVisitors,
                upcomingEvents,
                coverage,
                parentEngagement,
                List.of(
                        available(
                                "SAFETY_VISITORS",
                                "VisitorCheckInService"
                        ),
                        available(
                                "EVENTS",
                                "AcademicCalendarEventService"
                        ),
                        pending(
                                "LEARNERS",
                                "Student/Enrollment authoritative services"
                        ),
                        pending(
                                "ATTENDANCE",
                                "Authoritative attendance source reconciliation"
                        ),
                        pending(
                                "TEACHERS",
                                "Teacher/TeachingAssignment authoritative services"
                        ),
                        available(
                                "CURRICULUM_COVERAGE",
                                "TeacherCoverageRepository"
                        ),
                        pending(
                                "ACADEMICS_RESULTS",
                                "Assessment/Results authoritative services"
                        ),
                        pending(
                                "FINANCE",
                                "Finance authoritative services"
                        ),
                        available(
                                "PARENT_ENGAGEMENT",
                                "ParentEngagementDashboardService"
                        ),
                        pending(
                                "ALERTS",
                                "EAP to ENS integration"
                        ),
                        pending(
                                "ACTIONS",
                                "EWE workflow/task integration"
                        )
                )
        );
    }

    private long coverageCount(
            UUID tenantId,
            String status
    ) {
        return coverageRepository
                .findByTenantIdAndCoverageStatus(
                        tenantId,
                        status
                )
                .size();
    }

    private static LeadershipOverviewResponse.CapabilityState available(
            String code,
            String source
    ) {
        return new LeadershipOverviewResponse.CapabilityState(
                code,
                "AVAILABLE",
                source
        );
    }

    private static LeadershipOverviewResponse.CapabilityState pending(
            String code,
            String source
    ) {
        return new LeadershipOverviewResponse.CapabilityState(
                code,
                "PENDING_AGGREGATION",
                source
        );
    }
}
