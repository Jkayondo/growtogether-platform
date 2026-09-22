import apiClient from "./apiClient";

import {
  getCurrentUser
} from "../auth/authService";

import type {
  ApiResponse,
  Campus,
  CreateCampusRequest
} from "../types/campus";


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


export async function getCampuses(
  schoolProfileId: string
): Promise<ApiResponse<Campus[]>> {

  const tenantId =
    requireTenantId();


  const params =
    new URLSearchParams({
      tenantId,
      schoolProfileId
    });


  return apiClient.get<
    ApiResponse<Campus[]>
  >(
    `/api/v1/school/academic/campuses?${params.toString()}`
  );

}


export async function createCampus(
  schoolProfileId: string,
  request: CreateCampusRequest
): Promise<ApiResponse<Campus>> {

  const tenantId =
    requireTenantId();


  const params =
    new URLSearchParams({
      tenantId,
      schoolProfileId,
      campusCode:
        request.campusCode,
      campusName:
        request.campusName,
      mainCampus:
        String(
          request.mainCampus
        )
    });


  const optionalValues: Array<
    [string, string | undefined]
  > = [
    [
      "addressLine",
      request.addressLine
    ],
    [
      "district",
      request.district
    ],
    [
      "city",
      request.city
    ],
    [
      "countryCode",
      request.countryCode
    ],
    [
      "phoneNumber",
      request.phoneNumber
    ],
    [
      "email",
      request.email
    ]
  ];


  for (
    const [key, value]
    of optionalValues
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


  return apiClient.post<
    ApiResponse<Campus>
  >(
    `/api/v1/school/academic/campuses?${params.toString()}`
  );

}


export async function activateCampus(
  campusCode: string
): Promise<ApiResponse<Campus>> {

  const tenantId =
    requireTenantId();


  const params =
    new URLSearchParams({
      tenantId
    });


  return apiClient.patch<
    ApiResponse<Campus>
  >(
    `/api/v1/school/academic/campuses/${encodeURIComponent(
      campusCode
    )}/activate?${params.toString()}`
  );

}


export async function deactivateCampus(
  campusCode: string
): Promise<ApiResponse<Campus>> {

  const tenantId =
    requireTenantId();


  const params =
    new URLSearchParams({
      tenantId
    });


  return apiClient.patch<
    ApiResponse<Campus>
  >(
    `/api/v1/school/academic/campuses/${encodeURIComponent(
      campusCode
    )}/deactivate?${params.toString()}`
  );

}
