import apiClient from "./apiClient";

import {
  getCurrentUser
} from "../auth/authService";

import type {
  SchoolProfile
} from "../types/schoolProfile";


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


export async function getCurrentSchoolProfile()
  : Promise<SchoolProfile> {

  const tenantId =
    requireTenantId();


  return apiClient.get<SchoolProfile>(
    `/api/v1/school/profiles/current?tenantId=${encodeURIComponent(
      tenantId
    )}`
  );

}
