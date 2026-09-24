export interface ApiResponse<T> {
  success: boolean;
  code: string;
  message: string;
  data: T;
  errors: unknown[];
  metadata: {
    correlationId: string;
    tenantId?: string;
    timestamp: string;
  };
}


export interface Curriculum {
  id: string;
  tenantId: string;
  curriculumCode: string;
  curriculumName: string;
  curriculumType: string;
  curriculumStatus: string;
  status?: string;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
  version?: number;
}


export interface CreateCurriculumRequest {
  curriculumCode: string;
  curriculumName: string;
  curriculumType: string;
}


export interface CurriculumVersion {
  id: string;
  tenantId: string;
  curriculum: Curriculum;
  versionCode: string;
  versionName?: string | null;
  effectiveFrom: string;
  versionStatus: string;
  status?: string;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
  version?: number;
}


export interface CreateCurriculumVersionRequest {
  curriculumId: string;
  versionCode: string;
  versionName: string;
  effectiveFrom: string;
}


export interface ApproveCurriculumVersionRequest {
  curriculumId: string;
  versionCode: string;
  approvalReference: string;
}


export interface CurriculumClassGrade {
  id: string;
  tenantId: string;
  curriculumVersion: CurriculumVersion;
  classGradeId: string;
  sequenceNumber: number;
  minimumAge?: number | null;
  maximumAge?: number | null;
  mandatoryStage: boolean;
  status?: string;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
  version?: number;
}


export interface CreateCurriculumClassGradeRequest {
  curriculumVersionId: string;
  classGradeId: string;
  sequenceNumber: number;
}


export interface CurriculumLearningArea {
  id: string;
  tenantId: string;
  curriculumVersionId: string;
  learningAreaCode: string;
  learningAreaName: string;
  learningAreaType: string;
  description?: string | null;
  sequenceNumber: number;
  status: string;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
  version?: number;
}


export interface CreateCurriculumLearningAreaRequest {
  curriculumVersionId: string;
  learningAreaCode: string;
  learningAreaName: string;
  learningAreaType: string;
  description?: string;
  sequenceNumber: number;
}


export interface SubjectCatalogue {
  id: string;
  tenantId: string;
  curriculumVersionId: string;
  learningAreaId: string;
  subjectCode: string;
  subjectName: string;
  subjectType: string;
  description?: string | null;
  sequenceNumber: number;
  status: string;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
  version?: number;
}


export interface CreateSubjectCatalogueRequest {
  curriculumVersionId: string;
  learningAreaId: string;
  subjectCode: string;
  subjectName: string;
  subjectType: string;
  description?: string;
  sequenceNumber: number;
}

export interface CurriculumSubject {
  id: string;
  tenantId: string;
  curriculumVersion: CurriculumVersion;
  classGradeId: string;
  subjectId: string;
  subjectRequirement: string;
  recommendedWeeklyPeriods?: number | null;
  creditValue?: number | null;
  passMark?: number | null;
  status?: string;
  createdAt?: string;
  createdBy?: string;
  updatedAt?: string;
  updatedBy?: string;
  version?: number;
}


export interface CreateCurriculumSubjectRequest {
  curriculumVersionId: string;
  classGradeId: string;
  subjectId: string;
}


export interface ChangeCurriculumSubjectRequirementRequest {
  curriculumVersionId: string;
  classGradeId: string;
  subjectId: string;
  requirement: string;
}
