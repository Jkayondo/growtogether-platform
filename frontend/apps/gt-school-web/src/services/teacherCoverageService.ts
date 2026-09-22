import {
  completeMyTeacherCoverage,
  getMyTeacherCoverage,
  getMyTeacherCoverageForAssignment,
  markMyTeacherCoverageAheadOfSchedule,
  markMyTeacherCoverageInProgress,
  markMyTeacherCoverageRequiresRemediation
} from "./teacherCoverageApi";

import type {
  TeacherCoverageItem,
  TeacherCoverageStatus,
  TeacherCoverageView
} from "../types/teacherCoverage";


const COVERAGE_STATUSES:
  ReadonlySet<TeacherCoverageStatus> =
    new Set<TeacherCoverageStatus>([
      "NOT_STARTED",
      "IN_PROGRESS",
      "COMPLETED",
      "REQUIRES_REMEDIATION",
      "AHEAD_OF_SCHEDULE"
    ]);


function isRecord(
  value: unknown
): value is Record<string, unknown> {

  return (
    typeof value === "object"
    && value !== null
  );
}


function requireIdentifier(
  value: string,
  field: string
): string {

  const normalized =
    value.trim();

  if (!normalized) {
    throw new Error(
      `${field} is required.`
    );
  }

  return normalized;
}


function isCoverageItem(
  value: unknown
): value is TeacherCoverageItem {

  if (!isRecord(value)) {
    return false;
  }

  return (
    typeof value.id === "string"
    && value.id.trim() !== ""
    && typeof value.teacherProfileId === "string"
    && value.teacherProfileId.trim() !== ""
    && typeof value.teachingAssignmentId === "string"
    && value.teachingAssignmentId.trim() !== ""
    && typeof value.academicYearId === "string"
    && value.academicYearId.trim() !== ""
    && typeof value.curriculumVersionId === "string"
    && value.curriculumVersionId.trim() !== ""
    && typeof value.classGradeId === "string"
    && value.classGradeId.trim() !== ""
    && typeof value.coverageType === "string"
    && typeof value.coverageItem === "string"
    && value.coverageItem.trim() !== ""
    && typeof value.coverageStatus === "string"
    && COVERAGE_STATUSES.has(
      value.coverageStatus as TeacherCoverageStatus
    )
    && (
      value.academicTermId === null
      || typeof value.academicTermId === "string"
    )
    && (
      value.curriculumSubjectId === null
      || typeof value.curriculumSubjectId === "string"
    )
    && (
      value.plannedWeek === null
      || typeof value.plannedWeek === "number"
    )
    && (
      value.completionDate === null
      || typeof value.completionDate === "string"
    )
    && (
      value.teacherRemarks === null
      || typeof value.teacherRemarks === "string"
    )
  );
}


function requireCoverageItem(
  value: unknown
): TeacherCoverageItem {

  if (!isCoverageItem(value)) {
    throw new Error(
      "Invalid teacher coverage item response."
    );
  }

  return value;
}


function requireCoverageView(
  value: unknown
): TeacherCoverageView {

  if (!isRecord(value)) {
    throw new Error(
      "Invalid teacher coverage response."
    );
  }

  if (
    typeof value.teacherProfileId !== "string"
    || value.teacherProfileId.trim() === ""
  ) {
    throw new Error(
      "Invalid teacher coverage response."
    );
  }

  if (
    value.teachingAssignmentId !== null
    && typeof value.teachingAssignmentId !== "string"
  ) {
    throw new Error(
      "Invalid teacher coverage response."
    );
  }

  if (
    !isRecord(value.summary)
    || typeof value.summary.total !== "number"
    || typeof value.summary.notStarted !== "number"
    || typeof value.summary.inProgress !== "number"
    || typeof value.summary.completed !== "number"
    || typeof value.summary.requiresRemediation !== "number"
    || typeof value.summary.aheadOfSchedule !== "number"
  ) {
    throw new Error(
      "Invalid teacher coverage response."
    );
  }

  if (
    !Array.isArray(value.items)
    || !value.items.every(isCoverageItem)
  ) {
    throw new Error(
      "Invalid teacher coverage response."
    );
  }

  return value as unknown as TeacherCoverageView;
}


export async function loadMyTeacherCoverage()
  : Promise<TeacherCoverageView> {

  return requireCoverageView(
    await getMyTeacherCoverage()
  );
}


export async function loadMyTeacherCoverageForAssignment(
  assignmentId: string
): Promise<TeacherCoverageView> {

  return requireCoverageView(
    await getMyTeacherCoverageForAssignment(
      requireIdentifier(
        assignmentId,
        "assignmentId"
      )
    )
  );
}


export async function setMyTeacherCoverageInProgress(
  coverageId: string
): Promise<TeacherCoverageItem> {

  return requireCoverageItem(
    await markMyTeacherCoverageInProgress(
      requireIdentifier(
        coverageId,
        "coverageId"
      )
    )
  );
}


export async function completeMyCoverage(
  coverageId: string,
  remarks?: string | null
): Promise<TeacherCoverageItem> {

  return requireCoverageItem(
    await completeMyTeacherCoverage(
      requireIdentifier(
        coverageId,
        "coverageId"
      ),
      remarks
    )
  );
}


export async function setMyTeacherCoverageRequiresRemediation(
  coverageId: string
): Promise<TeacherCoverageItem> {

  return requireCoverageItem(
    await markMyTeacherCoverageRequiresRemediation(
      requireIdentifier(
        coverageId,
        "coverageId"
      )
    )
  );
}


export async function setMyTeacherCoverageAheadOfSchedule(
  coverageId: string
): Promise<TeacherCoverageItem> {

  return requireCoverageItem(
    await markMyTeacherCoverageAheadOfSchedule(
      requireIdentifier(
        coverageId,
        "coverageId"
      )
    )
  );
}
