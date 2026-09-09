import type { Role } from "./roles";


export interface AuthUser {

  id: string;

  name: string;

  email: string;

  organisationId: string;

  organisationName: string;

  role: Role;

  permissions: string[];

}



export interface AuthTokenResponse {

  tokenType: string;

  accessToken: string;

  accessTokenExpiresAt: string;

  refreshToken: string;

  refreshTokenExpiresAt: string;

  sessionId: string;

  userId: string;

  tenantId: string;

  username: string;

  roles: string[];

  permissions: string[];

}



export interface AuthSession {

  accessToken: string;

  refreshToken: string;

  user: AuthUser;

}



export interface TokenResponse {

  tokenType: string;

  accessToken: string;

  accessTokenExpiresAt: string;

  refreshToken: string;

  refreshTokenExpiresAt: string;

  sessionId: string;

  userId: string;

  tenantId: string;

  username: string;

  roles: string[];

  permissions: string[];

}



export interface LoginResponse {

  mfaRequired: boolean;

  challengeToken?: string;

  challengeExpiresAt?: string;

  tokens?: TokenResponse;

  trustedDeviceToken?: string;

  trustedDeviceExpiresAt?: string;

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