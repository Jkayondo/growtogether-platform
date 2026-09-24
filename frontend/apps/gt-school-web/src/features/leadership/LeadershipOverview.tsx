import { useEffect, useState } from "react";
import { getLeadershipOverview } from "../../services/leadershipApi";
import type {
  LeadershipAttendanceSummary,
  LeadershipOverviewData
} from "../../types/leadership";

export function attendanceRegisterStatus(
  attendance: Pick<
    LeadershipAttendanceSummary,
    "registerStarted" | "fullyRecorded"
  >
): string {
  if (!attendance.registerStarted) {
    return "Register not started";
  }

  if (attendance.fullyRecorded) {
    return "Fully recorded";
  }

  return "Recording in progress";
}

export default function LeadershipOverview() {
  const [overview, setOverview] =
    useState<LeadershipOverviewData | null>(null);

  const [error, setError] =
    useState<string | null>(null);

  useEffect(() => {
    let active = true;

    getLeadershipOverview()
      .then((data) => {
        if (active) {
          setOverview(data);
        }
      })
      .catch(() => {
        if (active) {
          setError(
            "Leadership overview could not be loaded."
          );
        }
      });

    return () => {
      active = false;
    };
  }, []);

  if (error) {
    return (
      <main className="gt-dashboard leadership-overview">
        <h1>Leadership</h1>
        <p role="alert">{error}</p>
      </main>
    );
  }

  if (!overview) {
    return (
      <main className="gt-dashboard leadership-overview">
        <h1>Leadership</h1>
        <p>Loading leadership overview...</p>
      </main>
    );
  }

  const pending =
    overview.capabilities.filter(
      (capability) =>
        capability.status === "PENDING_AGGREGATION"
    );

  const attendanceState =
    attendanceRegisterStatus(overview.attendance);

  return (
    <main className="gt-dashboard leadership-overview">
      <header>
        <p>School leadership overview</p>
        <h1>What requires attention right now?</h1>
        <p>
          Data as of{" "}
          <time dateTime={overview.asOf}>
            {new Date(overview.asOf).toLocaleString()}
          </time>
        </p>
      </header>

      <section
        className="gt-dashboard-summary-grid"
        aria-label="Available operational indicators"
      >
        <article className="gt-card">
          <p>Active visitors</p>
          <strong>{overview.activeVisitors}</strong>
          <small>Source: Visitor operations</small>
        </article>

        <article className="gt-card">
          <p>Upcoming events</p>
          <strong>{overview.upcomingEvents}</strong>
          <small>Next 30 days</small>
        </article>

        <article className="gt-card">
          <p>Coverage requiring remediation</p>
          <strong>
            {overview.coverage.requiresRemediation}
          </strong>
          <small>
            {overview.coverage.totalItems} tracked coverage items
          </small>
        </article>

        <article className="gt-card">
          <p>Parent acknowledgements</p>
          <strong>
            {overview.parentEngagement.acknowledgedNotifications}
          </strong>
          <small>
            {overview.parentEngagement.totalNotifications}
            {" "}engagement events
          </small>
        </article>

                <article className="gt-card">
          <p>Active learner records</p>
          <strong>{overview.learners.activeLearnerRecords}</strong>
          <small>Active Student records</small>
        </article>

        <article className="gt-card">
          <p>Active enrolments</p>
          <strong>{overview.learners.activeEnrollments}</strong>
          <small>Active current enrolment records</small>
        </article>

        <article className="gt-card">
          <p>Active teachers</p>
          <strong>{overview.teachers.activeTeacherProfiles}</strong>
          <small>Active teacher profiles</small>
        </article>

        <article className="gt-card">
          <p>Active teaching assignments</p>
          <strong>{overview.teachers.activeTeachingAssignments}</strong>
          <small>Assignment status: ACTIVE</small>
        </article>

<article className="gt-card">
          <p>Attendance register</p>
          <strong>{attendanceState}</strong>
          <small>
            {overview.attendance.attendanceDate}
            {" · "}
            {overview.attendance.sessionType}
          </small>
        </article>

        <article className="gt-card">
          <p>Attendance recorded</p>
          <strong>
            {overview.attendance.recordedAttendanceCount}
          </strong>
          <small>
            {overview.attendance.expectedStudentCount}
            {" "}expected ·
            {" "}
            {overview.attendance.unrecordedCount}
            {" "}unrecorded
          </small>
        </article>

        <article className="gt-card">
          <p>Absent today</p>
          <strong>{overview.attendance.absentCount}</strong>
          <small>
            {overview.attendance.lateCount}
            {" "}late
          </small>
        </article>

        <article className="gt-card">
          <p>Integration status</p>
          <strong>
            {
              overview.capabilities.filter(
                (capability) =>
                  capability.status === "AVAILABLE"
              ).length
            }
          </strong>
          <small>
            authoritative Leadership sources active
          </small>
        </article>
      </section>

      <section aria-labelledby="leadership-attendance-heading">
        <h2 id="leadership-attendance-heading">
          Today's attendance
        </h2>

        <p>
          Authoritative DAILY_REGISTER counts for{" "}
          <time dateTime={overview.attendance.attendanceDate}>
            {overview.attendance.attendanceDate}
          </time>
          .
        </p>

        <dl className="leadership-attendance-counts">
          <div>
            <dt>Present</dt>
            <dd>{overview.attendance.presentCount}</dd>
          </div>
          <div>
            <dt>Absent</dt>
            <dd>{overview.attendance.absentCount}</dd>
          </div>
          <div>
            <dt>Late</dt>
            <dd>{overview.attendance.lateCount}</dd>
          </div>
          <div>
            <dt>Excused absence</dt>
            <dd>{overview.attendance.excusedAbsenceCount}</dd>
          </div>
          <div>
            <dt>Unexcused absence</dt>
            <dd>{overview.attendance.unexcusedAbsenceCount}</dd>
          </div>
          <div>
            <dt>Medical absence</dt>
            <dd>{overview.attendance.medicalAbsenceCount}</dd>
          </div>
          <div>
            <dt>School activity</dt>
            <dd>{overview.attendance.schoolActivityCount}</dd>
          </div>
          <div>
            <dt>Remote learning</dt>
            <dd>{overview.attendance.remoteLearningCount}</dd>
          </div>
          <div>
            <dt>Early departure</dt>
            <dd>{overview.attendance.earlyDepartureCount}</dd>
          </div>
          <div>
            <dt>Suspended</dt>
            <dd>{overview.attendance.suspendedCount}</dd>
          </div>
          <div>
            <dt>Not required</dt>
            <dd>{overview.attendance.notRequiredCount}</dd>
          </div>
          <div>
            <dt>Unknown</dt>
            <dd>{overview.attendance.unknownCount}</dd>
          </div>
        </dl>
      </section>

      <section aria-labelledby="leadership-integration-heading">
        <h2 id="leadership-integration-heading">
          Leadership intelligence integration
        </h2>

        {pending.length === 0 ? (
          <p>All authorised overview sources are available.</p>
        ) : (
          <ul>
            {pending.map((capability) => (
              <li key={capability.code}>
                <strong>{capability.code}</strong>
                {" — "}
                integration pending
                {" · "}
                {capability.source}
              </li>
            ))}
          </ul>
        )}
      </section>
    </main>
  );
}
