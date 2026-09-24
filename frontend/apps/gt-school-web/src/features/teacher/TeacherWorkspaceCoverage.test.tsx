import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi
} from "vitest";

import {
  cleanup,
  fireEvent,
  render,
  screen,
  waitFor,
  within
} from "@testing-library/react";

import {
  Permission
} from "../../auth/permissions";

import type {
  TeacherCoverageItem,
  TeacherCoverageStatus,
  TeacherCoverageView
} from "../../types/teacherCoverage";

import TeacherWorkspace from "./TeacherWorkspace";


const auth = vi.hoisted(() => ({
  hasPermission:
    vi.fn<(permission: string) => boolean>()
}));


const api = vi.hoisted(() => ({
  assignments: vi.fn(),
  programme: vi.fn(),
  subjects: vi.fn(),
  levels: vi.fn(),
  grades: vi.fn(),
  coverage: vi.fn(),
  progress: vi.fn(),
  complete: vi.fn(),
  remediation: vi.fn(),
  ahead: vi.fn()
}));


vi.mock("../../auth/authContext", () => ({
  useAuth: () => auth
}));


vi.mock("../../services/teachingAssignmentService", () => ({
  loadMyActiveTeachingAssignments:
    api.assignments
}));


vi.mock("../../services/teacherProgrammeService", () => ({
  loadMyTodayProgramme:
    api.programme
}));


vi.mock("../../services/subjectService", () => ({
  loadSubjects:
    api.subjects
}));


vi.mock("../../services/educationLevelService", () => ({
  loadEducationLevels:
    api.levels
}));


vi.mock("../../services/classGradeService", () => ({
  loadClassGrades:
    api.grades
}));


vi.mock("../../services/teacherCoverageService", () => ({
  loadMyTeacherCoverage:
    api.coverage,

  setMyTeacherCoverageInProgress:
    api.progress,

  completeMyCoverage:
    api.complete,

  setMyTeacherCoverageRequiresRemediation:
    api.remediation,

  setMyTeacherCoverageAheadOfSchedule:
    api.ahead
}));


function coverageItem(
  id: string,
  coverageStatus: TeacherCoverageStatus,
  coverageItem = "Fractions"
): TeacherCoverageItem {

  return {
    id,
    teacherProfileId: "teacher-1",
    teachingAssignmentId: "assignment-1",
    academicYearId: "year-1",
    academicTermId: "term-1",
    curriculumVersionId: "curriculum-version-1",
    curriculumSubjectId: "curriculum-subject-1",
    classGradeId: "class-1",
    coverageType: "TOPIC",
    coverageItem,
    plannedWeek: 4,
    coverageStatus,
    completionDate:
      coverageStatus === "COMPLETED"
        ? "2026-09-21"
        : null,
    teacherRemarks: null
  };

}


function coverageView(
  items: TeacherCoverageItem[],
  summary: TeacherCoverageView["summary"] = {
    total: items.length,
    notStarted:
      items.filter(
        item =>
          item.coverageStatus === "NOT_STARTED"
      ).length,
    inProgress:
      items.filter(
        item =>
          item.coverageStatus === "IN_PROGRESS"
      ).length,
    completed:
      items.filter(
        item =>
          item.coverageStatus === "COMPLETED"
      ).length,
    requiresRemediation:
      items.filter(
        item =>
          item.coverageStatus === "REQUIRES_REMEDIATION"
      ).length,
    aheadOfSchedule:
      items.filter(
        item =>
          item.coverageStatus === "AHEAD_OF_SCHEDULE"
      ).length
  }
): TeacherCoverageView {

  return {
    teacherProfileId: "teacher-1",
    teachingAssignmentId: null,
    summary,
    items
  };

}


async function finishCoverageLoading() {

  await waitFor(() => {

    expect(
      screen.queryByTestId(
        "teacher-coverage-loading"
      )
    ).toBeNull();

  });

}


beforeEach(() => {

  auth.hasPermission.mockReset();

  for (
    const mock
    of Object.values(api)
  ) {
    mock.mockReset();
  }


  auth.hasPermission.mockImplementation(
    permission =>
      permission === Permission.TEACHER_COVERAGE_READ
      || permission === Permission.TEACHER_COVERAGE_UPDATE
  );


  api.assignments.mockResolvedValue([]);

  api.programme.mockResolvedValue({
    date: "2026-09-21",
    zone: "Africa/Kampala",
    lessons: [],
    calendarEvents: []
  });

  api.subjects.mockResolvedValue([]);
  api.levels.mockResolvedValue([]);
  api.grades.mockResolvedValue([]);

  api.coverage.mockResolvedValue(
    coverageView([])
  );

  api.progress.mockResolvedValue(
    coverageItem(
      "coverage-1",
      "IN_PROGRESS"
    )
  );

  api.complete.mockResolvedValue(
    coverageItem(
      "coverage-1",
      "COMPLETED"
    )
  );

  api.remediation.mockResolvedValue(
    coverageItem(
      "coverage-1",
      "REQUIRES_REMEDIATION"
    )
  );

  api.ahead.mockResolvedValue(
    coverageItem(
      "coverage-1",
      "AHEAD_OF_SCHEDULE"
    )
  );

});


