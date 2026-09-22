import apiClient from "./apiClient";

import type {
  AssessmentPlan,
  CreateAssessmentPlanRequest
} from "../types/assessmentPlan";


export async function createAssessmentPlan(
  request: CreateAssessmentPlanRequest
): Promise<AssessmentPlan> {

  return apiClient.post<AssessmentPlan>(
    "/api/v1/school/assessment-plans",
    request
  );

}

export async function getAssessmentPlan(
  assessmentPlanId: string
): Promise<AssessmentPlan> {

  return apiClient.get<AssessmentPlan>(
    `/api/v1/school/assessment-plans/${assessmentPlanId}`
  );

}



export async function getAssessmentPlansByAcademicYear(
  academicYearId: string
): Promise<AssessmentPlan[]> {

  return apiClient.get<AssessmentPlan[]>(
    `/api/v1/school/assessment-plans/academic-year/${academicYearId}`
  );

}

export async function getAssessmentPlansByCampus(
  campusId: string
): Promise<AssessmentPlan[]> {

  return apiClient.get<AssessmentPlan[]>(
    `/api/v1/school/assessment-plans/campus/${campusId}`
  );

}

export async function getAssessmentPlansByClassGrade(
  classGradeId: string
): Promise<AssessmentPlan[]> {

  return apiClient.get<AssessmentPlan[]>(
    `/api/v1/school/assessment-plans/grade/${classGradeId}`
  );

}
