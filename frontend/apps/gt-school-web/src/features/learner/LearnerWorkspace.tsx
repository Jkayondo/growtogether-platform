import {
  useEffect,
  useState
} from "react";

import {
  getLearnerWorkspace
} from "../../services/learnerWorkspaceApi";

import type {
  LearnerWorkspaceView
} from "../../types/learnerWorkspace";

function displayValue(
  value: string | null
) {
  return value?.trim()
    ? value
    : "Not yet available";
}

function displaySupport(
  value: boolean | null
) {
  if (value === null) {
    return "Not yet available";
  }

  return value
    ? "Support available"
    : "No additional support indicated";
}

function displayUpdatedAt(
  value: string | null
) {
  if (!value) {
    return "Not yet available";
  }

  const parsed =
    new Date(value);

  if (
    Number.isNaN(
      parsed.getTime()
    )
  ) {
    return value;
  }

  return parsed.toLocaleString();
}

export default function LearnerWorkspace() {
  const [
    workspace,
    setWorkspace
  ] =
    useState<LearnerWorkspaceView | null>(
      null
    );

  const [
    loading,
    setLoading
  ] =
    useState(true);

  const [
    error,
    setError
  ] =
    useState<string | null>(
      null
    );

  useEffect(
    () => {
      let active = true;

      async function load() {
        try {
          const data =
            await getLearnerWorkspace();

          if (active) {
            setWorkspace(data);
          }
        }
        catch {
          if (active) {
            setError(
              "Unable to load your learner workspace."
            );
          }
        }
        finally {
          if (active) {
            setLoading(false);
          }
        }
      }

      void load();

      return () => {
        active = false;
      };
    },
    []
  );

  if (loading) {
    return (
      <div
        className="gt-dashboard"
        data-testid="LearnerWorkspace"
      >
        <div
          className="gt-card"
          role="status"
        >
          Loading your learner workspace...
        </div>
      </div>
    );
  }

  if (
    error
    || !workspace
  ) {
    return (
      <div
        className="gt-dashboard"
        data-testid="LearnerWorkspace"
      >
        <div
          className="gt-card"
          role="alert"
        >
          {
            error
            ?? "Your learner workspace is unavailable."
          }
        </div>
      </div>
    );
  }

  return (
    <main
      className="gt-dashboard"
      data-testid="LearnerWorkspace"
    >
      <section className="gt-card">
        <h1>My Learning</h1>

        <p>
          Your personal GT School learning
          workspace.
        </p>
      </section>

      <section
        className="gt-card"
        aria-labelledby="learner-progress-heading"
      >
        <h2 id="learner-progress-heading">
          My Progress
        </h2>

        <dl>
          <div>
            <dt>Achievement status</dt>
            <dd>
              {
                displayValue(
                  workspace.achievementStatus
                )
              }
            </dd>
          </div>

          <div>
            <dt>Growth level</dt>
            <dd>
              {
                displayValue(
                  workspace.growthLevel
                )
              }
            </dd>
          </div>

          <div>
            <dt>Learning support</dt>
            <dd>
              {
                displaySupport(
                  workspace.supportRequired
                )
              }
            </dd>
          </div>
        </dl>
      </section>

      <section
        className="gt-card"
        aria-labelledby="learner-guidance-heading"
      >
        <h2 id="learner-guidance-heading">
          Learning Guidance
        </h2>

        <p>
          {
            displayValue(
              workspace.growthMessage
            )
          }
        </p>

        <p>
          <small>
            Updated:{" "}
            {
              displayUpdatedAt(
                workspace.updatedAt
              )
            }
          </small>
        </p>
      </section>
    </main>
  );
}
