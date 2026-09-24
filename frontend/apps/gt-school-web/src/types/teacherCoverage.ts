export type TeacherCoverageType =
  | "TOPIC"
  | "LEARNING_OUTCOME"
  | "COMPETENCY"
  | "THEME"
  | "ACTIVITY";


export type TeacherCoverageStatus =
  | "NOT_STARTED"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "REQUIRES_REMEDIATION"
  | "AHEAD_OF_SCHEDULE";


export interface TeacherCoverageItem {
  id: string;
  teacherProfileId: string;
  teachingAssignmentId: string;
  academicYearId: string;
  academicTermId: string | null;
  curriculumVersionId: string;
  curriculumSubjectId: string | null;
  classGradeId: string;
  coverageType: TeacherCoverageType;
  coverageItem: string;
  plannedWeek: number | null;
  coverageStatus: TeacherCoverageStatus;
  completionDate: string | null;
  teacherRemarks: string | null;
}


export interface TeacherCoverageSummary {
  total: number;
  notStarted: number;
  inProgress: number;
  completed: number;
  requiresRemediation: number;
  aheadOfSchedule: number;
}


export interface TeacherCoverageView {
  teacherProfileId: string;
  teachingAssignmentId: string | null;
  summary: TeacherCoverageSummary;
  items: TeacherCoverageItem[];
}
