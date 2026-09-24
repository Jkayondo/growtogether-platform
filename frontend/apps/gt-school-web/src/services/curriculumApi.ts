import apiClient from "./apiClient";

import {
  getCurrentUser
} from "../auth/authService";

import type {
  ApiResponse,
  ApproveCurriculumVersionRequest,
  CreateCurriculumClassGradeRequest,
  CreateCurriculumLearningAreaRequest,
  CreateCurriculumRequest,
  CreateCurriculumVersionRequest,
  ChangeCurriculumSubjectRequirementRequest,
  CreateCurriculumSubjectRequest,
  CreateSubjectCatalogueRequest,
  Curriculum,
  CurriculumClassGrade,
  CurriculumLearningArea,
  CurriculumSubject,
  CurriculumVersion,
  SubjectCatalogue
} from "../types/curriculum";


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


export async function getCurricula()
  : Promise<ApiResponse<Curriculum[]>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.get<
    ApiResponse<Curriculum[]>
  >(
    `/api/v1/school/academic/curriculum?${params.toString()}`
  );

}


export async function createCurriculum(
  request: CreateCurriculumRequest
): Promise<ApiResponse<Curriculum>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId,
      curriculumCode:
        request.curriculumCode,
      curriculumName:
        request.curriculumName,
      curriculumType:
        request.curriculumType
    });

  return apiClient.post<
    ApiResponse<Curriculum>
  >(
    `/api/v1/school/academic/curriculum?${params.toString()}`
  );

}


export async function activateCurriculum(
  curriculumCode: string
): Promise<ApiResponse<Curriculum>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.patch<
    ApiResponse<Curriculum>
  >(
    `/api/v1/school/academic/curriculum/${encodeURIComponent(
      curriculumCode
    )}/activate?${params.toString()}`
  );

}


export async function getCurriculumVersions(
  curriculumId: string
): Promise<ApiResponse<CurriculumVersion[]>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.get<
    ApiResponse<CurriculumVersion[]>
  >(
    `/api/v1/school/academic/curriculum/${encodeURIComponent(
      curriculumId
    )}/versions?${params.toString()}`
  );

}


export async function createCurriculumVersion(
  request: CreateCurriculumVersionRequest
): Promise<ApiResponse<CurriculumVersion>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId,
      versionCode:
        request.versionCode,
      versionName:
        request.versionName,
      effectiveFrom:
        request.effectiveFrom
    });

  return apiClient.post<
    ApiResponse<CurriculumVersion>
  >(
    `/api/v1/school/academic/curriculum/${encodeURIComponent(
      request.curriculumId
    )}/versions?${params.toString()}`
  );

}


export async function approveCurriculumVersion(
  request: ApproveCurriculumVersionRequest
): Promise<ApiResponse<CurriculumVersion>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId,
      approvalReference:
        request.approvalReference
    });

  return apiClient.patch<
    ApiResponse<CurriculumVersion>
  >(
    `/api/v1/school/academic/curriculum/${encodeURIComponent(
      request.curriculumId
    )}/versions/${encodeURIComponent(
      request.versionCode
    )}/approve?${params.toString()}`
  );

}


export async function activateCurriculumVersion(
  curriculumId: string,
  versionCode: string
): Promise<ApiResponse<CurriculumVersion>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.patch<
    ApiResponse<CurriculumVersion>
  >(
    `/api/v1/school/academic/curriculum/${encodeURIComponent(
      curriculumId
    )}/versions/${encodeURIComponent(
      versionCode
    )}/activate?${params.toString()}`
  );

}


export async function getCurriculumClassGrades(
  curriculumVersionId: string
): Promise<CurriculumClassGrade[]> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.get<
    CurriculumClassGrade[]
  >(
    `/api/v1/school/academic/curriculum-version/${encodeURIComponent(
      curriculumVersionId
    )}/grades?${params.toString()}`
  );

}


export async function createCurriculumClassGrade(
  request: CreateCurriculumClassGradeRequest
): Promise<CurriculumClassGrade> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId,
      classGradeId:
        request.classGradeId,
      sequenceNumber:
        String(
          request.sequenceNumber
        )
    });

  return apiClient.post<
    CurriculumClassGrade
  >(
    `/api/v1/school/academic/curriculum-version/${encodeURIComponent(
      request.curriculumVersionId
    )}/grades?${params.toString()}`
  );

}


export async function archiveCurriculumClassGrade(
  curriculumVersionId: string,
  classGradeId: string
): Promise<CurriculumClassGrade> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.patch<
    CurriculumClassGrade
  >(
    `/api/v1/school/academic/curriculum-version/${encodeURIComponent(
      curriculumVersionId
    )}/grades/${encodeURIComponent(
      classGradeId
    )}/archive?${params.toString()}`
  );

}


export async function getCurriculumLearningAreas(
  curriculumVersionId: string
): Promise<ApiResponse<CurriculumLearningArea[]>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.get<
    ApiResponse<CurriculumLearningArea[]>
  >(
    `/api/v1/school/academic/curriculum/${encodeURIComponent(
      curriculumVersionId
    )}/learning-areas?${params.toString()}`
  );

}


