import {
  act,
  cleanup,
  render,
  screen,
  waitFor,
} from "@testing-library/react";

import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";

import ConnectDashboard
  from "./ConnectDashboard";

import {
  acknowledgeConnectMessageDelivered,
  acknowledgeConnectMessageRead,
  loadConnectMessageReceipts,
  loadConnectMessages,
  loadMyConnectSpaces,
  sendConnectTextMessage,
} from "../../services/connectService";

import type {
  ConnectMessage,
  ConnectReceipt,
  ConnectSpace,
} from "../../types/connect";


const authState =
  vi.hoisted(
    () => ({
      user: {
        id: "user-1",
      },
    })
  );


vi.mock(
  "../../auth/authContext",
  () => ({
    useAuth:
      () => authState,
  })
);


vi.mock(
  "../../services/connectService",
  () => ({
    acknowledgeConnectMessageDelivered:
      vi.fn(),

    acknowledgeConnectMessageRead:
      vi.fn(),

    loadConnectMessageReceipts:
      vi.fn(),

    loadConnectMessages:
      vi.fn(),

    loadMyConnectSpaces:
      vi.fn(),

    sendConnectTextMessage:
      vi.fn(),
  })
);


const mockDelivered =
  vi.mocked(
    acknowledgeConnectMessageDelivered
  );

const mockRead =
  vi.mocked(
    acknowledgeConnectMessageRead
  );

const mockLoadReceipts =
  vi.mocked(
    loadConnectMessageReceipts
  );

const mockLoadMessages =
  vi.mocked(
    loadConnectMessages
  );

const mockLoadSpaces =
  vi.mocked(
    loadMyConnectSpaces
  );

const mockSendMessage =
  vi.mocked(
    sendConnectTextMessage
  );


const space: ConnectSpace = {
  id: "space-1",
  spaceType: "INSTITUTION",
  name: "Parent Communication",
  contextType: null,
  contextReference: null,
};


function incomingMessage(
  id: string,
  body: string
): ConnectMessage {

  return {
    id,
    spaceId: space.id,
    senderUserId: "parent-1",
    messageType: "TEXT",
    body,
    replyToMessageId: null,
    sentAt:
      "2026-09-06T09:00:00Z",
    editedAt: null,
    deletedAt: null,
    attachments: [],
  };

}


function receiptFor(
  messageId: string
): ConnectReceipt {

  return {
    id:
      `receipt-${messageId}`,
    messageId,
    userId: "user-1",
    deliveredAt:
      "2026-09-06T09:00:01Z",
    readAt: null,
  };

}


describe(
  "ConnectDashboard receipt lifecycle",
  () => {

    let visibility:
      DocumentVisibilityState;

    let pollCallback:
      (() => void) | null;


    beforeEach(
      () => {

        visibility =
          "visible";

        pollCallback =
          null;


        Object.defineProperty(
          document,
          "visibilityState",
          {
            configurable: true,
            get:
              () => visibility,
          }
        );


        const nativeSetInterval =
          window.setInterval.bind(
            window
          );


        vi.spyOn(
          window,
          "setInterval"
        ).mockImplementation(
          (
            handler,
            timeout
          ) => {

            /*
             * Capture only GT Connect's controlled
             * 10-second polling timer.
             *
             * Other timers remain real so Testing
             * Library's asynchronous waiting cannot
             * overwrite the component poll callback.
             */
            if (
              timeout === 10_000 &&
              typeof handler ===
                "function"
            ) {

              pollCallback =
                handler as
                  () => void;

              return 1;

            }


            return nativeSetInterval(
              handler,
              timeout
            );

          }
        );


        mockDelivered
          .mockReset();

        mockRead
          .mockReset();

        mockLoadReceipts
          .mockReset();

        mockLoadMessages
          .mockReset();

        mockLoadSpaces
          .mockReset();

        mockSendMessage
          .mockReset();


        mockLoadSpaces
          .mockResolvedValue(
            [space]
          );

        mockLoadReceipts
          .mockResolvedValue(
            []
          );

        mockDelivered
          .mockImplementation(
            async (
              _spaceId,
              messageId
            ) =>
              receiptFor(
                messageId
              )
          );

        mockRead
          .mockImplementation(
            async (
              _spaceId,
              messageId
            ) => ({
              ...receiptFor(
                messageId
              ),
              readAt:
                "2026-09-06T09:00:02Z",
            })
          );

      }
    );


    afterEach(
      () => {

        cleanup();

        vi.restoreAllMocks();

        Reflect.deleteProperty(
          document,
          "visibilityState"
        );

      }
    );


    it(
      "marks incoming messages Read when the conversation is visible",
      async () => {

        const message =
          incomingMessage(
            "message-visible",
            "Visible message"
          );


        mockLoadMessages
          .mockResolvedValue(
            [message]
          );


        render(
          <ConnectDashboard />
        );


        await waitFor(
          () => {

            expect(
              mockRead
            ).toHaveBeenCalledWith(
              space.id,
              message.id
            );

          }
        );


        expect(
          mockDelivered
        ).not.toHaveBeenCalled();


        expect(
          screen.getByText(
            "Visible message"
          )
        ).toBeTruthy();

      }
    );


    it(
      "marks a new hidden-tab message Delivered and marks it Read after visibility returns",
      async () => {

        const existingMessage =
          incomingMessage(
            "message-existing",
            "Existing message"
          );

        const newMessage =
          incomingMessage(
            "message-new",
            "New hidden message"
          );


        mockLoadMessages
          .mockResolvedValueOnce(
            [existingMessage]
          );


        render(
          <ConnectDashboard />
        );


        await waitFor(
          () => {

            expect(
              mockRead
            ).toHaveBeenCalledWith(
              space.id,
              existingMessage.id
            );

          }
        );


        await waitFor(
          () => {

            expect(
              pollCallback
            ).not.toBeNull();

          }
        );


        mockRead.mockClear();
        mockDelivered.mockClear();


        visibility =
          "hidden";


        mockLoadMessages
          .mockResolvedValueOnce(
            [
              existingMessage,
              newMessage,
            ]
          );


        await act(
          async () => {

            pollCallback?.();

          }
        );


        await waitFor(
          () => {

            expect(
              mockDelivered
            ).toHaveBeenCalledWith(
              space.id,
              newMessage.id
            );

          }
        );


        expect(
          mockRead
        ).not.toHaveBeenCalled();


        mockRead.mockClear();


        visibility =
          "visible";


        mockLoadMessages
          .mockResolvedValueOnce(
            [
              existingMessage,
              newMessage,
            ]
          );


        await act(
          async () => {

            document.dispatchEvent(
              new Event(
                "visibilitychange"
              )
            );

          }
        );


        await waitFor(
          () => {

            expect(
              mockRead
            ).toHaveBeenCalledWith(
              space.id,
              newMessage.id
            );

          }
        );

      }
    );

  }
);
