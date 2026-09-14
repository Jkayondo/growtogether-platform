export interface TeacherAiSubmissionRequest {
  teacherProfileId: string;
  assignmentId: string;
  modelCode: string;
  input: string;
}

export interface TeacherAiExecutionRequest {
  teacherProfileId: string;
  assignmentId: string;
  input: string;
}

export interface TeacherAiSubmission {
  requestId: string;
  status: string;
}

export interface TeacherAiRequestStatus {
  requestId: string;
  status: string;
  outputReference: string | null;
}

export interface TeacherAiRequestContext {
  teacherProfileId: string;
  assignmentId: string;
}

export interface TeacherAiAssignmentContext {
  assignmentId: string;
  teacherProfileId: string;
  classGradeId: string;
  subjectId: string;
  weeklyPeriods: number;
  assignmentStatus: string;
}

export interface TeacherAiRunResult {
  context: TeacherAiAssignmentContext;
  submission: TeacherAiSubmission;
  execution: TeacherAiRequestStatus;
}
