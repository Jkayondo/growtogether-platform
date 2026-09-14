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
  within
} from "@testing-library/react";

const api =
  vi.hoisted(
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

import TeacherAiWorkspace from "./TeacherAiWorkspace";

describe(
  "TeacherAiWorkspace UX accessibility",
  () => {
    beforeEach(() => {
      api.contexts.mockReset();
      api.run.mockReset();

      api.contexts.mockResolvedValue({
        contexts: [
          {
            assignmentId:
              "assignment-1",

            teacherProfileId:
              "teacher-1",

            classGradeId:
              "class-1",

            subjectId:
              "subject-1",

            className:
              "Primary One",

            subjectName:
              "Mathematics",

            weeklyPeriods: 5,

            assignmentStatus:
              "ACTIVE"
          }
        ],

        notices: []
      });
    });

    afterEach(() => {
      cleanup();
    });

    it(
      "shows the governed three-step Teacher AI workflow",
      async () => {
        render(
          <TeacherAiWorkspace />
        );

        await screen.findByText(
          "Primary One"
        );

        const workflow =
          screen.getByLabelText(
            "Teacher AI workflow"
          );

        expect(
          within(workflow).getByText(
            "Choose teaching context"
          )
        ).toBeTruthy();

        expect(
          within(workflow).getByText(
            "Ask GT Teacher AI"
          )
        ).toBeTruthy();

        expect(
          within(workflow).getByText(
            "Review before classroom use"
          )
        ).toBeTruthy();
      }
    );

    it(
      "associates prompt guidance with the teaching request",
      async () => {
        render(
          <TeacherAiWorkspace />
        );

        await screen.findByText(
          "Primary One"
        );

        const prompt =
          screen.getByLabelText(
            "Teaching request"
          );

        expect(
          prompt.getAttribute(
            "aria-describedby"
          )
        ).toBe(
          "teacher-ai-prompt-help"
        );

        expect(
          screen.getByText(
            /Be specific about the lesson objective/
          )
        ).toBeTruthy();

        expect(
          screen.getByText(
            /Do not include unnecessary confidential learner information/
          )
        ).toBeTruthy();
      }
    );
  }
);
