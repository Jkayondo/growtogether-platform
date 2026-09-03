import apiClient from "../services/apiClient";


import type {
  LoginResponse,
  ApiResponse
} from "./authTypes";



interface LoginRequest {


  usernameOrEmail: string;


  password: string;


  trustedDeviceToken?: string;


  deviceFingerprint?: string;


}



export function login(
  credentials: LoginRequest
) {


  return apiClient.post<
    ApiResponse<LoginResponse>
  >(
    "/api/v1/eiam/auth/login",
    credentials
  );


}