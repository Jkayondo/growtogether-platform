import apiClient from "./apiClient";
import type { DashboardData } from "../types/dashboard";


export const getDashboardFromApi = async (): Promise<DashboardData> => {

  return apiClient.get<DashboardData>(
    "/api/v1/dashboard"
  );

};
