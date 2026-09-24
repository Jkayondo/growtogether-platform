import {
  useEffect,
  useState
} from "react";

import GTSection from "../../components/common/GTSection";

import {
  useAuth
} from "../../auth/authContext";

import {
  Permission
} from "../../auth/permissions";

import {
  completeMyCoverage,
  loadMyTeacherCoverage,
  setMyTeacherCoverageAheadOfSchedule,
  setMyTeacherCoverageInProgress,
  setMyTeacherCoverageRequiresRemediation
} from "../../services/teacherCoverageService";

import {
  loadMyTodayProgramme
} from "../../services/teacherProgrammeService";

import {
  loadMyActiveTeachingAssignments
} from "../../services/teachingAssignmentService";

import {
  loadSubjects
} from "../../services/subjectService";

import {
  loadClassGrades
} from "../../services/classGradeService";

import type {
  TeachingAssignment
} from "../../types/teachingAssignment";

import type {
  Subject
} from "../../types/subject";

import type {
  ClassGrade
} from "../../types/classGrade";
import type {
  TeacherProgrammeToday
} from "../../types/teacherProgramme";

import type {
  TeacherCoverageStatus,
  TeacherCoverageView
} from "../../types/teacherCoverage";

import { loadEducationLevels } from "../../services/educationLevelService";

import "./TeacherWorkspace.css";


