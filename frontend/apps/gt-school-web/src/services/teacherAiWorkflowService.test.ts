import {
  beforeEach,
  describe,
  expect,
  it,
  vi
} from "vitest";

const api = vi.hoisted(() => ({
  submit: vi.fn(),
  execute: vi.fn(),
  status: vi.fn()
}));

vi.mock(
  "./teacherAiApi",
  () => ({
    submitTeacherAiRequest: api.submit,
    executeTeacherAiRequest: api.execute,
    getTeacherAiRequestStatus: api.status
  })
);

import {
  loadTeacherAiStatus,
  runTeacherAiRequest
} from "./teacherAiWorkflowService";

const context = {
  assignmentId: "assignment-1",
  teacherProfileId: "teacher-1",
  classGradeId: "class-1",
  subjectId: "subject-1",
  weeklyPeriods: 5,
  assignmentStatus: "ACTIVE"
};

describe(
  "teacherAiWorkflowService",
  () => {
    beforeEach(() => {
      api.submit.mockReset();
      api.execute.mockReset();
      api.status.mockReset();
    });

    it(
      "submits and executes using the same governed teacher context",
      async () => {
        api.submit.mockResolvedValue({
          requestId: "request-1",
          status: "ACTIVE"
        });

        api.execute.mockResolvedValue({
          requestId: "request-1",
          status: "SUCCEEDED",
          outputReference:
            "eds://teacher-output-1"
        });

        const result =
          await runTeacherAiRequest(
            context,
            "teacher-assistant",
            "Prepare a lesson on motion."
          );

        expect(api.submit)
          .toHaveBeenCalledWith({
            teacherProfileId: "teacher-1",
            assignmentId: "assignment-1",
            modelCode: "teacher-assistant",
            input: "Prepare a lesson on motion."
          });

        expect(api.execute)
          .toHaveBeenCalledWith(
            "request-1",
            {
              teacherProfileId: "teacher-1",
              assignmentId: "assignment-1",
              input: "Prepare a lesson on motion."
            }
          );

        expect(result.execution.status)
          .toBe("SUCCEEDED");

        expect(result.context)
          .toEqual(context);
      }
    );

    it(
      "does not execute when submission fails",
      async () => {
        api.submit.mockRejectedValue(
          new Error("Submission failed")
        );

        await expect(
          runTeacherAiRequest(
            context,
            "teacher-assistant",
            "Prepare revision questions."
          )
        ).rejects.toThrow(
          "Submission failed"
        );

        expect(api.execute)
          .not.toHaveBeenCalled();
      }
    );

    it(
      "loads status using the same teacher and assignment context",
      async () => {
        const expected = {
          requestId: "request-1",
          status: "SUCCEEDED",
          outputReference:
            "eds://teacher-output-1"
        };

        api.status.mockResolvedValue(
          expected
        );

        await expect(
          loadTeacherAiStatus(
            context,
            "request-1"
          )
        ).resolves.toEqual(expected);

        expect(api.status)
          .toHaveBeenCalledWith(
            "request-1",
            {
              teacherProfileId: "teacher-1",
              assignmentId: "assignment-1"
            }
          );
      }
    );
  }
);
