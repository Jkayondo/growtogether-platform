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
  waitFor
} from "@testing-library/react";

const api = vi.hoisted(
  () => ({
    contexts: vi.fn(),
    run: vi.fn()
  })
);

vi.mock(
  "../../services/teacherAiDisplayContextService",
  () => ({
    loadTeacherAiDisplayContexts:
      api.contexts
  })
);

vi.mock(
  "../../services/teacherAiWorkflowService",
  () => ({
    runTeacherAiRequest:
      api.run
  })
);

import TeacherAiWorkspace
  from "./TeacherAiWorkspace";

function context(
  assignmentId = "assignment-1"
) {
  return {
    assignmentId,
    teacherProfileId: "teacher-1",
    classGradeId:
      assignmentId === "assignment-2"
        ? "class-2"
        : "class-1",
    subjectId:
      assignmentId === "assignment-2"
        ? "subject-2"
        : "subject-1",
    className:
      assignmentId === "assignment-2"
        ? "Senior One"
        : "Primary One",
    subjectName:
      assignmentId === "assignment-2"
        ? "Physics"
        : "Mathematics",
    weeklyPeriods: 5,
    assignmentStatus: "ACTIVE"
  };
}

async function finishLoading() {
  await waitFor(
    () => {
      expect(
        screen.queryByText(
          "Loading active teaching assignments..."
        )
      ).toBeNull();
    }
  );
}

describe(
  "TeacherAiWorkspace",
  () => {
    beforeEach(() => {
      api.contexts.mockReset();
      api.run.mockReset();

      api.contexts.mockResolvedValue({
        contexts: [
          context()
        ],
        notices: []
      });
    });

    afterEach(cleanup);

    it(
      "loads and displays the teacher active assignment context",
      async () => {
        render(<TeacherAiWorkspace />);

        await finishLoading();

        expect(
          screen.getByText(
            "GT Teacher AI"
          )
        ).toBeTruthy();

        expect(
          screen.getByText(
            "teacher-1"
          )
        ).toBeTruthy();

        expect(
          screen.getByText(
            "Primary One"
          )
        ).toBeTruthy();

        expect(
          screen.getByText(
            "Mathematics"
          )
        ).toBeTruthy();

        expect(
          screen.getByRole(
            "button",
            {
              name: "Run Teacher AI"
            }
          )
        ).toBeTruthy();
      }
    );

    it(
      "allows the teacher to switch active assignment context",
      async () => {
        api.contexts.mockResolvedValue({
          contexts: [
            context("assignment-1"),
            context("assignment-2")
          ],
          notices: []
        });

        render(<TeacherAiWorkspace />);

        await finishLoading();

        fireEvent.change(
          screen.getByLabelText(
            "Active assignment"
          ),
          {
            target: {
              value: "assignment-2"
            }
          }
        );

        expect(
          screen.getByText(
            "Senior One"
          )
        ).toBeTruthy();

        expect(
          screen.getByText(
            "Physics"
          )
        ).toBeTruthy();
      }
    );

    it(
      "shows a governed empty state when no assignment exists",
      async () => {
        api.contexts.mockResolvedValue({
          contexts: [],
          notices: []
        });

        render(<TeacherAiWorkspace />);

        await finishLoading();

        expect(
          screen.getByText(
            "No active teaching assignment"
          )
        ).toBeTruthy();

        expect(
          screen.getByText(
            /when an active assignment is linked/
          )
        ).toBeTruthy();

        expect(
          screen.getByRole(
            "button",
            {
              name: "Run Teacher AI"
            }
          )
        ).toHaveProperty(
          "disabled",
          true
        );
      }
    );

    it(
      "runs Teacher AI using selected real assignment context",
      async () => {
        api.run.mockResolvedValue({
          context: context(),
          submission: {
            requestId: "request-1",
            status: "ACTIVE"
          },
          execution: {
            requestId: "request-1",
            status: "SUCCEEDED",
            outputReference:
              "eds://teacher-output-1"
          }
        });

        render(<TeacherAiWorkspace />);

        await finishLoading();

        fireEvent.change(
          screen.getByLabelText(
            "Authorised AI model code"
          ),
          {
            target: {
              value: "configured-model"
            }
          }
        );

        fireEvent.change(
          screen.getByLabelText(
            "Teaching request"
          ),
          {
            target: {
              value:
                "Prepare a lesson on motion."
            }
          }
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name: "Run Teacher AI"
            }
          )
        );

        await waitFor(
          () => {
            expect(api.run)
              .toHaveBeenCalledWith(
                context(),
                "configured-model",
                "Prepare a lesson on motion."
              );
          }
        );

        expect(
          await screen.findByText(
            "Request completed"
          )
        ).toBeTruthy();

        expect(
          screen.getByText(
            "SUCCEEDED"
          )
        ).toBeTruthy();

        expect(
          screen.getByText(
            "eds://teacher-output-1"
          )
        ).toBeTruthy();
      }
    );

    it(
      "shows governed loading and execution failures",
      async () => {
        api.contexts.mockRejectedValueOnce(
          new Error("API Error 403")
        );

        const first =
          render(<TeacherAiWorkspace />);

        await finishLoading();

        expect(
          screen.getByRole(
            "alert"
          ).textContent
        ).toContain(
          "Access denied"
        );

        first.unmount();

        api.contexts.mockResolvedValue({
          contexts: [
            context()
          ],
          notices: []
        });

        api.run.mockRejectedValue(
          new Error(
            "Teacher AI execution failed."
          )
        );

        render(<TeacherAiWorkspace />);

        await finishLoading();

        fireEvent.change(
          screen.getByLabelText(
            "Authorised AI model code"
          ),
          {
            target: {
              value: "configured-model"
            }
          }
        );

        fireEvent.change(
          screen.getByLabelText(
            "Teaching request"
          ),
          {
            target: {
              value:
                "Prepare revision questions."
            }
          }
        );

        fireEvent.click(
          screen.getByRole(
            "button",
            {
              name: "Run Teacher AI"
            }
          )
        );

        await waitFor(
          () => {
            expect(
              screen.getByRole(
                "alert"
              ).textContent
            ).toContain(
              "Teacher AI execution failed."
            );
          }
        );
      }
    );
  }
);
