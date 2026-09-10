import apiClient from "./apiClient";
import { getCurrentUser } from "../auth/authService";
import type { ApiResponse, Subject } from "../types/subject";

function requireTenantId() {
  const user = getCurrentUser();
  const tenantId = user?.organisationId;

  if (!tenantId) {
    throw new Error("An authenticated tenant is required.");
  }

  return tenantId;
}

export async function getSubjects(): Promise<ApiResponse<Subject[]>> {
  const tenantId = requireTenantId();
  const params = new URLSearchParams({ tenantId });

  return apiClient.get<ApiResponse<Subject[]>>(
    `/api/v1/school/academic/subjects?${params.toString()}`
  );
}
