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
  render,
  screen,
  waitFor
} from "@testing-library/react";

import TeacherWorkspace from "./TeacherWorkspace";

vi.mock("../../auth/authContext", () => ({
  useAuth: () => ({
    hasPermission: () => false,
  }),
}));


const api = vi.hoisted(() => ({
  assignments: vi.fn(),
  subjects: vi.fn(),
  levels: vi.fn(),
  grades: vi.fn(),
  programme: vi.fn(),
}));

vi.mock("../../services/teachingAssignmentService", () => ({
  loadMyActiveTeachingAssignments:
    api.assignments,
}));

vi.mock("../../services/subjectService", () => ({
  loadSubjects:
    api.subjects,
}));

vi.mock("../../services/educationLevelService", () => ({
  loadEducationLevels:
    api.levels,
}));

vi.mock("../../services/classGradeService", () => ({
  loadClassGrades:
    api.grades,
}));

vi.mock("../../services/teacherProgrammeService", () => ({
  loadMyTodayProgramme:
    api.programme,
}));

function emptyProgramme() {
  return {
    date: "2026-09-19",
    zone: "Africa/Kampala",
    lessons: [],
    calendarEvents: [],
  };
}

async function finishProgrammeLoading() {

  await waitFor(() => {

    expect(
      screen.queryByText(
        "Loading today's programme..."
      )
    ).toBeNull();

  });

}

beforeEach(() => {

  for (const mock of Object.values(api)) {
    mock.mockReset();
  }

  api.assignments.mockResolvedValue([]);

  api.subjects.mockResolvedValue([]);

  api.levels.mockResolvedValue([]);

  api.grades.mockResolvedValue([]);

  api.programme.mockResolvedValue(
    emptyProgramme()
  );

});

afterEach(cleanup);

describe(
  "Teacher Workspace Today's Programme",
  () => {

    it(
      "loads independently from teaching assignments",
      async () => {

        api.programme.mockImplementation(
          () => new Promise(() => {})
        );

        render(
          <TeacherWorkspace />
        );

        await waitFor(() => {

          expect(
            screen.queryByText(
              "Loading assignments..."
            )
          ).toBeNull();

        });

        expect(
          screen.getByText(
            "Loading today's programme..."
          )
        ).toBeTruthy();

        expect(
          api.programme
        ).toHaveBeenCalledWith();

      }
    );


    it(
      "shows the legitimate empty programme state",
      async () => {

        render(
          <TeacherWorkspace />
        );

        await finishProgrammeLoading();

        expect(
          screen.getByText(
            "No lessons, meetings or school activities are scheduled for today."
          )
        ).toBeTruthy();

        expect(
          api.programme
        ).toHaveBeenCalledTimes(1);

      }
    );


    it(
      "renders today's lessons and school calendar",
      async () => {

        api.programme.mockResolvedValue({
          date: "2026-09-19",
          zone: "Africa/Kampala",
          lessons: [
            {
              timetableId: "timetable-1",
              bellPeriodId: "period-1",
              periodCode: "P1",
              periodName: "Period 1",
              sequenceNumber: 1,
              startTime: "08:00:00",
              endTime: "08:40:00",
              classGradeId: "class-1",
              classCode: "P1",
              className: "Primary One",
              streamId: "stream-1",
              streamCode: "A",
              streamName: "A",
              subjectOfferingId: "offering-1",
              subjectId: "subject-1",
              subjectCode: "MATH",
              subjectName: "Mathematics",
              activityName: null,
            },
          ],
          calendarEvents: [
            {
              id: "event-1",
              eventCode: "STAFF-MEETING",
              eventName: "Staff Meeting",
              eventType: "MEETING",
              startAt:
                "2026-09-19T10:00:00Z",
              endAt:
                "2026-09-19T11:00:00Z",
              eventStatus: "SCHEDULED",
            },
          ],
        });

        render(
          <TeacherWorkspace />
        );

        await finishProgrammeLoading();

        expect(
          screen.getByText("Lessons")
        ).toBeTruthy();

        expect(
          screen.getByText(
            /08:00–08:40/
          )
        ).toBeTruthy();

        expect(
          screen.getByText(
            /Mathematics/
          )
        ).toBeTruthy();

        expect(
          screen.getByText(
            /Primary One/
          )
        ).toBeTruthy();

        expect(
          screen.getByText(
            "School calendar"
          )
        ).toBeTruthy();

        expect(
          screen.getByText(
            /Staff Meeting/
          )
        ).toBeTruthy();

      }
    );


    it.each([
      [
        "API Error 403",
        "You do not have permission to view today's programme.",
      ],
      [
        "API Error 401",
        "Your session has expired. Please sign in again.",
      ],
      [
        "Network failure",
        "Today's programme could not be loaded. Please reload to try again.",
      ],
    ])(
      "localises programme failure: %s",
      async (failure, expected) => {

        api.programme.mockRejectedValue(
          new Error(failure)
        );

        render(
          <TeacherWorkspace />
        );

        await finishProgrammeLoading();

        expect(
          screen.getByTestId(
            "teacher-programme-error"
          ).textContent
        ).toBe(expected);

        expect(
          screen.queryByText(
            "No active teaching assignments found."
          )
        ).toBeTruthy();

      }
    );


    it(
      "keeps programme visible when assignment loading fails",
      async () => {

        api.assignments.mockRejectedValue(
          new Error("API Error 403")
        );

        api.programme.mockResolvedValue(
          emptyProgramme()
        );

        render(
          <TeacherWorkspace />
        );

        await finishProgrammeLoading();

        await waitFor(() => {

          expect(
            screen.getByRole(
              "alert"
            ).textContent
          ).toContain(
            "Access denied."
          );

        });

        expect(
          screen.getByText(
            "No lessons, meetings or school activities are scheduled for today."
          )
        ).toBeTruthy();

      }
    );

  }
);
