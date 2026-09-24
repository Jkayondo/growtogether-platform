import apiClient from "./apiClient";

import {
  getCurrentUser
} from "../auth/authService";

import type {
  ApiResponse,
  ClassOffering,
  CreateClassOfferingRequest
} from "../types/classOffering";


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


export async function getClassOfferingsByAcademicYear(
  academicYearId: string
): Promise<ApiResponse<ClassOffering[]>> {

  const tenantId =
    requireTenantId();


  const params =
    new URLSearchParams({
      tenantId
    });


  return apiClient.get<
    ApiResponse<ClassOffering[]>
  >(
    `/api/v1/school/academic/class-offerings/year/${encodeURIComponent(
      academicYearId
    )}?${params.toString()}`
  );

}


export async function createClassOffering(
  request: CreateClassOfferingRequest
): Promise<ApiResponse<ClassOffering>> {

  const tenantId =
    requireTenantId();


  const params =
    new URLSearchParams({
      tenantId,
      offeringCode:
        request.offeringCode,
      academicYearId:
        request.academicYearId,
      campusId:
        request.campusId,
      classGradeId:
        request.classGradeId
    });


  const optionalStrings: Array<
    [string, string | undefined]
  > = [
    [
      "academicProgrammeId",
      request.academicProgrammeId
    ],
    [
      "studyTrackId",
      request.studyTrackId
    ],
    [
      "curriculumVersionId",
      request.curriculumVersionId
    ],
    [
      "enrollmentOpenDate",
      request.enrollmentOpenDate
    ],
    [
      "enrollmentCloseDate",
      request.enrollmentCloseDate
    ]
  ];


  for (
    const [key, value]
    of optionalStrings
  ) {

    if (
      value !== undefined
      && value.trim() !== ""
    ) {

      params.set(
        key,
        value.trim()
      );

    }

  }


  const optionalNumbers: Array<
    [string, number | undefined]
  > = [
    [
      "plannedCapacity",
      request.plannedCapacity
    ],
    [
      "minimumEnrollment",
      request.minimumEnrollment
    ],
    [
      "maximumEnrollment",
      request.maximumEnrollment
    ]
  ];


  for (
    const [key, value]
    of optionalNumbers
  ) {

    if (
      value !== undefined
    ) {

      params.set(
        key,
        String(value)
      );

    }

  }


  return apiClient.post<
    ApiResponse<ClassOffering>
  >(
    `/api/v1/school/academic/class-offerings?${params.toString()}`
  );

}


export async function activateClassOffering(
  offeringCode: string
): Promise<ApiResponse<ClassOffering>> {

  const tenantId =
    requireTenantId();


  const params =
    new URLSearchParams({
      tenantId
    });


  return apiClient.patch<
    ApiResponse<ClassOffering>
  >(
    `/api/v1/school/academic/class-offerings/${encodeURIComponent(
      offeringCode
    )}/activate?${params.toString()}`
  );

}


export async function deactivateClassOffering(
  offeringCode: string
): Promise<ApiResponse<ClassOffering>> {

  const tenantId =
    requireTenantId();


  const params =
    new URLSearchParams({
      tenantId
    });


  return apiClient.patch<
    ApiResponse<ClassOffering>
  >(
    `/api/v1/school/academic/class-offerings/${encodeURIComponent(
      offeringCode
    )}/deactivate?${params.toString()}`
  );

}
