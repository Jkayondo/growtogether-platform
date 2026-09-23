import { useEffect, useState } from "react";
import { getLeadershipOverview } from "../../services/leadershipApi";
import type {
  LeadershipOverviewData
} from "../../types/leadership";

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