export async function createCurriculumLearningArea(
  request: CreateCurriculumLearningAreaRequest
): Promise<ApiResponse<CurriculumLearningArea>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId,
      learningAreaCode:
        request.learningAreaCode,
      learningAreaName:
        request.learningAreaName,
      learningAreaType:
        request.learningAreaType,
      sequenceNumber:
        String(
          request.sequenceNumber
        )
    });

  if (request.description) {
    params.set(
      "description",
      request.description
    );
  }

  return apiClient.post<
    ApiResponse<CurriculumLearningArea>
  >(
    `/api/v1/school/academic/curriculum/${encodeURIComponent(
      request.curriculumVersionId
    )}/learning-areas?${params.toString()}`
  );

}


export async function activateCurriculumLearningArea(
  curriculumVersionId: string,
  learningAreaCode: string
): Promise<ApiResponse<CurriculumLearningArea>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.patch<
    ApiResponse<CurriculumLearningArea>
  >(
    `/api/v1/school/academic/curriculum/${encodeURIComponent(
      curriculumVersionId
    )}/learning-areas/${encodeURIComponent(
      learningAreaCode
    )}/activate?${params.toString()}`
  );

}


export async function deactivateCurriculumLearningArea(
  curriculumVersionId: string,
  learningAreaCode: string
): Promise<ApiResponse<CurriculumLearningArea>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.patch<
    ApiResponse<CurriculumLearningArea>
  >(
    `/api/v1/school/academic/curriculum/${encodeURIComponent(
      curriculumVersionId
    )}/learning-areas/${encodeURIComponent(
      learningAreaCode
    )}/deactivate?${params.toString()}`
  );

}


export async function getSubjectCatalogue(
  curriculumVersionId: string
): Promise<ApiResponse<SubjectCatalogue[]>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.get<
    ApiResponse<SubjectCatalogue[]>
  >(
    `/api/v1/school/academic/curriculum/${encodeURIComponent(
      curriculumVersionId
    )}/subject-catalogue?${params.toString()}`
  );

}


export async function createSubjectCatalogue(
  request: CreateSubjectCatalogueRequest
): Promise<ApiResponse<SubjectCatalogue>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId,
      learningAreaId:
        request.learningAreaId,
      subjectCode:
        request.subjectCode,
      subjectName:
        request.subjectName,
      subjectType:
        request.subjectType,
      sequenceNumber:
        String(
          request.sequenceNumber
        )
    });

  if (request.description) {
    params.set(
      "description",
      request.description
    );
  }

  return apiClient.post<
    ApiResponse<SubjectCatalogue>
  >(
    `/api/v1/school/academic/curriculum/${encodeURIComponent(
      request.curriculumVersionId
    )}/subject-catalogue?${params.toString()}`
  );

}


export async function activateSubjectCatalogueEntry(
  curriculumVersionId: string,
  subjectCode: string
): Promise<ApiResponse<SubjectCatalogue>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.patch<
    ApiResponse<SubjectCatalogue>
  >(
    `/api/v1/school/academic/curriculum/${encodeURIComponent(
      curriculumVersionId
    )}/subject-catalogue/${encodeURIComponent(
      subjectCode
    )}/activate?${params.toString()}`
  );

}


export async function deactivateSubjectCatalogueEntry(
  curriculumVersionId: string,
  subjectCode: string
): Promise<ApiResponse<SubjectCatalogue>> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.patch<
    ApiResponse<SubjectCatalogue>
  >(
    `/api/v1/school/academic/curriculum/${encodeURIComponent(
      curriculumVersionId
    )}/subject-catalogue/${encodeURIComponent(
      subjectCode
    )}/deactivate?${params.toString()}`
  );

}

export async function getCurriculumSubjects(
  curriculumVersionId: string,
  classGradeId: string
): Promise<CurriculumSubject[]> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId
    });

  return apiClient.get<
    CurriculumSubject[]
  >(
    `/api/v1/school/academic/curriculum-version/${encodeURIComponent(
      curriculumVersionId
    )}/grades/${encodeURIComponent(
      classGradeId
    )}/subjects?${params.toString()}`
  );

}


export async function createCurriculumSubject(
  request: CreateCurriculumSubjectRequest
): Promise<CurriculumSubject> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId,
      subjectId:
        request.subjectId
    });

  return apiClient.post<
    CurriculumSubject
  >(
    `/api/v1/school/academic/curriculum-version/${encodeURIComponent(
      request.curriculumVersionId
    )}/grades/${encodeURIComponent(
      request.classGradeId
    )}/subjects?${params.toString()}`
  );

}


export async function changeCurriculumSubjectRequirement(
  request: ChangeCurriculumSubjectRequirementRequest
): Promise<CurriculumSubject> {

  const tenantId =
    requireTenantId();

  const params =
    new URLSearchParams({
      tenantId,
      requirement:
        request.requirement
    });

  return apiClient.patch<
    CurriculumSubject
  >(
    `/api/v1/school/academic/curriculum-version/${encodeURIComponent(
      request.curriculumVersionId
    )}/grades/${encodeURIComponent(
      request.classGradeId
    )}/subjects/${encodeURIComponent(
      request.subjectId
    )}/requirement?${params.toString()}`
  );

}
