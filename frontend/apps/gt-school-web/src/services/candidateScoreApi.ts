import apiClient from "./apiClient";

import type {
  CandidateScore,
  CreateCandidateScoreRequest,
  EnterCandidateScoreRequest
} from "../types/candidateScore";


const BASE =
  "/api/v1/school/candidate-scores";


export async function createCandidateScore(
  request: CreateCandidateScoreRequest
): Promise<CandidateScore> {

  return apiClient.post<CandidateScore>(
    BASE,
    request
  );
}


export async function getCandidateScore(
  candidateScoreId: string
): Promise<CandidateScore> {

  return apiClient.get<CandidateScore>(
    `${BASE}/${encodeURIComponent(candidateScoreId)}`
  );
}


export async function getCandidateScoreByMarkSheetAndStudent(
  markSheetId: string,
  studentId: string
): Promise<CandidateScore> {

  return apiClient.get<CandidateScore>(
    `${BASE}/mark-sheet/${encodeURIComponent(markSheetId)}`
    + `/student/${encodeURIComponent(studentId)}`
  );
}


export async function enterCandidateScore(
  candidateScoreId: string,
  request: EnterCandidateScoreRequest
): Promise<CandidateScore> {

  return apiClient.post<CandidateScore>(
    `${BASE}/${encodeURIComponent(candidateScoreId)}/score`,
    request
  );
}


export async function markCandidateScoreAbsent(
  candidateScoreId: string
): Promise<CandidateScore> {

  return apiClient.post<CandidateScore>(
    `${BASE}/${encodeURIComponent(candidateScoreId)}/absent`,
    {}
  );
}