afterEach(
  cleanup
);


describe(
  "Teacher Workspace curriculum progress",
  () => {

    it(
      "does not call Coverage when read permission is absent",
      async () => {

        auth.hasPermission.mockReturnValue(false);

        render(
          <TeacherWorkspace />
        );

        expect(
          await screen.findByTestId(
            "teacher-coverage-read-denied"
          )
        ).toBeTruthy();

        expect(
          api.coverage
        ).not.toHaveBeenCalled();

      }
    );


    it(
      "loads Coverage independently from assignments and Today's Programme",
      async () => {

        api.coverage.mockImplementation(
          () =>
            new Promise<TeacherCoverageView>(
              () => {}
            )
        );

        render(
          <TeacherWorkspace />
        );

        expect(
          await screen.findByText(
            "No active teaching assignments found."
          )
        ).toBeTruthy();

        const programme =
          await screen.findByTestId(
            "teacher-programme"
          );

        expect(
          programme.textContent
        ).toContain(
          "2026-09-21"
        );

        expect(
          screen.getByTestId(
            "teacher-coverage-loading"
          )
        ).toBeTruthy();

      }
    );


    it(
      "renders a controlled empty Coverage state",
      async () => {

        render(
          <TeacherWorkspace />
        );

        expect(
          await screen.findByTestId(
            "teacher-coverage-empty"
          )
        ).toBeTruthy();

        expect(
          api.coverage
        ).toHaveBeenCalledTimes(1);

      }
    );


    it(
      "renders summary counters and Coverage item metadata",
      async () => {

        api.coverage.mockResolvedValue(
          coverageView(
            [
              coverageItem(
                "coverage-1",
                "NOT_STARTED"
              )
            ],
            {
              total: 15,
              notStarted: 1,
              inProgress: 2,
              completed: 3,
              requiresRemediation: 4,
              aheadOfSchedule: 5
            }
          )
        );

        render(
          <TeacherWorkspace />
        );

        await finishCoverageLoading();

        const summary =
          screen.getByLabelText(
            "Curriculum progress summary"
          );

        for (
          const value
          of ["15", "1", "2", "3", "4", "5"]
        ) {

          expect(
            within(summary).getByText(value)
          ).toBeTruthy();

        }

        const item =
          screen.getByTestId(
            "teacher-coverage-item-coverage-1"
          );

        expect(
          within(item).getByText("Fractions")
        ).toBeTruthy();

        expect(
          within(item).getByText("Topic")
        ).toBeTruthy();

        expect(
          item.textContent
        ).toContain("Planned week");

        expect(
          item.textContent
        ).toContain("assignment-1");

      }
    );


    it(
      "filters Coverage items by lifecycle status",
      async () => {

        api.coverage.mockResolvedValue(
          coverageView([
            coverageItem(
              "coverage-not-started",
              "NOT_STARTED",
              "Fractions"
            ),
            coverageItem(
              "coverage-completed",
              "COMPLETED",
              "Geometry"
            )
          ])
        );

        render(
          <TeacherWorkspace />
        );

        await screen.findByText("Fractions");

        const filter =
          screen.getByLabelText(
            "Filter curriculum progress by status"
          );

        fireEvent.change(
          filter,
          {
            target: {
              value: "COMPLETED"
            }
          }
        );

        expect(
          screen.queryByText("Fractions")
        ).toBeNull();

        expect(
          screen.getByText("Geometry")
        ).toBeTruthy();

        fireEvent.change(
          filter,
          {
            target: {
              value: "AHEAD_OF_SCHEDULE"
            }
          }
        );

        expect(
          screen.getByTestId(
            "teacher-coverage-filter-empty"
          )
        ).toBeTruthy();

      }
    );


    it(
      "renders Coverage as read-only without update permission",
      async () => {

        auth.hasPermission.mockImplementation(
          permission =>
            permission
            === Permission.TEACHER_COVERAGE_READ
        );

        api.coverage.mockResolvedValue(
          coverageView([
            coverageItem(
              "coverage-1",
              "NOT_STARTED"
            )
          ])
        );

        render(
          <TeacherWorkspace />
        );

        expect(
          await screen.findByText(
            "Read-only access. Status updates are not permitted."
          )
        ).toBeTruthy();

        expect(
          screen.queryByRole(
            "button",
            {
              name: "Complete"
            }
          )
        ).toBeNull();

      }
    );


    it(
      "exposes all four lifecycle actions with update permission",
      async () => {

        api.coverage.mockResolvedValue(
          coverageView([
            coverageItem(
              "coverage-1",
              "NOT_STARTED"
            )
          ])
        );

        render(
          <TeacherWorkspace />
        );

        await screen.findByText("Fractions");

        for (
          const name
          of [
            "In progress",
            "Complete",
            "Needs remediation",
            "Ahead of schedule"
          ]
        ) {

          expect(
            screen.getByRole(
              "button",
              { name }
            )
          ).toBeTruthy();

        }

      }
    );


    it(
      "completes Coverage with remarks and refreshes the view",
      async () => {

        api.coverage
          .mockResolvedValueOnce(
            coverageView([
              coverageItem(
                "coverage-1",
                "NOT_STARTED"
              )
            ])
          )
          .mockResolvedValueOnce(
            coverageView([
              coverageItem(
                "coverage-1",
                "COMPLETED"
              )
            ])
          );

        render(
          <TeacherWorkspace />
        );

        await screen.findByText("Fractions");

        fireEvent.change(
          screen.getByLabelText(
            "Completion remarks"
          ),
          {
            target: {
              value: "Finished chapter"
            }
          }
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            { name: "Complete" }
          )
        );

        await waitFor(() => {

          expect(
            api.complete
          ).toHaveBeenCalledWith(
            "coverage-1",
            "Finished chapter"
          );

        });

        await waitFor(() => {

          expect(
            api.coverage
          ).toHaveBeenCalledTimes(2);

        });

      }
    );


    it(
      "invokes progress, remediation and ahead lifecycle operations",
      async () => {

        api.coverage.mockResolvedValue(
          coverageView([
            coverageItem(
              "coverage-1",
              "NOT_STARTED"
            )
          ])
        );

        render(
          <TeacherWorkspace />
        );

        await screen.findByText("Fractions");

        const actions = [
          {
            name: "In progress",
            mock: api.progress
          },
          {
            name: "Needs remediation",
            mock: api.remediation
          },
          {
            name: "Ahead of schedule",
            mock: api.ahead
          }
        ];

        for (
          const [index, action]
          of actions.entries()
        ) {

          fireEvent.click(
            screen.getByRole(
              "button",
              {
                name: action.name
              }
            )
          );

          await waitFor(() => {

            expect(
              action.mock
            ).toHaveBeenCalledWith(
              "coverage-1"
            );

          });

          await waitFor(() => {

            expect(
              api.coverage
            ).toHaveBeenCalledTimes(
              index + 2
            );

          });

          await waitFor(() => {

            expect(
              screen.queryByText(
                "Updating curriculum progress..."
              )
            ).toBeNull();

          });

        }

      }
    );


    it.each([
      [
        "API Error 403",
        "You do not have permission to view curriculum progress."
      ],
      [
        "API Error 401",
        "Your session has expired. Please sign in again."
      ],
      [
        "Network failure",
        "Curriculum progress could not be loaded. Please reload to try again."
      ]
    ])(
      "renders controlled Coverage read failure: %s",
      async (
        failure,
        expectedMessage
      ) => {

        api.coverage.mockRejectedValue(
          new Error(failure)
        );

        render(
          <TeacherWorkspace />
        );

        const error =
          await screen.findByTestId(
            "teacher-coverage-error"
          );

        expect(
          error.textContent
        ).toBe(expectedMessage);

      }
    );


    it(
      "retains current Coverage data when a mutation fails",
      async () => {

        api.coverage.mockResolvedValue(
          coverageView([
            coverageItem(
              "coverage-1",
              "NOT_STARTED"
            )
          ])
        );

        api.progress.mockRejectedValue(
          new Error("Network failure")
        );

        render(
          <TeacherWorkspace />
        );

        await screen.findByText("Fractions");

        fireEvent.click(
          screen.getByRole(
            "button",
            { name: "In progress" }
          )
        );

        const mutationError =
          await screen.findByTestId(
            "teacher-coverage-mutation-error"
          );

        expect(
          mutationError.textContent
        ).toBe(
          "Curriculum progress could not be updated. Please try again."
        );

        expect(
          screen.getByText("Fractions")
        ).toBeTruthy();

        expect(
          api.coverage
        ).toHaveBeenCalledTimes(1);

      }
    );


    it(
      "keeps Today's Programme visible when Coverage loading fails",
      async () => {

        api.coverage.mockRejectedValue(
          new Error("Network failure")
        );

        render(
          <TeacherWorkspace />
        );

        expect(
          await screen.findByTestId(
            "teacher-coverage-error"
          )
        ).toBeTruthy();

        const programme =
          screen.getByTestId(
            "teacher-programme"
          );

        expect(
          programme.textContent
        ).toContain(
          "2026-09-21"
        );

        expect(
          programme
        ).toBeTruthy();

      }
    );

  }
);
