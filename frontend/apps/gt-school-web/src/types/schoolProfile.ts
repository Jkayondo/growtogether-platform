export interface SchoolProfile {

  id: string;

  tenantId: string;

  schoolCode: string;

  schoolName: string;

  legalName?: string | null;

  educationSystem?: string | null;

  countryCode?: string | null;

  defaultCurrency?: string | null;

  timezone?: string | null;

  email?: string | null;

  phoneNumber?: string | null;

  website?: string | null;

  status?: string;

  createdAt?: string;

  createdBy?: string;

  updatedAt?: string;

  updatedBy?: string;

  version?: number;

}
