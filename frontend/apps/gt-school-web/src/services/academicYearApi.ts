import apiClient from "./apiClient";

import {
  getCurrentUser
} from "../auth/authService";

import type {
  AcademicYear,
  ApiResponse,
  CreateAcademicYearRequest
} from "../types/academicYear";


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


export async function getAcademicYears()
  : Promise<ApiResponse<AcademicYear[]>> {

  const tenantId =
    requireTenantId();


  return apiClient.get<
    ApiResponse<AcademicYear[]>
  >(
    `/api/v1/school/academic/years/tenant/${encodeURIComponent(
      tenantId
    )}`
  );

}


export async function createAcademicYear(
  request: CreateAcademicYearRequest
): Promise<ApiResponse<AcademicYear>> {

  const tenantId =
    requireTenantId();


  return apiClient.post<
    ApiResponse<AcademicYear>
  >(
    `/api/v1/school/academic/years?tenantId=${encodeURIComponent(
      tenantId
    )}`,
    request
  );

}
