import apiClient from "./apiClient";
import type { LeadershipOverviewData } from "../types/leadership";

export const getLeadershipOverview =
  async (): Promise<LeadershipOverviewData> => {
    return apiClient.get<LeadershipOverviewData>(
      "/api/v1/school/leadership/overview"
    );
  };
