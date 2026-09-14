import {
  useEffect,
  useMemo,
  useState
} from "react";

import type {
  FormEvent
} from "react";

import {
  loadTeacherAiDisplayContexts
} from "../../services/teacherAiDisplayContextService";

import {
  runTeacherAiRequest
} from "../../services/teacherAiWorkflowService";

import type {
  TeacherAiDisplayContext,
  TeacherAiRunResult
} from "../../types/teacherAi";

import "./TeacherAiWorkspace.css";

function friendlyError(
  cause: unknown,
  fallback: string
) {
  const message =
    cause instanceof Error
      ? cause.message
      : "";

  if (message === "API Error 401") {
    return "Your session has expired. Please sign in again.";
  }

  if (message === "API Error 403") {
    return "Access denied. Your Teacher AI permissions or active teaching assignment may need attention.";
  }

  return message || fallback;
}

export default function TeacherAiWorkspace() {
  const [
    assignments,
    setAssignments
  ] =
    useState<TeacherAiDisplayContext[]>([]);

  const [
    selectedAssignmentId,
    setSelectedAssignmentId
  ] =
    useState("");

  const [
    modelCode,
    setModelCode
  ] =
    useState("");

  const [
    prompt,
    setPrompt
  ] =
    useState("");

  const [
    loading,
    setLoading
  ] =
    useState(true);

  const [
    running,
    setRunning
  ] =
    useState(false);

  const [
    loadError,
    setLoadError
  ] =
    useState<string | null>(null);

  const [
    contextNotices,
    setContextNotices
  ] =
    useState<string[]>([]);

  const [
    actionError,
    setActionError
  ] =
    useState<string | null>(null);

  const [
    result,
    setResult
  ] =
    useState<TeacherAiRunResult | null>(null);

  useEffect(() => {
    let active = true;

    async function load() {
      setLoading(true);
      setLoadError(null);

      try {
        const display =
          await loadTeacherAiDisplayContexts();

        if (!active) {
          return;
        }

        setAssignments(
          display.contexts
        );

        setContextNotices(
          display.notices
        );

        setSelectedAssignmentId(
          current =>
            display.contexts.some(
              item =>
                item.assignmentId === current
            )
              ? current
              : display.contexts[0]?.assignmentId ?? ""
        );
      } catch (cause) {
        if (!active) {
          return;
        }

        setAssignments([]);
        setSelectedAssignmentId("");
        setContextNotices([]);

        setLoadError(
          friendlyError(
            cause,
            "Teaching assignments could not be loaded."
          )
        );
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    void load();

    return () => {
      active = false;
    };
  }, []);

  const selectedAssignment =
    useMemo(
      () =>
        assignments.find(
          item =>
            item.assignmentId ===
            selectedAssignmentId
        ) ?? null,
      [
        assignments,
        selectedAssignmentId
      ]
    );

  async function handleRun(
    event: FormEvent<HTMLFormElement>
  ) {
    event.preventDefault();

    setActionError(null);
    setResult(null);

    if (!selectedAssignment) {
      setActionError(
        "Select an active teaching assignment."
      );
      return;
    }

    const configuredModel =
      modelCode.trim();

    if (!configuredModel) {
      setActionError(
        "Enter an authorised AI model code."
      );
      return;
    }

    const input =
      prompt.trim();

    if (!input) {
      setActionError(
        "Enter a question or teaching task for GT Teacher AI."
      );
      return;
    }

    setRunning(true);

    try {
      const response =
        await runTeacherAiRequest(
          selectedAssignment,
          configuredModel,
          input
        );

      setResult(response);
    } catch (cause) {
      setActionError(
        friendlyError(
          cause,
          "GT Teacher AI could not complete the request."
        )
      );
    } finally {
      setRunning(false);
    }
  }

  return (
    <main
      className="teacher-ai-workspace"
      aria-labelledby="teacher-ai-title"
    >
      <header className="teacher-ai-hero">
        <div>
          <p className="teacher-ai-eyebrow">
            GrowTogether School
          </p>

          <h1 id="teacher-ai-title">
            GT Teacher AI
          </h1>

          <p className="teacher-ai-lead">
            Your governed teaching assistant,
            working within your active teaching
            assignment.
          </p>
        </div>

        <div
          className="teacher-ai-governance"
          aria-label="AI governance notice"
        >
          <strong>
            Teacher remains in control
          </strong>

          <span>
            AI suggestions require professional
            review before classroom use.
          </span>
        </div>
      </header>

      <div className="teacher-ai-layout">
        <aside
          className="teacher-ai-context-panel"
          aria-label="Teaching context"
        >
          <div className="teacher-ai-panel-heading">
            <span>
              Teaching context
            </span>

            <span
              className="teacher-ai-status-pill"
            >
              Governed
            </span>
          </div>

          {
            loading && (
              <p role="status">
                Loading active teaching assignments...
              </p>
            )
          }

          {
            loadError && (
              <p
                className="teacher-ai-error"
                role="alert"
              >
                {loadError}
              </p>
            )
          }

          {
            !loading &&
            !loadError &&
            assignments.length === 0 && (
              <div
                className="teacher-ai-empty"
                role="status"
              >
                <strong>
                  No active teaching assignment
                </strong>

                <span>
                  Teacher AI becomes available
                  when an active assignment is
                  linked to your teacher profile.
                </span>
              </div>
            )
          }

          {
            assignments.length > 0 && (
              <>
                <label
                  className="teacher-ai-field"
                  htmlFor="teacher-ai-assignment"
                >
                  <span>
                    Active assignment
                  </span>

                  <select
                    id="teacher-ai-assignment"
                    value={selectedAssignmentId}
                    onChange={
                      event => {
                        setSelectedAssignmentId(
                          event.target.value
                        );
                        setResult(null);
                        setActionError(null);
                      }
                    }
                  >
                    {
                      assignments.map(
                        assignment => (
                          <option
                            key={
                              assignment.assignmentId
                            }
                            value={
                              assignment.assignmentId
                            }
                          >
                            {
                              `${assignment.className} — ${assignment.subjectName}`
                            }
                          </option>
                        )
                      )
                    }
                  </select>
                </label>

                {
                  contextNotices.length > 0 && (
                    <div
                      className="teacher-ai-context-notice"
                      role="status"
                    >
                      {
                        contextNotices.map(
                          notice => (
                            <p key={notice}>
                              {notice}
                            </p>
                          )
                        )
                      }
                    </div>
                  )
                }

                {
                  selectedAssignment && (
                    <dl className="teacher-ai-context-list">
                      <div>
                        <dt>
                          Teacher profile
                        </dt>
                        <dd>
                          {
                            selectedAssignment.teacherProfileId
                          }
                        </dd>
                      </div>

                      <div>
                        <dt>
                          Class
                        </dt>
                        <dd>
                          {
                            selectedAssignment.className
                          }
                        </dd>
                      </div>

                      <div>
                        <dt>
                          Subject
                        </dt>
                        <dd>
                          {
                            selectedAssignment.subjectName
                          }
                        </dd>
                      </div>

                      <div>
                        <dt>
                          Weekly periods
                        </dt>
                        <dd>
                          {
                            selectedAssignment.weeklyPeriods
                          }
                        </dd>
                      </div>

                      <div>
                        <dt>
                          Assignment status
                        </dt>
                        <dd>
                          {
                            selectedAssignment.assignmentStatus
                          }
                        </dd>
                      </div>
                    </dl>
                  )
                }
              </>
            )
          }
        </aside>

        <section
          className="teacher-ai-interaction"
          aria-label="Teacher AI interaction"
        >
          <div className="teacher-ai-panel-heading">
            <span>
              Ask GT Teacher AI
            </span>

            {
              running && (
                <span
                  className="teacher-ai-running"
                  role="status"
                >
                  Working...
                </span>
              )
            }
          </div>

          <div className="teacher-ai-quick-actions">
            <button
              type="button"
              onClick={
                () =>
                  setPrompt(
                    "Prepare a clear lesson outline for this teaching assignment."
                  )
              }
            >
              Lesson outline
            </button>

            <button
              type="button"
              onClick={
                () =>
                  setPrompt(
                    "Create revision questions for this teaching assignment."
                  )
              }
            >
              Revision questions
            </button>

            <button
              type="button"
              onClick={
                () =>
                  setPrompt(
                    "Suggest ways to support learners who are struggling with this subject."
                  )
              }
            >
              Support learners
            </button>
          </div>

          <form
            className="teacher-ai-form"
            onSubmit={handleRun}
          >
            <label
              className="teacher-ai-field"
              htmlFor="teacher-ai-model"
            >
              <span>
                Authorised AI model code
              </span>

              <input
                id="teacher-ai-model"
                value={modelCode}
                onChange={
                  event =>
                    setModelCode(
                      event.target.value
                    )
                }
                placeholder="Enter configured model code"
                autoComplete="off"
                aria-describedby="teacher-ai-model-help"
              />
            </label>

            <small
              id="teacher-ai-model-help"
              className="teacher-ai-field-help"
            >
              This must match a model authorised
              through the GT Enterprise AI
              Integration Framework.
            </small>

            <label
              className="teacher-ai-field"
              htmlFor="teacher-ai-prompt"
            >
              <span>
                Teaching request
              </span>

              <textarea
                id="teacher-ai-prompt"
                value={prompt}
                onChange={
                  event =>
                    setPrompt(
                      event.target.value
                    )
                }
                rows={8}
                maxLength={100000}
                placeholder="Ask for a lesson plan, explanation, revision activity, learner-support idea, assessment support..."
              />
            </label>

            {
              actionError && (
                <p
                  className="teacher-ai-error"
                  role="alert"
                >
                  {actionError}
                </p>
              )
            }

            <div className="teacher-ai-form-actions">
              <button
                className="teacher-ai-run-button"
                type="submit"
                disabled={
                  running ||
                  loading ||
                  !selectedAssignment
                }
              >
                {
                  running
                    ? "GT Teacher AI is working..."
                    : "Run Teacher AI"
                }
              </button>
            </div>
          </form>

          {
            result && (
              <section
                className="teacher-ai-result"
                aria-labelledby="teacher-ai-result-title"
              >
                <div className="teacher-ai-result-header">
                  <div>
                    <p className="teacher-ai-eyebrow">
                      Governed AI result
                    </p>

                    <h2 id="teacher-ai-result-title">
                      Request completed
                    </h2>
                  </div>

                  <span
                    className="teacher-ai-result-status"
                  >
                    {
                      result.execution.status
                    }
                  </span>
                </div>

                <dl className="teacher-ai-result-details">
                  <div>
                    <dt>
                      Request ID
                    </dt>

                    <dd>
                      {
                        result.execution.requestId
                      }
                    </dd>
                  </div>

                  <div>
                    <dt>
                      Assignment
                    </dt>

                    <dd>
                      {
                        result.context.assignmentId
                      }
                    </dd>
                  </div>

                  <div>
                    <dt>
                      Output reference
                    </dt>

                    <dd>
                      {
                        result.execution.outputReference
                          ?? "No output reference returned."
                      }
                    </dd>
                  </div>
                </dl>

                <p className="teacher-ai-review-notice">
                  Review AI-generated material
                  before using it for teaching,
                  assessment or learner guidance.
                </p>
              </section>
            )
          }
        </section>
      </div>
    </main>
  );
}
