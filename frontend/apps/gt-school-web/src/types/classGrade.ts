export interface ClassGrade {

  id: string;

  tenantId: string;

  educationLevelId: string;

  classCode: string;

  className: string;

  sequenceNumber: number;

  capacity?: number | null;

  status: string;

  createdAt?: string;

  createdBy?: string;

  updatedAt?: string;

  updatedBy?: string;

  version?: number;

}


export interface CreateClassGradeRequest {

  educationLevelId: string;

  classCode: string;

  className: string;

  sequenceNumber: number;

  capacity?: number;

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
