import apiClient from "./apiClient";

import {
  getCurrentUser
} from "../auth/authService";

import type {
  ApiResponse,
  ClassGrade,
  CreateClassGradeRequest
} from "../types/classGrade";


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


export async function getClassGrades(
  educationLevelId: string
): Promise<ApiResponse<ClassGrade[]>> {

  const tenantId =
    requireTenantId();


  const params =
    new URLSearchParams({
      tenantId,
      educationLevelId
    });


  return apiClient.get<
    ApiResponse<ClassGrade[]>
  >(
    `/api/v1/school/academic/class-grades?${params.toString()}`
  );

}


export async function createClassGrade(
  request: CreateClassGradeRequest
): Promise<ApiResponse<ClassGrade>> {

  const tenantId =
    requireTenantId();


  const params =
    new URLSearchParams({
      tenantId,
      educationLevelId:
        request.educationLevelId,
      classCode:
        request.classCode,
      className:
        request.className,
      sequenceNumber:
        String(
          request.sequenceNumber
        )
    });


  if (
    request.capacity !== undefined
  ) {

    params.set(
      "capacity",
      String(
        request.capacity
      )
    );

  }


  return apiClient.post<
    ApiResponse<ClassGrade>
  >(
    `/api/v1/school/academic/class-grades?${params.toString()}`
  );

}


export async function activateClassGrade(
  classCode: string
): Promise<ApiResponse<ClassGrade>> {

  const tenantId =
    requireTenantId();


  const params =
    new URLSearchParams({
      tenantId
    });


  return apiClient.patch<
    ApiResponse<ClassGrade>
  >(
    `/api/v1/school/academic/class-grades/${encodeURIComponent(
      classCode
    )}/activate?${params.toString()}`
  );

}


export async function deactivateClassGrade(
  classCode: string
): Promise<ApiResponse<ClassGrade>> {

  const tenantId =
    requireTenantId();


  const params =
    new URLSearchParams({
      tenantId
    });


  return apiClient.patch<
    ApiResponse<ClassGrade>
  >(
    `/api/v1/school/academic/class-grades/${encodeURIComponent(
      classCode
    )}/deactivate?${params.toString()}`
  );

}
