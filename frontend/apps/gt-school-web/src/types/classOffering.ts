export interface ClassOffering {

  id: string;

  tenantId: string;

  offeringCode: string;

  academicYearId: string;

  campusId: string;

  academicProgrammeId?: string | null;

  studyTrackId?: string | null;

  curriculumVersionId?: string | null;

  classGradeId: string;

  plannedCapacity?: number | null;

  minimumEnrollment?: number | null;

  maximumEnrollment?: number | null;

  enrollmentOpenDate?: string | null;

  enrollmentCloseDate?: string | null;

  offeringStatus: string;

  status: string;

  createdAt?: string;

  createdBy?: string;

  updatedAt?: string;

  updatedBy?: string;

  version?: number;

}


export interface CreateClassOfferingRequest {

  offeringCode: string;

  academicYearId: string;

  campusId: string;

  academicProgrammeId?: string;

  studyTrackId?: string;

  curriculumVersionId?: string;

  classGradeId: string;

  plannedCapacity?: number;

  minimumEnrollment?: number;

  maximumEnrollment?: number;

  enrollmentOpenDate?: string;

  enrollmentCloseDate?: string;

}


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
