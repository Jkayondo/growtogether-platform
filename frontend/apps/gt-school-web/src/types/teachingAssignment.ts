export interface TeachingAssignment {

  id: string;

  tenantId: string;

  assignmentReference: string;

  teacherProfileId: string;

  ewfAssignmentId?: string | null;

  academicYearId: string;

  academicTermId?: string | null;

  campusId: string;

  classGradeId: string;

  streamId?: string | null;

  subjectId: string;

  assignmentType: string;

  weeklyPeriods: number;

  workloadPercentage?: number | null;

  effectiveFrom: string;

  effectiveTo?: string | null;

  roomReference?: string | null;

  timetableReference?: string | null;

  workflowInstanceId?: string | null;

  approvedAt?: string | null;

  approvedBy?: string | null;

  assignmentStatus: string;

  status: string;

  createdAt?: string;

  createdBy?: string;

  updatedAt?: string;

  updatedBy?: string;

  version?: number;

}


export interface CreateTeachingAssignmentRequest {

  assignmentReference: string;

  teacherProfileId: string;

  academicYearId: string;

  campusId: string;

  classGradeId: string;

  subjectId: string;

  assignmentType?: string;

  weeklyPeriods: number;

  workloadPercentage?: number;

  effectiveFrom: string;

  effectiveTo?: string;

  roomReference?: string;

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
