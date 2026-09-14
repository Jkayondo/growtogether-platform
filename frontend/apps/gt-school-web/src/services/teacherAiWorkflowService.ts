import {
  executeTeacherAiRequest,
  getTeacherAiRequestStatus,
  submitTeacherAiRequest
} from "./teacherAiApi";

import type {
  TeacherAiAssignmentContext,
  TeacherAiRequestStatus,
  TeacherAiRunResult
} from "../types/teacherAi";

export async function runTeacherAiRequest(
  context: TeacherAiAssignmentContext,
  modelCode: string,
  input: string
): Promise<TeacherAiRunResult> {
  const submission =
    await submitTeacherAiRequest({
      teacherProfileId:
        context.teacherProfileId,
      assignmentId:
        context.assignmentId,
      modelCode,
      input
    });

  const execution =
    await executeTeacherAiRequest(
      submission.requestId,
      {
        teacherProfileId:
          context.teacherProfileId,
        assignmentId:
          context.assignmentId,
        input
      }
    );

  return {
    context,
    submission,
    execution
  };
}

export async function loadTeacherAiStatus(
  context: TeacherAiAssignmentContext,
  requestId: string
): Promise<TeacherAiRequestStatus> {
  return getTeacherAiRequestStatus(
    requestId,
    {
      teacherProfileId:
        context.teacherProfileId,
      assignmentId:
        context.assignmentId
    }
  );
}