export default function TeacherWorkspace() {


  const {
    hasPermission
  } =
    useAuth();


  const canReadCoverage =
    hasPermission(
      Permission.TEACHER_COVERAGE_READ
    );


  const canUpdateCoverage =
    hasPermission(
      Permission.TEACHER_COVERAGE_UPDATE
    );


  const [
    assignments,
    setAssignments
  ] =
    useState<TeachingAssignment[]>([]);


  const [
    subjects,
    setSubjects
  ] =
    useState<Subject[]>([]);


  const [
    classGrades,
    setClassGrades
  ] =
    useState<ClassGrade[]>([]);


  const [
    loading,
    setLoading
  ] =
    useState(true);


  const [
    error,
    setError
  ] =
    useState<string | null>(null);

  const [
    programme,
    setProgramme
  ] =
    useState<TeacherProgrammeToday | null>(
      null
    );

  const [
    programmeLoading,
    setProgrammeLoading
  ] =
    useState(true);

  const [
    programmeError,
    setProgrammeError
  ] =
    useState<string | null>(null);


  const [
    coverage,
    setCoverage
  ] =
    useState<TeacherCoverageView | null>(
      null
    );


  const [
    coverageLoading,
    setCoverageLoading
  ] =
    useState(true);


  const [
    coverageError,
    setCoverageError
  ] =
    useState<string | null>(null);


  const [
    coverageFilter,
    setCoverageFilter
  ] =
    useState<TeacherCoverageStatus | "ALL">(
      "ALL"
    );


  const [
    coverageMutatingId,
    setCoverageMutatingId
  ] =
    useState<string | null>(null);


  const [
    coverageMutationError,
    setCoverageMutationError
  ] =
    useState<string | null>(null);


  const [
    coverageRemarks,
    setCoverageRemarks
  ] =
    useState<Record<string, string>>(
      {}
    );



  useEffect(() => {
    let active = true;

    async function load() {
      setLoading(true);
      setError(null);

      try {
        const assignmentData =
          await loadMyActiveTeachingAssignments();

        if (!active) return;
        setAssignments(assignmentData);

        if (assignmentData.length === 0) {
          setSubjects([]);
          setClassGrades([]);
          return;
        }

        async function loadAssignedClasses() {
          const levels = await loadEducationLevels();
          const levelIds = [...new Set(levels.map(level => level.id))];
          const assignedIds = new Set(
            assignmentData.map(assignment => assignment.classGradeId)
          );
          const gradesById = new Map<string, ClassGrade>();
          let failed = false;

          // Limit concurrent requests while covering every education level.
          for (let offset = 0; offset < levelIds.length; offset += 4) {
            if (!active) return { grades: [], incomplete: true };

            const results = await Promise.allSettled(
              levelIds.slice(offset, offset + 4).map(id => loadClassGrades(id))
            );

            for (const result of results) {
              if (result.status === "rejected") {
                failed = true;
                continue;
              }

              for (const grade of result.value) {
                if (assignedIds.has(grade.id)) {
                  gradesById.set(grade.id, grade);
                }
              }
            }
          }

          return {
            grades: [...gradesById.values()],
            incomplete: failed ||
              [...assignedIds].some(id => !gradesById.has(id))
          };
        }

        const [subjectResult, classResult] = await Promise.allSettled([
          loadSubjects(),
          loadAssignedClasses()
        ]);

        if (!active) return;

        const notices: string[] = [];

        if (subjectResult.status === "fulfilled") {
          setSubjects(subjectResult.value);
          const subjectIds = new Set(
            subjectResult.value.map(subject => subject.id)
          );
          if (assignmentData.some(item => !subjectIds.has(item.subjectId))) {
            notices.push("Some subject names are unavailable.");
          }
        } else {
          setSubjects([]);
          notices.push("Subject names could not be loaded.");
        }

        if (classResult.status === "fulfilled") {
          setClassGrades(classResult.value.grades);
          if (classResult.value.incomplete) {
            notices.push("Some class names are unavailable.");
          }
        } else {
          setClassGrades([]);
          notices.push("Class names could not be loaded.");
        }

        if (notices.length > 0) {
          setError(
            notices.join(" ") +
            " Assignments remain visible with reference IDs where needed."
          );
        }
      } catch (cause) {
        if (!active) return;
        setAssignments([]);
        setSubjects([]);
        setClassGrades([]);

        const message = cause instanceof Error ? cause.message : "";
        setError(
          message === "API Error 403"
            ? "Access denied. Check your assignment-read permission and active teacher profile linkage."
            : message === "API Error 401"
              ? "Your session has expired. Please sign in again."
              : "Teaching assignments could not be loaded. Please reload to try again."
        );
      } finally {
        if (active) setLoading(false);
      }
    }

    void load();

    return () => {
      active = false;
    };
  }, []);



  useEffect(() => {

    let active = true;

    async function loadProgramme() {

      setProgrammeLoading(true);
      setProgrammeError(null);

      try {

        const data =
          await loadMyTodayProgramme();

        if (!active) return;

        setProgramme(data);

      } catch (cause) {

        if (!active) return;

        setProgramme(null);

        const message =
          cause instanceof Error
            ? cause.message
            : "";

        setProgrammeError(
          message === "API Error 403"
            ? "You do not have permission to view today's programme."
            : message === "API Error 401"
              ? "Your session has expired. Please sign in again."
              : "Today's programme could not be loaded. Please reload to try again."
        );

      } finally {

        if (active) {
          setProgrammeLoading(false);
        }

      }

    }

    void loadProgramme();

    return () => {
      active = false;
    };

  }, []);



  useEffect(() => {

    let active = true;

    if (!canReadCoverage) {

      setCoverage(null);
      setCoverageError(null);
      setCoverageLoading(false);

      return () => {
        active = false;
      };

    }


    async function loadCoverage() {

      setCoverageLoading(true);
      setCoverageError(null);

      try {

        const data =
          await loadMyTeacherCoverage();

        if (!active) return;

        setCoverage(data);

      } catch (cause) {

        if (!active) return;

        setCoverage(null);

        const message =
          cause instanceof Error
            ? cause.message
            : "";

        setCoverageError(
          message === "API Error 403"
            ? "You do not have permission to view curriculum progress."
            : message === "API Error 401"
              ? "Your session has expired. Please sign in again."
              : "Curriculum progress could not be loaded. Please reload to try again."
        );

      } finally {

        if (active) {
          setCoverageLoading(false);
        }

      }

    }


    void loadCoverage();

    return () => {
      active = false;
    };

  }, [canReadCoverage]);



  async function runCoverageMutation(
    coverageId: string,
    operation: () => Promise<unknown>
  ) {

    setCoverageMutatingId(
      coverageId
    );

    setCoverageMutationError(
      null
    );

    try {

      await operation();

      const refreshed =
        await loadMyTeacherCoverage();

      setCoverage(
        refreshed
      );

      setCoverageRemarks(
        current => {

          const next = {
            ...current
          };

          delete next[
            coverageId
          ];

          return next;

        }
      );

    } catch (cause) {

      const message =
        cause instanceof Error
          ? cause.message
          : "";

      setCoverageMutationError(
        message === "API Error 403"
          ? "You do not have permission to update curriculum progress."
          : message === "API Error 401"
            ? "Your session has expired. Please sign in again."
            : "Curriculum progress could not be updated. Please try again."
      );

    } finally {

      setCoverageMutatingId(
        null
      );

    }

  }



  function formatCoverageLabel(
    value: string
  ) {

    return value
      .toLowerCase()
      .split("_")
      .map(
        word =>
          word.charAt(0).toUpperCase()
          + word.slice(1)
      )
      .join(" ");

  }



  function subjectName(
    id: string
  ) {

    return subjects.find(
      subject =>
        subject.id === id
    )?.subjectName
      ?? id;

  }



  function className(
    id: string
  ) {

    return classGrades.find(
      grade =>
        grade.id === id
    )?.className
      ?? id;

  }



  const filteredCoverageItems =
    coverage?.items.filter(
      item =>
        coverageFilter === "ALL"
        || item.coverageStatus
          === coverageFilter
    )
    ?? [];


  return (

    <div className="gt-dashboard teacher-workspace">


      <GTSection title="Teacher Workspace">


        <div className="teacher-summary-grid">


          <div className="teacher-card">

            <h3>
              My Teaching Assignments
            </h3>


            {
              loading && (
                <p>
                  Loading assignments...
                </p>
              )
            }


            {
              error && (
                <p role="alert">{error}</p>
              )
            }


            {!loading && !error && assignments.length === 0 && (
              <p role="status">
                No active teaching assignments found.
              </p>
            )}

            {
              assignments.map(
                assignment => (

                  <div
                    key={assignment.id}
                    className="teacher-assignment-card"
                  >

                    <strong>
                      {
                        className(
                          assignment.classGradeId
                        )
                      }
                    </strong>


                    <p>
                      Subject:
                      {" "}
                      {
                        subjectName(
                          assignment.subjectId
                        )
                      }
                    </p>


                    <p>
                      Periods per week:
                      {" "}
                      {assignment.weeklyPeriods}
                    </p>


                    <p>
                      Status:
                      {" "}
                      {assignment.assignmentStatus}
                    </p>


                  </div>

                )
              )
            }


          </div>



          <div
              className="teacher-card"
              data-testid="teacher-programme"
            >

              <h3>
                Today's Programme
              </h3>

              {
                programmeLoading
                  ? (
                    <p>
                      Loading today's programme...
                    </p>
                  )
                  : programmeError
                    ? (
                      <p data-testid="teacher-programme-error">
                        {programmeError}
                      </p>
                    )
                    : programme
                      ? (
                        <>

                          <p>
                            {programme.date}
                            {" · "}
                            {programme.zone}
                          </p>

                          {
                            programme.lessons.length === 0 &&
                            programme.calendarEvents.length === 0
                              ? (
                                <p>
                                  No lessons, meetings or school activities are scheduled for today.
                                </p>
                              )
                              : (
                                <>

                                  {
                                    programme.lessons.length > 0 &&
                                    (
                                      <div>

                                        <strong>
                                          Lessons
                                        </strong>

                                        <ul>

                                          {
                                            programme.lessons.map(
                                              lesson => (
                                                <li
                                                  key={
                                                    [
                                                      lesson.timetableId,
                                                      lesson.bellPeriodId,
                                                      lesson.classGradeId,
                                                      lesson.subjectOfferingId
                                                    ].join("-")
                                                  }
                                                >
                                                  {
                                                    lesson.startTime.slice(
                                                      0,
                                                      5
                                                    )
                                                  }
                                                  {"–"}
                                                  {
                                                    lesson.endTime.slice(
                                                      0,
                                                      5
                                                    )
                                                  }
                                                  {" · "}
                                                  {lesson.subjectName}
                                                  {" · "}
                                                  {lesson.className}
                                                  {
                                                    lesson.streamName
                                                      ? (
                                                        <>
                                                          {" / "}
                                                          {lesson.streamName}
                                                        </>
                                                      )
                                                      : null
                                                  }
                                                </li>
                                              )
                                            )
                                          }

                                        </ul>

                                      </div>
                                    )
                                  }

                                  {
                                    programme.calendarEvents.length > 0 &&
                                    (
                                      <div>

                                        <strong>
                                          School calendar
                                        </strong>

                                        <ul>

                                          {
                                            programme.calendarEvents.map(
                                              event => (
                                                <li key={event.id}>
                                                  {event.eventName}
                                                  {" · "}
                                                  {event.eventType}
                                                  {" · "}
                                                  {event.eventStatus}
                                                </li>
                                              )
                                            )
                                          }

                                        </ul>

                                      </div>
                                    )
                                  }

                                </>
                              )
                          }

                        </>
                      )
                      : (
                        <p>
                          Today's programme is unavailable.
                        </p>
                      )
              }

            </div>



          <div
              className="teacher-card teacher-coverage-card"
              data-testid="teacher-coverage"
            >

              <div className="teacher-coverage-header">

                <div>

                  <h3>
                    Curriculum Progress
                  </h3>

                  <p>
                    Track completed topics and teaching coverage.
                  </p>

                </div>


                {
                  canReadCoverage
                  && coverage
                  && (
                    <label
                      className="teacher-coverage-filter"
                      htmlFor="teacher-coverage-filter"
                    >
                      Status

                      <select
                        id="teacher-coverage-filter"
                        aria-label="Filter curriculum progress by status"
                        value={coverageFilter}
                        onChange={
                          event => {

                            const nextStatus =
                              event.target.value;

                            if (
                              nextStatus === "ALL"
                              || nextStatus === "NOT_STARTED"
                              || nextStatus === "IN_PROGRESS"
                              || nextStatus === "COMPLETED"
                              || nextStatus === "REQUIRES_REMEDIATION"
                              || nextStatus === "AHEAD_OF_SCHEDULE"
                            ) {

                              setCoverageFilter(
                                nextStatus
                              );

                            }

                          }
                        }
                      >
                        <option value="ALL">
                          All statuses
                        </option>

                        <option value="NOT_STARTED">
                          Not started
                        </option>

                        <option value="IN_PROGRESS">
                          In progress
                        </option>

                        <option value="COMPLETED">
                          Completed
                        </option>

                        <option value="REQUIRES_REMEDIATION">
                          Requires remediation
                        </option>

                        <option value="AHEAD_OF_SCHEDULE">
                          Ahead of schedule
                        </option>
                      </select>

                    </label>
                  )
                }

              </div>


              {
                !canReadCoverage
                  ? (
                    <p
                      role="status"
                      data-testid="teacher-coverage-read-denied"
                    >
                      You do not have permission to view curriculum progress.
                    </p>
                  )
                  : coverageLoading
                    ? (
                      <p data-testid="teacher-coverage-loading">
                        Loading curriculum progress...
                      </p>
                    )
                    : coverageError
                      ? (
                        <p
                          role="alert"
                          data-testid="teacher-coverage-error"
                        >
                          {coverageError}
                        </p>
                      )
                      : coverage
                        ? (
                          <>

                            <div
                              className="teacher-coverage-summary"
                              aria-label="Curriculum progress summary"
                            >

                              <div>
                                <strong>{coverage.summary.total}</strong>
                                <span>Total</span>
                              </div>

                              <div>
                                <strong>{coverage.summary.notStarted}</strong>
                                <span>Not started</span>
                              </div>

                              <div>
                                <strong>{coverage.summary.inProgress}</strong>
                                <span>In progress</span>
                              </div>

                              <div>
                                <strong>{coverage.summary.completed}</strong>
                                <span>Completed</span>
                              </div>

                              <div>
                                <strong>
                                  {coverage.summary.requiresRemediation}
                                </strong>
                                <span>Needs remediation</span>
                              </div>

                              <div>
                                <strong>
                                  {coverage.summary.aheadOfSchedule}
                                </strong>
                                <span>Ahead</span>
                              </div>

                            </div>


                            {
                              coverageMutationError
                              && (
                                <p
                                  role="alert"
                                  data-testid="teacher-coverage-mutation-error"
                                >
                                  {coverageMutationError}
                                </p>
                              )
                            }


                            {
                              coverage.items.length === 0
                                ? (
                                  <p
                                    role="status"
                                    data-testid="teacher-coverage-empty"
                                  >
                                    No curriculum coverage items are currently assigned.
                                  </p>
                                )
                                : filteredCoverageItems.length === 0
                                  ? (
                                    <p
                                      role="status"
                                      data-testid="teacher-coverage-filter-empty"
                                    >
                                      No curriculum coverage items match this status.
                                    </p>
                                  )
                                  : (
                                    <div className="teacher-coverage-items">

                                      {
                                        filteredCoverageItems.map(
                                          item => {

                                            const mutating =
                                              coverageMutatingId
                                              === item.id;

                                            const remarks =
                                              coverageRemarks[
                                                item.id
                                              ]
                                              ?? item.teacherRemarks
                                              ?? "";

                                            return (
                                              <article
                                                key={item.id}
                                                className="teacher-coverage-item"
                                                data-testid={
                                                  `teacher-coverage-item-${item.id}`
                                                }
                                              >

                                                <div className="teacher-coverage-item-header">

                                                  <div>
                                                    <strong>
                                                      {item.coverageItem}
                                                    </strong>

                                                    <span>
                                                      {
                                                        formatCoverageLabel(
                                                          item.coverageType
                                                        )
                                                      }
                                                    </span>
                                                  </div>

                                                  <span className="teacher-coverage-status">
                                                    {
                                                      formatCoverageLabel(
                                                        item.coverageStatus
                                                      )
                                                    }
                                                  </span>

                                                </div>


                                                <dl className="teacher-coverage-meta">

                                                  <div>
                                                    <dt>Planned week</dt>
                                                    <dd>
                                                      {
                                                        item.plannedWeek
                                                        ?? "Not set"
                                                      }
                                                    </dd>
                                                  </div>

                                                  <div>
                                                    <dt>Completion date</dt>
                                                    <dd>
                                                      {
                                                        item.completionDate
                                                        ?? "Not completed"
                                                      }
                                                    </dd>
                                                  </div>

                                                  <div>
                                                    <dt>Assignment</dt>
                                                    <dd>
                                                      {item.teachingAssignmentId}
                                                    </dd>
                                                  </div>

                                                </dl>


                                                {
                                                  item.teacherRemarks
                                                  && (
                                                    <p className="teacher-coverage-existing-remarks">
                                                      <strong>
                                                        Remarks:
                                                      </strong>
                                                      {" "}
                                                      {item.teacherRemarks}
                                                    </p>
                                                  )
                                                }


                                                {
                                                  canUpdateCoverage
                                                    ? (
                                                      <>

                                                        <label
                                                          className="teacher-coverage-remarks"
                                                          htmlFor={
                                                            `teacher-coverage-remarks-${item.id}`
                                                          }
                                                        >
                                                          Completion remarks

                                                          <input
                                                            id={
                                                              `teacher-coverage-remarks-${item.id}`
                                                            }
                                                            type="text"
                                                            value={remarks}
                                                            disabled={mutating}
                                                            onChange={
                                                              event =>
                                                                setCoverageRemarks(
                                                                  current => ({
                                                                    ...current,
                                                                    [item.id]:
                                                                      event.target.value
                                                                  })
                                                                )
                                                            }
                                                            placeholder="Optional remarks"
                                                          />

                                                        </label>


                                                        <div className="teacher-coverage-actions">

                                                          <button
                                                            type="button"
                                                            disabled={
                                                              mutating
                                                              || item.coverageStatus
                                                                === "IN_PROGRESS"
                                                            }
                                                            onClick={
                                                              () => {
                                                                void runCoverageMutation(
                                                                  item.id,
                                                                  () =>
                                                                    setMyTeacherCoverageInProgress(
                                                                      item.id
                                                                    )
                                                                );
                                                              }
                                                            }
                                                          >
                                                            In progress
                                                          </button>

                                                          <button
                                                            type="button"
                                                            disabled={
                                                              mutating
                                                              || item.coverageStatus
                                                                === "COMPLETED"
                                                            }
                                                            onClick={
                                                              () => {
                                                                void runCoverageMutation(
                                                                  item.id,
                                                                  () =>
                                                                    completeMyCoverage(
                                                                      item.id,
                                                                      remarks
                                                                    )
                                                                );
                                                              }
                                                            }
                                                          >
                                                            Complete
                                                          </button>

                                                          <button
                                                            type="button"
                                                            disabled={
                                                              mutating
                                                              || item.coverageStatus
                                                                === "REQUIRES_REMEDIATION"
                                                            }
                                                            onClick={
                                                              () => {
                                                                void runCoverageMutation(
                                                                  item.id,
                                                                  () =>
                                                                    setMyTeacherCoverageRequiresRemediation(
                                                                      item.id
                                                                    )
                                                                );
                                                              }
                                                            }
                                                          >
                                                            Needs remediation
                                                          </button>

                                                          <button
                                                            type="button"
                                                            disabled={
                                                              mutating
                                                              || item.coverageStatus
                                                                === "AHEAD_OF_SCHEDULE"
                                                            }
                                                            onClick={
                                                              () => {
                                                                void runCoverageMutation(
                                                                  item.id,
                                                                  () =>
                                                                    setMyTeacherCoverageAheadOfSchedule(
                                                                      item.id
                                                                    )
                                                                );
                                                              }
                                                            }
                                                          >
                                                            Ahead of schedule
                                                          </button>

                                                        </div>


                                                        {
                                                          mutating
                                                          && (
                                                            <p
                                                              role="status"
                                                              className="teacher-coverage-updating"
                                                            >
                                                              Updating curriculum progress...
                                                            </p>
                                                          )
                                                        }

                                                      </>
                                                    )
                                                    : (
                                                      <p className="teacher-coverage-readonly">
                                                        Read-only access. Status updates are not permitted.
                                                      </p>
                                                    )
                                                }

                                              </article>
                                            );

                                          }
                                        )
                                      }

                                    </div>
                                  )
                            }

                          </>
                        )
                        : (
                          <p>
                            Curriculum progress is unavailable.
                          </p>
                        )
              }

            </div>


        </div>


      </GTSection>


      <GTSection title="Teaching Tools">


        <div className="teacher-tools-grid">

          <div className="teacher-tool-card">
            Curriculum
          </div>

          <div className="teacher-tool-card">
            Scheme of Work
          </div>

          <div className="teacher-tool-card">
            Lesson Plans
          </div>

          <div className="teacher-tool-card">
            Teaching Notes
          </div>

          <div className="teacher-tool-card">
            Assessments
          </div>

          <div className="teacher-tool-card">
            Exams & Results
          </div>

          <div className="teacher-tool-card">
            Attendance
          </div>

          <div className="teacher-tool-card">
            GT Connect
          </div>

        </div>


      </GTSection>


    </div>

  );

}
