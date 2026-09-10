export interface Subject {
  id: string;
  tenantId: string;
  subjectCode: string;
  subjectName: string;
  shortName?: string | null;
  subjectType: string;
  description?: string | null;
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
