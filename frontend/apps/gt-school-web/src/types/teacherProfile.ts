export interface TeacherProfile {

  id: string;

  tenantId: string;

  workforceMemberId: string;

  teacherNumber: string;

  teacherRegistrationNumber?: string | null;

  teachingLicenceNumber?: string | null;

  teachingLicenceIssuedAt?: string | null;

  teachingLicenceExpiresAt?: string | null;

  highestTeachingLevel?: string | null;

  primarySpecialization?: string | null;

  secondarySpecialization?: string | null;

  teacherCategory: string;

  teachingStatus: string;

  qualifiedForBoardingDuty: boolean;

  qualifiedForSpecialNeeds: boolean;

  qualifiedForCounselling: boolean;

  maximumWeeklyPeriods?: number | null;

  notes?: string | null;

  status: string;

  createdAt?: string;

  createdBy?: string;

  updatedAt?: string;

  updatedBy?: string;

  version?: number;

}


export interface CreateTeacherProfileRequest {

  workforceMemberId: string;

  teacherNumber: string;

  teacherRegistrationNumber?: string;

  teachingLicenceNumber?: string;

  teachingLicenceIssuedAt?: string;

  teachingLicenceExpiresAt?: string;

  highestTeachingLevel?: string;

  primarySpecialization?: string;

  secondarySpecialization?: string;

  teacherCategory?: string;

  qualifiedForBoardingDuty?: boolean;

  qualifiedForSpecialNeeds?: boolean;

  qualifiedForCounselling?: boolean;

  maximumWeeklyPeriods?: number;

  notes?: string;

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
