import apiClient from "./apiClient";

import type {
  LearnerWorkspaceView
} from "../types/learnerWorkspace";

const LEARNER_SELF_ENDPOINT =
  "/api/school/learner/intelligence/me";

export async function getLearnerWorkspace():
Promise<LearnerWorkspaceView> {
  return apiClient.get<LearnerWorkspaceView>(
    LEARNER_SELF_ENDPOINT
  );
}
