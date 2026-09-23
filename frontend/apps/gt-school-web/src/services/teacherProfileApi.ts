import apiClient from "./apiClient";

import {
  getCurrentUser
} from "../auth/authService";

import type {
  ApiResponse,
  CreateTeacherProfileRequest,
  TeacherProfile
} from "../types/teacherProfile";


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


export async function getTeacherProfilesByStatus(
  teachingStatus: string
): Promise<ApiResponse<TeacherProfile[]>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.get<
    ApiResponse<TeacherProfile[]>
  >(
    `/api/v1/school/academic/teachers/status/${encodeURIComponent(
      teachingStatus
    )}?${params.toString()}`
  );

}


export async function getTeacherProfileByNumber(
  teacherNumber: string
): Promise<ApiResponse<TeacherProfile>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.get<
    ApiResponse<TeacherProfile>
  >(
    `/api/v1/school/academic/teachers/${encodeURIComponent(
      teacherNumber
    )}?${params.toString()}`
  );

}


export async function createTeacherProfile(
  request: CreateTeacherProfileRequest
): Promise<ApiResponse<TeacherProfile>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId,
      workforceMemberId:
        request.workforceMemberId,
      teacherNumber:
        request.teacherNumber
    });


  const optionalStrings:
    Array<
      [
        keyof CreateTeacherProfileRequest,
        string
      ]
    > = [
      [
        "teacherRegistrationNumber",
        "teacherRegistrationNumber"
      ],
      [
        "teachingLicenceNumber",
        "teachingLicenceNumber"
      ],
      [
        "teachingLicenceIssuedAt",
        "teachingLicenceIssuedAt"
      ],
      [
        "teachingLicenceExpiresAt",
        "teachingLicenceExpiresAt"
      ],
      [
        "highestTeachingLevel",
        "highestTeachingLevel"
      ],
      [
        "primarySpecialization",
        "primarySpecialization"
      ],
      [
        "secondarySpecialization",
        "secondarySpecialization"
      ],
      [
        "teacherCategory",
        "teacherCategory"
      ],
      [
        "notes",
        "notes"
      ]
    ];


  for (
    const [
      requestField,
      parameterName
    ] of optionalStrings
  ) {

    const value =
      request[requestField];

    if (
      typeof value === "string" &&
      value.trim() !== ""
    ) {

      params.set(
        parameterName,
        value
      );

    }

  }


  if (
    request.qualifiedForBoardingDuty !== undefined
  ) {

    params.set(
      "qualifiedForBoardingDuty",
      String(
        request.qualifiedForBoardingDuty
      )
    );

  }


  if (
    request.qualifiedForSpecialNeeds !== undefined
  ) {

    params.set(
      "qualifiedForSpecialNeeds",
      String(
        request.qualifiedForSpecialNeeds
      )
    );

  }


  if (
    request.qualifiedForCounselling !== undefined
  ) {

    params.set(
      "qualifiedForCounselling",
      String(
        request.qualifiedForCounselling
      )
    );

  }


  if (
    request.maximumWeeklyPeriods !== undefined
  ) {

    params.set(
      "maximumWeeklyPeriods",
      String(
        request.maximumWeeklyPeriods
      )
    );

  }


  return apiClient.post<
    ApiResponse<TeacherProfile>
  >(
    `/api/v1/school/academic/teachers?${params.toString()}`
  );

}


export async function activateTeacherProfile(
  teacherNumber: string
): Promise<ApiResponse<TeacherProfile>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.patch<
    ApiResponse<TeacherProfile>
  >(
    `/api/v1/school/academic/teachers/${encodeURIComponent(
      teacherNumber
    )}/activate?${params.toString()}`
  );

}


export async function deactivateTeacherProfile(
  teacherNumber: string
): Promise<ApiResponse<TeacherProfile>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.patch<
    ApiResponse<TeacherProfile>
  >(
    `/api/v1/school/academic/teachers/${encodeURIComponent(
      teacherNumber
    )}/deactivate?${params.toString()}`
  );

}
