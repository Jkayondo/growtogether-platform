import { render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import LeadershipOverview from "./LeadershipOverview";
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
      capabilities: [
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

    expect(
      screen.getByText(/FINANCE/)
    ).toBeTruthy();

    expect(
      screen.getByText(/integration pending/)
    ).toBeTruthy();
  });
});
