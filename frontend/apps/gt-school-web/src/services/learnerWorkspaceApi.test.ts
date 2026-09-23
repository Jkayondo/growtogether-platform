import {
  beforeEach,
  describe,
  expect,
  it,
  vi
} from "vitest";

const apiGet =
  vi.hoisted(
    () => vi.fn()
  );

vi.mock(
  "./apiClient",
  () => ({
    default: {
      get: apiGet
    }
  })
);

import {
  getLearnerWorkspace
} from "./learnerWorkspaceApi";

describe(
  "learnerWorkspaceApi",
  () => {
    beforeEach(
      () => {
        apiGet.mockReset();
      }
    );

    it(
      "uses only the authenticated learner self endpoint",
      async () => {
        const response = {
          learnerId:
            "00000000-0000-0000-0000-000000000101",
          achievementStatus:
            "ON_TRACK",
          growthLevel:
            "LOW",
          supportRequired:
            false,
          growthMessage:
            "Keep learning.",
          updatedAt:
            "2026-09-23T10:00:00Z"
        };

        apiGet.mockResolvedValue(
          response
        );

        await expect(
          getLearnerWorkspace()
        ).resolves.toEqual(
          response
        );

        expect(
          apiGet
        ).toHaveBeenCalledTimes(
          1
        );

        expect(
          apiGet
        ).toHaveBeenCalledWith(
          "/api/school/learner/intelligence/me"
        );

        const endpoint =
          String(
            apiGet.mock.calls[0][0]
          );

        expect(
          endpoint
        ).not.toContain(
          "learnerId="
        );

        expect(
          endpoint
        ).not.toContain(
          "tenantId="
        );

        expect(
          endpoint
        ).not.toMatch(
          /\/[0-9a-f]{8}-[0-9a-f-]{27,}$/i
        );
      }
    );
  }
);
