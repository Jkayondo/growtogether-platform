import {
  createAssessmentPlan,
  getAssessmentPlan,
  getAssessmentPlansByAcademicYear,
  getAssessmentPlansByCampus,
  getAssessmentPlansByClassGrade
} from "./assessmentPlanApi";

import type {
  AssessmentPlan,
  CreateAssessmentPlanRequest
} from "../types/assessmentPlan";


export async function saveAssessmentPlan(
  request: CreateAssessmentPlanRequest
): Promise<AssessmentPlan> {

  return createAssessmentPlan(
    request
  );

}

export async function loadAssessmentPlan(
  assessmentPlanId: string
): Promise<AssessmentPlan> {

  return getAssessmentPlan(
    assessmentPlanId
  );

}

export async function loadAssessmentPlansByAcademicYear(
  academicYearId: string
): Promise<AssessmentPlan[]> {

  return getAssessmentPlansByAcademicYear(
    academicYearId
  );

}

export async function loadAssessmentPlansByCampus(
  campusId: string
): Promise<AssessmentPlan[]> {

  return getAssessmentPlansByCampus(
    campusId
  );

}

export async function loadAssessmentPlansByClassGrade(
  classGradeId: string
): Promise<AssessmentPlan[]> {

  return getAssessmentPlansByClassGrade(
    classGradeId
  );

}
