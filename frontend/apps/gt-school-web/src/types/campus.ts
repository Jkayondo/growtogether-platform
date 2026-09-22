export interface Campus {

  id: string;

  tenantId: string;

  schoolProfileId: string;

  campusCode: string;

  campusName: string;

  addressLine?: string | null;

  district?: string | null;

  city?: string | null;

  countryCode?: string | null;

  phoneNumber?: string | null;

  email?: string | null;

  mainCampus: boolean;

  status: string;

  createdAt?: string;

  createdBy?: string;

  updatedAt?: string;

  updatedBy?: string;

  version?: number;

}


export interface CreateCampusRequest {

  campusCode: string;

  campusName: string;

  addressLine?: string;

  district?: string;

  city?: string;

  countryCode?: string;

  phoneNumber?: string;

  email?: string;

  mainCampus: boolean;

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
