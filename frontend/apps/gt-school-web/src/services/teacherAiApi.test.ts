import {
  beforeEach,
  describe,
  expect,
  it,
  vi
} from "vitest";

import apiClient from "./apiClient";

import {
  executeTeacherAiRequest,
  getTeacherAiRequestStatus,
  submitTeacherAiRequest
} from "./teacherAiApi";

vi.mock("./apiClient", () => ({
  default: {
    get: vi.fn(),
    post: vi.fn()
  }
}));

describe(
  "teacherAiApi",
  () => {
    beforeEach(() => {
      vi.clearAllMocks();
    });

    it(
      "submits a governed Teacher AI request",
      async () => {
        const request = {
          teacherProfileId: "teacher-1",
          assignmentId: "assignment-1",
          modelCode: "teacher-assistant",
          input: "Prepare a lesson outline."
        };

        const response = {
          requestId: "request-1",
          status: "ACTIVE"
        };

        vi.mocked(
          apiClient.post
        ).mockResolvedValue(response);

        await expect(
          submitTeacherAiRequest(request)
        ).resolves.toEqual(response);

        expect(
          apiClient.post
        ).toHaveBeenCalledTimes(1);

        expect(
          apiClient.post
        ).toHaveBeenCalledWith(
          "/api/v1/school/teacher/ai/requests",
          request
        );
      }
    );

    it(
      "executes an existing Teacher AI request",
      async () => {
        const request = {
          teacherProfileId: "teacher-1",
          assignmentId: "assignment-1",
          input: "Prepare a lesson outline."
        };

        const response = {
          requestId: "request/with spaces",
          status: "SUCCEEDED",
          outputReference: "eds://teacher-output-1"
        };

        vi.mocked(
          apiClient.post
        ).mockResolvedValue(response);

        await expect(
          executeTeacherAiRequest(
            "request/with spaces",
            request
          )
        ).resolves.toEqual(response);

        expect(
          apiClient.post
        ).toHaveBeenCalledTimes(1);

        expect(
          apiClient.post
        ).toHaveBeenCalledWith(
          "/api/v1/school/teacher/ai/requests/request%2Fwith%20spaces/execute",
          request
        );
      }
    );

    it(
      "reads Teacher AI request status with governed context",
      async () => {
        const response = {
          requestId: "request-1",
          status: "SUCCEEDED",
          outputReference: "eds://teacher-output-1"
        };

        vi.mocked(
          apiClient.get
        ).mockResolvedValue(response);

        await expect(
          getTeacherAiRequestStatus(
            "request-1",
            {
              teacherProfileId:
                "teacher profile/1",
              assignmentId:
                "assignment & 1"
            }
          )
        ).resolves.toEqual(response);

        expect(
          apiClient.get
        ).toHaveBeenCalledTimes(1);

        expect(
          apiClient.get
        ).toHaveBeenCalledWith(
          "/api/v1/school/teacher/ai/requests/request-1?teacherProfileId=teacher+profile%2F1&assignmentId=assignment+%26+1"
        );
      }
    );

    it(
      "does not bypass the shared API client",
      () => {
        expect(
          vi.isMockFunction(
            apiClient.post
          )
        ).toBe(true);

        expect(
          vi.isMockFunction(
            apiClient.get
          )
        ).toBe(true);
      }
    );
  }
);
