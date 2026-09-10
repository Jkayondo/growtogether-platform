export interface EducationLevel {

  id: string;

  tenantId: string;

  levelCode: string;

  levelName: string;

  description?: string | null;

  sequenceNumber: number;

  status: string;

  createdAt?: string;

  createdBy?: string;

  updatedAt?: string;

  updatedBy?: string;

  version?: number;

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
