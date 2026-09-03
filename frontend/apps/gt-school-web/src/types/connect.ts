export interface ConnectSpace {
  id: string;
  spaceType: string;
  name: string;
  contextType: string | null;
  contextReference: string | null;
}

export interface ConnectAttachment {
  documentId: string;
  documentVersion: number;
  mimeType: string;
  sizeBytes: number;
}

export interface ConnectMessage {
  id: string;
  spaceId: string;
  senderUserId: string | null;
  messageType: string;
  body: string | null;
  replyToMessageId: string | null;
  sentAt: string;
  editedAt: string | null;
  deletedAt: string | null;
  attachments: ConnectAttachment[];
}

export interface ConnectReceipt {
  id: string;
  messageId: string;
  userId: string;
  deliveredAt: string | null;
  readAt: string | null;
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
