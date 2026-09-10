import apiClient from "./apiClient";

import {
  getCurrentUser
} from "../auth/authService";

import type {
  ApiResponse,
  CreateTeachingAssignmentRequest,
  TeachingAssignment
} from "../types/teachingAssignment";


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


function requireCurrentUserId() {

  const user =
    getCurrentUser();

  const userId =
    user?.id;

  if (!userId) {

    throw new Error(
      "An authenticated user is required."
    );

  }

  return userId;

}


export async function getTeachingAssignmentsByStatus(
  assignmentStatus: string
): Promise<ApiResponse<TeachingAssignment[]>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.get<
    ApiResponse<TeachingAssignment[]>
  >(
    `/api/v1/school/academic/teaching-assignments/status/${encodeURIComponent(
      assignmentStatus
    )}?${params.toString()}`
  );

}


export async function getTeachingAssignmentById(
  assignmentId: string
): Promise<ApiResponse<TeachingAssignment>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.get<
    ApiResponse<TeachingAssignment>
  >(
    `/api/v1/school/academic/teaching-assignments/${encodeURIComponent(
      assignmentId
    )}?${params.toString()}`
  );

}


export async function createTeachingAssignment(
  request: CreateTeachingAssignmentRequest
): Promise<ApiResponse<TeachingAssignment>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId,
      assignmentReference:
        request.assignmentReference,
      teacherProfileId:
        request.teacherProfileId,
      academicYearId:
        request.academicYearId,
      campusId:
        request.campusId,
      classGradeId:
        request.classGradeId,
      subjectId:
        request.subjectId,
      weeklyPeriods:
        String(request.weeklyPeriods),
      effectiveFrom:
        request.effectiveFrom
    });


  if (
    request.assignmentType &&
    request.assignmentType.trim() !== ""
  ) {

    params.set(
      "assignmentType",
      request.assignmentType
    );

  }


  if (
    request.workloadPercentage !== undefined
  ) {

    params.set(
      "workloadPercentage",
      String(
        request.workloadPercentage
      )
    );

  }


  if (
    request.effectiveTo &&
    request.effectiveTo.trim() !== ""
  ) {

    params.set(
      "effectiveTo",
      request.effectiveTo
    );

  }


  if (
    request.roomReference &&
    request.roomReference.trim() !== ""
  ) {

    params.set(
      "roomReference",
      request.roomReference
    );

  }


  return apiClient.post<
    ApiResponse<TeachingAssignment>
  >(
    `/api/v1/school/academic/teaching-assignments?${params.toString()}`
  );

}


export async function requestTeachingAssignmentApproval(
  assignmentId: string
): Promise<ApiResponse<TeachingAssignment>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.patch<
    ApiResponse<TeachingAssignment>
  >(
    `/api/v1/school/academic/teaching-assignments/${encodeURIComponent(
      assignmentId
    )}/request-approval?${params.toString()}`
  );

}


export async function activateTeachingAssignment(
  assignmentId: string
): Promise<ApiResponse<TeachingAssignment>> {

  const tenantId =
    requireTenantId();

  const approvedBy =
    requireCurrentUserId();

  const params =
    new URLSearchParams({
      tenantId,
      approvedBy
    });

  return apiClient.patch<
    ApiResponse<TeachingAssignment>
  >(
    `/api/v1/school/academic/teaching-assignments/${encodeURIComponent(
      assignmentId
    )}/activate?${params.toString()}`
  );

}


export async function suspendTeachingAssignment(
  assignmentId: string
): Promise<ApiResponse<TeachingAssignment>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.patch<
    ApiResponse<TeachingAssignment>
  >(
    `/api/v1/school/academic/teaching-assignments/${encodeURIComponent(
      assignmentId
    )}/suspend?${params.toString()}`
  );

}


export async function completeTeachingAssignment(
  assignmentId: string
): Promise<ApiResponse<TeachingAssignment>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.patch<
    ApiResponse<TeachingAssignment>
  >(
    `/api/v1/school/academic/teaching-assignments/${encodeURIComponent(
      assignmentId
    )}/complete?${params.toString()}`
  );

}


export async function cancelTeachingAssignment(
  assignmentId: string
): Promise<ApiResponse<TeachingAssignment>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.patch<
    ApiResponse<TeachingAssignment>
  >(
    `/api/v1/school/academic/teaching-assignments/${encodeURIComponent(
      assignmentId
    )}/cancel?${params.toString()}`
  );

}


export async function getMyActiveTeachingAssignments()
  : Promise<ApiResponse<TeachingAssignment[]>> {
  return apiClient.get<ApiResponse<TeachingAssignment[]>>(
    "/api/v1/school/academic/teaching-assignments/me/active"
  );
}
