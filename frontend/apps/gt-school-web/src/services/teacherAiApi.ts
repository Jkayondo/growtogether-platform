import apiClient from "./apiClient";

import type {
  TeacherAiExecutionRequest,
  TeacherAiRequestContext,
  TeacherAiRequestStatus,
  TeacherAiSubmission,
  TeacherAiSubmissionRequest
} from "../types/teacherAi";

const TEACHER_AI_BASE =
  "/api/v1/school/teacher/ai";

export function submitTeacherAiRequest(
  request: TeacherAiSubmissionRequest
): Promise<TeacherAiSubmission> {
  return apiClient.post<TeacherAiSubmission>(
    `${TEACHER_AI_BASE}/requests`,
    request
  );
}

export function executeTeacherAiRequest(
  requestId: string,
  request: TeacherAiExecutionRequest
): Promise<TeacherAiRequestStatus> {
  return apiClient.post<TeacherAiRequestStatus>(
    `${TEACHER_AI_BASE}/requests/${encodeURIComponent(
      requestId
    )}/execute`,
    request
  );
}

export function getTeacherAiRequestStatus(
  requestId: string,
  context: TeacherAiRequestContext
): Promise<TeacherAiRequestStatus> {
  const params =
    new URLSearchParams({
      teacherProfileId:
        context.teacherProfileId,
      assignmentId:
        context.assignmentId
    });

  return apiClient.get<TeacherAiRequestStatus>(
    `${TEACHER_AI_BASE}/requests/${encodeURIComponent(
      requestId
    )}?${params.toString()}`
  );
}
