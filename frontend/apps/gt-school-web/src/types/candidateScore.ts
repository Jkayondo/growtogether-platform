export interface CandidateScore {
  id?: string;
  tenantId?: string;

  markSheetId: string;
  markEntryBatchId?: string | null;

  examinationCandidateId?: string | null;
  candidatePaperRegistrationId?: string | null;

  studentId: string;
  studentEnrollmentId: string;

  rawScore?: number | null;
  adjustedScore?: number | null;
  finalScore?: number | null;

  scoreStatus: string;

  absent: boolean;
  exempted: boolean;
  missingMark: boolean;
  withheld: boolean;

  absenceReason?: string | null;
  withholdingReason?: string | null;

  enteredAt?: string | null;
  enteredBy?: string | null;

  validatedAt?: string | null;
  validatedBy?: string | null;

  approvedAt?: string | null;
  approvedBy?: string | null;

  sourceType?: string;
  sourceReference?: string | null;

  status?: string;

  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
  version?: number;
}


export interface CreateCandidateScoreRequest {
  markSheetId: string;

  examinationCandidateId?: string | null;
  candidatePaperRegistrationId?: string | null;

  studentId: string;
  studentEnrollmentId: string;
}


export interface EnterCandidateScoreRequest {
  score: number;
}
