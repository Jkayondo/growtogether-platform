import apiClient from "./apiClient";

import {
  getCurrentUser
} from "../auth/authService";

import type {
  ApiResponse,
  EducationLevel
} from "../types/educationLevel";


function requireTenantId() {

  const user =
    getCurrentUser();


  const tenantId =
    user?.organisationId;


  if (!tenantId) {

    throw new Error(
      "An authenticated tenant is required."
    );

  }


  return tenantId;

}


export async function getEducationLevels()
  : Promise<ApiResponse<EducationLevel[]>> {

  const tenantId =
    requireTenantId();


  const params =
    new URLSearchParams({
      tenantId
    });


  return apiClient.get<
    ApiResponse<EducationLevel[]>
  >(
    `/api/v1/school/academic/education-levels?${params.toString()}`
  );

}
