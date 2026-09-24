import apiClient from "./apiClient";

import type {
  TeacherCoverageItem,
  TeacherCoverageView
} from "../types/teacherCoverage";


const TEACHER_COVERAGE_ENDPOINT =
  "/api/v1/school/teacher/coverage";


function encodeIdentifier(
  value: string
): string {

  return encodeURIComponent(
    value
  );
}


export async function getMyTeacherCoverage()
  : Promise<TeacherCoverageView> {

  return apiClient.get<TeacherCoverageView>(
    TEACHER_COVERAGE_ENDPOINT
  );
}


export async function getMyTeacherCoverageForAssignment(
  assignmentId: string
): Promise<TeacherCoverageView> {

  return apiClient.get<TeacherCoverageView>(
    `${TEACHER_COVERAGE_ENDPOINT}/assignments/${encodeIdentifier(assignmentId)}`
  );
}


export async function markMyTeacherCoverageInProgress(
  coverageId: string
): Promise<TeacherCoverageItem> {

  return apiClient.patch<TeacherCoverageItem>(
    `${TEACHER_COVERAGE_ENDPOINT}/${encodeIdentifier(coverageId)}/progress`
  );
}


export async function completeMyTeacherCoverage(
  coverageId: string,
  remarks?: string | null
): Promise<TeacherCoverageItem> {

  const normalizedRemarks =
    remarks?.trim();

  const query =
    normalizedRemarks
      ? `?remarks=${encodeURIComponent(normalizedRemarks)}`
      : "";

  return apiClient.patch<TeacherCoverageItem>(
    `${TEACHER_COVERAGE_ENDPOINT}/${encodeIdentifier(coverageId)}/complete${query}`
  );
}


export async function markMyTeacherCoverageRequiresRemediation(
  coverageId: string
): Promise<TeacherCoverageItem> {

  return apiClient.patch<TeacherCoverageItem>(
    `${TEACHER_COVERAGE_ENDPOINT}/${encodeIdentifier(coverageId)}/remediation`
  );
}


export async function markMyTeacherCoverageAheadOfSchedule(
  coverageId: string
): Promise<TeacherCoverageItem> {

  return apiClient.patch<TeacherCoverageItem>(
    `${TEACHER_COVERAGE_ENDPOINT}/${encodeIdentifier(coverageId)}/ahead`
  );
}
