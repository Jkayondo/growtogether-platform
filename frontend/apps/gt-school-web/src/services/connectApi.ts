import apiClient from "./apiClient";

import type {
  ApiResponse,
  ConnectMessage,
  ConnectReceipt,
  ConnectSpace,
  ConnectInstitutionMemberCandidate,
  ConnectMember
} from "../types/connect";

export function getMyConnectSpaces()
  : Promise<ApiResponse<ConnectSpace[]>> {

  return apiClient.get<
    ApiResponse<ConnectSpace[]>
  >(
    "/api/v1/connect/spaces"
  );
}

export function getConnectMessages(
  spaceId: string
): Promise<ApiResponse<ConnectMessage[]>> {

  return apiClient.get<
    ApiResponse<ConnectMessage[]>
  >(
    `/api/v1/connect/spaces/${encodeURIComponent(
      spaceId
    )}/messages`
  );
}

export function postConnectTextMessage(
  spaceId: string,
  body: string
): Promise<ApiResponse<ConnectMessage>> {

  return apiClient.post<
    ApiResponse<ConnectMessage>
  >(
    `/api/v1/connect/spaces/${encodeURIComponent(
      spaceId
    )}/messages`,
    {
      body,
      replyToMessageId: null
    }
  );
}


export function getConnectMessageReceipts(
  spaceId: string,
  messageId: string
): Promise<ApiResponse<ConnectReceipt[]>> {

  return apiClient.get<
    ApiResponse<ConnectReceipt[]>
  >(
    `/api/v1/connect/spaces/${encodeURIComponent(
      spaceId
    )}/messages/${encodeURIComponent(
      messageId
    )}/receipts`
  );
}

export function markConnectMessageDelivered(
  spaceId: string,
  messageId: string
): Promise<ApiResponse<ConnectReceipt>> {

  return apiClient.post<
    ApiResponse<ConnectReceipt>
  >(
    `/api/v1/connect/spaces/${encodeURIComponent(
      spaceId
    )}/messages/${encodeURIComponent(
      messageId
    )}/delivered`,
    {}
  );
}


export function markConnectMessageRead(
  spaceId: string,
  messageId: string
): Promise<ApiResponse<ConnectReceipt>> {

  return apiClient.post<
    ApiResponse<ConnectReceipt>
  >(
    `/api/v1/connect/spaces/${encodeURIComponent(
      spaceId
    )}/messages/${encodeURIComponent(
      messageId
    )}/read`,
    {}
  );
}


export async function searchConnectInstitutionMemberCandidates(
  spaceId: string,
  query: string
): Promise<ApiResponse<ConnectInstitutionMemberCandidate[]>> {

  return apiClient.get<
    ApiResponse<ConnectInstitutionMemberCandidate[]>
  >(
    `/api/v1/connect/spaces/${encodeURIComponent(
      spaceId
    )}/institution-member-candidates?query=${encodeURIComponent(
      query
    )}`
  );
}


export async function postConnectInstitutionMember(
  spaceId: string,
  userId: string
): Promise<ApiResponse<ConnectMember>> {

  return apiClient.post<
    ApiResponse<ConnectMember>
  >(
    `/api/v1/connect/spaces/${encodeURIComponent(
      spaceId
    )}/institution-members`,
    {
      userId
    }
  );
}
