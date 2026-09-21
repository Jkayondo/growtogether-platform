import {
  createCandidateScore,
  enterCandidateScore,
  getCandidateScore,
  getCandidateScoreByMarkSheetAndStudent,
  markCandidateScoreAbsent
} from "./candidateScoreApi";

import type {
  CandidateScore,
  CreateCandidateScoreRequest,
  EnterCandidateScoreRequest
} from "../types/candidateScore";


export function saveCandidateScore(
  request: CreateCandidateScoreRequest
): Promise<CandidateScore> {

  return createCandidateScore(
    request
  );
}


export function loadCandidateScore(
  candidateScoreId: string
): Promise<CandidateScore> {

  return getCandidateScore(
    candidateScoreId
  );
}


export function loadCandidateScoreByMarkSheetAndStudent(
  markSheetId: string,
  studentId: string
): Promise<CandidateScore> {

  return getCandidateScoreByMarkSheetAndStudent(
    markSheetId,
    studentId
  );
}


export function recordCandidateScore(
  candidateScoreId: string,
  score: number
): Promise<CandidateScore> {

  const request: EnterCandidateScoreRequest = {
    score
  };

  return enterCandidateScore(
    candidateScoreId,
    request
  );
}


export function markScoreAbsent(
  candidateScoreId: string
): Promise<CandidateScore> {

  return markCandidateScoreAbsent(
    candidateScoreId
  );
}
