export interface AcademicYear {

  id: string;

  tenantId: string;

  academicYearCode: string;

  academicYearName: string;

  startDate: string;

  endDate: string;

  currentYear: boolean;

  status: string;

  createdAt?: string;

  createdBy?: string;

  updatedAt?: string;

  updatedBy?: string;

  version?: number;

}


export interface CreateAcademicYearRequest {

  academicYearCode: string;

  academicYearName: string;

  startDate: string;

  endDate: string;

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
