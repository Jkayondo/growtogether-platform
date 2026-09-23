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
  screen
} from "@testing-library/react";

const getLearnerWorkspace =
  vi.hoisted(
    () => vi.fn()
  );

vi.mock(
  "../../services/learnerWorkspaceApi",
  () => ({
    getLearnerWorkspace
  })
);

import LearnerWorkspace from "./LearnerWorkspace";

afterEach(cleanup);

beforeEach(
  () => {
    getLearnerWorkspace.mockReset();
  }
);

describe(
  "LearnerWorkspace",
  () => {
    it(
      "shows a learner-friendly loading state",
      () => {
        getLearnerWorkspace.mockReturnValue(
          new Promise(
            () => undefined
          )
        );

        render(
          <LearnerWorkspace />
        );

        expect(
          screen.getByRole(
            "status"
          ).textContent
        ).toContain(
          "Loading your learner workspace"
        );
      }
    );

    it(
      "renders the authenticated learner self view",
      async () => {
        getLearnerWorkspace.mockResolvedValue({
          learnerId:
            "00000000-0000-0000-0000-000000000201",
          achievementStatus:
            "ON_TRACK",
          growthLevel:
            "LOW",
          supportRequired:
            false,
          growthMessage:
            "Great work! Continue building your skills.",
          updatedAt:
            "2026-09-23T10:00:00Z"
        });

        render(
          <LearnerWorkspace />
        );

        expect(
          await screen.findByText(
            "ON_TRACK"
          )
        ).toBeTruthy();

        expect(
          screen.getByText(
            "Great work! Continue building your skills."
          )
        ).toBeTruthy();

        expect(
          getLearnerWorkspace
        ).toHaveBeenCalledTimes(
          1
        );
      }
    );

    it(
      "shows a safe error state when the self view cannot load",
      async () => {
        getLearnerWorkspace.mockRejectedValue(
          new Error(
            "Forbidden"
          )
        );

        render(
          <LearnerWorkspace />
        );

        const alert =
          await screen.findByRole(
            "alert"
          );

        expect(
          alert.textContent
        ).toContain(
          "Unable to load your learner workspace."
        );
      }
    );
  }
);
