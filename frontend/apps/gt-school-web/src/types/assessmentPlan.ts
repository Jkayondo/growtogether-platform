export interface AssessmentPlan {
  id?: string;

  planCode: string;
  planName: string;
  description?: string | null;

  academicYearId: string;
  academicTermId?: string | null;

  campusId: string;

  academicProgrammeId?: string | null;
  studyTrackId?: string | null;
  curriculumVersionId?: string | null;

  classGradeId: string;
  streamId?: string | null;

  gradingSchemeId?: string | null;

  effectiveFrom: string;
  effectiveTo?: string | null;

  workflowInstanceId?: string | null;

  approvedAt?: string | null;
  approvedBy?: string | null;

  planStatus: string;
}


export interface CreateAssessmentPlanRequest {
  planCode: string;
  planName: string;
  description?: string;

  academicYearId: string;
  academicTermId?: string;

  campusId: string;

  academicProgrammeId?: string;
  studyTrackId?: string;
  curriculumVersionId?: string;

  classGradeId: string;
  streamId?: string;

  gradingSchemeId?: string;

  effectiveFrom: string;
  effectiveTo?: string;

  workflowInstanceId?: string;
}
