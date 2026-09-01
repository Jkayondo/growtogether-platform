import {
  getConnectMessageReceipts,
  getConnectMessages,
  getMyConnectSpaces,
  markConnectMessageDelivered,
  markConnectMessageRead,
  postConnectTextMessage
} from "./connectApi";

import type {
  ConnectMessage,
  ConnectReceipt,
  ConnectSpace
} from "../types/connect";

export async function loadMyConnectSpaces()
  : Promise<ConnectSpace[]> {

  const response =
    await getMyConnectSpaces();

  return response.data;
}

export async function loadConnectMessages(
  spaceId: string
): Promise<ConnectMessage[]> {

  const response =
    await getConnectMessages(
      spaceId
    );

  return response.data;
}

export async function sendConnectTextMessage(
  spaceId: string,
  body: string
): Promise<ConnectMessage> {

  const response =
    await postConnectTextMessage(
      spaceId,
      body
    );

  return response.data;
}


export async function loadConnectMessageReceipts(
  spaceId: string,
  messageId: string
): Promise<ConnectReceipt[]> {

  const response =
    await getConnectMessageReceipts(
      spaceId,
      messageId
    );

  return response.data;
}

export async function acknowledgeConnectMessageDelivered(
  spaceId: string,
  messageId: string
): Promise<ConnectReceipt> {

  const response =
    await markConnectMessageDelivered(
      spaceId,
      messageId
    );

  return response.data;
}


export async function acknowledgeConnectMessageRead(
  spaceId: string,
  messageId: string
): Promise<ConnectReceipt> {

  const response =
    await markConnectMessageRead(
      spaceId,
      messageId
    );

  return response.data;
}
