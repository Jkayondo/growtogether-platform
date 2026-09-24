import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import LeadershipOverview, {
  attendanceRegisterStatus
} from "./LeadershipOverview";
import { getLeadershipOverview } from "../../services/leadershipApi";

vi.mock("../../services/leadershipApi", () => ({
  getLeadershipOverview: vi.fn()
}));

describe("LeadershipOverview", () => {
  beforeEach(() => {
    vi.mocked(getLeadershipOverview).mockResolvedValue({
      tenantId: "11111111-1111-1111-1111-111111111111",
      asOf: "2026-09-23T06:00:00Z",
      eventWindowEnd: "2026-10-23T06:00:00Z",
      activeVisitors: 3,
      upcomingEvents: 5,
      coverage: {
        totalItems: 18,
        notStarted: 4,
        inProgress: 5,
        completed: 6,
        requiresRemediation: 2,
        aheadOfSchedule: 1
      },
      parentEngagement: {
        totalNotifications: 30,
        deliveredNotifications: 26,
        viewedNotifications: 18,
        acknowledgedNotifications: 9
      },
      learners: {
        activeLearnerRecords: 320,
        activeEnrollments: 306
      },
      teachers: {
        activeTeacherProfiles: 27,
        activeTeachingAssignments: 61
      },
      attendance: {
        attendanceDate: "2026-09-24",
        sessionType: "DAILY_REGISTER",
        sessionCount: 4,
        expectedStudentCount: 100,
        recordedAttendanceCount: 93,
        unrecordedCount: 7,
        presentCount: 85,
        absentCount: 5,
        lateCount: 3,
        excusedAbsenceCount: 2,
        unexcusedAbsenceCount: 3,
        medicalAbsenceCount: 0,
        schoolActivityCount: 0,
        remoteLearningCount: 0,
        earlyDepartureCount: 0,
        suspendedCount: 0,
        notRequiredCount: 0,
        unknownCount: 0,
        registerStarted: true,
        fullyRecorded: false
      },
      capabilities: [
        {
          code: "LEARNERS",
          status: "AVAILABLE",
          source: "StudentRepository + StudentEnrollmentRepository"
        },
        {
          code: "TEACHERS",
          status: "AVAILABLE",
          source: "TeacherProfileRepository + TeachingAssignmentRepository"
        },
        {
          code: "ATTENDANCE",
          status: "AVAILABLE",
          source: "AttendanceDailySummaryService"
        },
        {
          code: "SAFETY_VISITORS",
          status: "AVAILABLE",
          source: "VisitorCheckInService"
        },
        {
          code: "CURRICULUM_COVERAGE",
          status: "AVAILABLE",
          source: "TeacherCoverageRepository"
        },
        {
          code: "PARENT_ENGAGEMENT",
          status: "AVAILABLE",
          source: "ParentEngagementDashboardService"
        },
        {
          code: "FINANCE",
          status: "PENDING_AGGREGATION",
          source: "Finance authoritative services"
        }
      ]
    });
  });

  it("shows only authoritative live values and marks pending integrations", async () => {
    render(<LeadershipOverview />);

    expect(
      await screen.findByText("What requires attention right now?")
    ).toBeTruthy();

    const activeVisitorsCard =
      screen.getByText("Active visitors").closest("article");

    expect(activeVisitorsCard).toBeTruthy();
    expect(activeVisitorsCard?.textContent).toContain("3");

    const upcomingEventsCard =
      screen.getByText("Upcoming events").closest("article");

    expect(upcomingEventsCard).toBeTruthy();
    expect(upcomingEventsCard?.textContent).toContain("5");

    const activeLearnersCard =
      screen.getByText("Active learner records").closest("article");

    expect(activeLearnersCard).toBeTruthy();
    expect(activeLearnersCard?.textContent).toContain("320");

    const activeEnrollmentsCard =
      screen.getByText("Active enrolments").closest("article");

    expect(activeEnrollmentsCard).toBeTruthy();
    expect(activeEnrollmentsCard?.textContent).toContain("306");

    const activeTeachersCard =
      screen.getByText("Active teachers").closest("article");

    expect(activeTeachersCard).toBeTruthy();
    expect(activeTeachersCard?.textContent).toContain("27");

    const activeAssignmentsCard =
      screen.getByText("Active teaching assignments").closest("article");

    expect(activeAssignmentsCard).toBeTruthy();
    expect(activeAssignmentsCard?.textContent).toContain("61");

    expect(
      screen.getByText(/FINANCE/)
    ).toBeTruthy();

    const pendingIntegrations =
      screen.getAllByText(/integration pending/);

    expect(pendingIntegrations).toHaveLength(1);

    const attendanceRegisterCard =
      screen.getByText("Attendance register").closest("article");

    expect(attendanceRegisterCard).toBeTruthy();
    expect(attendanceRegisterCard?.textContent)
      .toContain("Recording in progress");

    const attendanceRecordedCard =
      screen.getByText("Attendance recorded").closest("article");

    expect(attendanceRecordedCard).toBeTruthy();
    expect(attendanceRecordedCard?.textContent).toContain("93");
    expect(attendanceRecordedCard?.textContent).toContain("100");
    expect(attendanceRecordedCard?.textContent).toContain("7");

    expect(
      screen.getByText("Today's attendance")
    ).toBeTruthy();

    const presentRow =
      screen.getByText("Present").closest("div");

    expect(presentRow?.textContent).toContain("85");

    const absentRow =
      screen.getByText("Absent").closest("div");

    expect(absentRow?.textContent).toContain("5");

    expect(
      screen.getByText("FINANCE")
    ).toBeTruthy();
  });


  it("classifies all Attendance register states without deriving a rate", () => {
    expect(
      attendanceRegisterStatus({
        registerStarted: false,
        fullyRecorded: false
      })
    ).toBe("Register not started");

    expect(
      attendanceRegisterStatus({
        registerStarted: true,
        fullyRecorded: false
      })
    ).toBe("Recording in progress");

    expect(
      attendanceRegisterStatus({
        registerStarted: true,
        fullyRecorded: true
      })
    ).toBe("Fully recorded");
  });
});
