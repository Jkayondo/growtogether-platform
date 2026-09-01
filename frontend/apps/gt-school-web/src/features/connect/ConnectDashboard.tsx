import {
  useCallback,
  useEffect,
  useRef,
  useState
} from "react";

import type {
  FormEvent
} from "react";

import GTSection
  from "../../components/common/GTSection";

import {
  useAuth
} from "../../auth/authContext";

import {
  acknowledgeConnectMessageDelivered,
  acknowledgeConnectMessageRead,
  loadConnectMessageReceipts,
  loadConnectMessages,
  loadMyConnectSpaces,
  sendConnectTextMessage
} from "../../services/connectService";

import type {
  ConnectMessage,
  ConnectReceipt,
  ConnectSpace
} from "../../types/connect";

import "./connect.css";


type ReceiptMap =
  Record<string, ConnectReceipt[]>;


const CONNECT_POLL_INTERVAL_MS =
  10_000;

const CONNECT_SOUND_PREFERENCE_KEY =
  "gt_connect_sound_enabled";


function formatMessageTime(
  value: string
) {

  return new Date(
    value
  ).toLocaleString();

}


function receiptLabel(
  receipts: ConnectReceipt[] | undefined
) {

  if (!receipts) {
    return "Loading...";
  }

  if (receipts.some(
    receipt => Boolean(receipt.readAt)
  )) {
    return "Read";
  }

  if (receipts.some(
    receipt => Boolean(receipt.deliveredAt)
  )) {
    return "Delivered";
  }

  return "Sent";
}


export default function ConnectDashboard() {

  const {
    user
  } = useAuth();


  const [spaces, setSpaces] =
    useState<ConnectSpace[]>([]);

  const [
    selectedSpaceId,
    setSelectedSpaceId
  ] =
    useState<string | null>(null);

  const [messages, setMessages] =
    useState<ConnectMessage[]>([]);

  const [
    receiptsByMessage,
    setReceiptsByMessage
  ] =
    useState<ReceiptMap>({});

  const [loadingSpaces, setLoadingSpaces] =
    useState(true);

  const [
    loadingMessages,
    setLoadingMessages
  ] =
    useState(false);

  const [error, setError] =
    useState<string | null>(null);

  const [messageBody, setMessageBody] =
    useState("");

  const [sendingMessage, setSendingMessage] =
    useState(false);

  const [soundEnabled, setSoundEnabled] =
    useState(
      () =>
        localStorage.getItem(
          CONNECT_SOUND_PREFERENCE_KEY
        ) !== "false"
    );

  const knownMessageIdsBySpace =
    useRef<Map<string, Set<string>>>(
      new Map()
    );

  const audioContextRef =
    useRef<AudioContext | null>(
      null
    );


  const getAudioContext =
    useCallback(
      () => {

        if (audioContextRef.current) {
          return audioContextRef.current;
        }

        if (!window.AudioContext) {
          return null;
        }

        const audioContext =
          new window.AudioContext();

        audioContextRef.current =
          audioContext;

        return audioContext;

      },
      []
    );


  const playIncomingMessageSound =
    useCallback(
      async () => {

        if (!soundEnabled) {
          return;
        }

        try {

          const audioContext =
            getAudioContext();

          if (!audioContext) {
            return;
          }

          if (
            audioContext.state === "suspended"
          ) {
            await audioContext.resume();
          }

          const oscillator =
            audioContext.createOscillator();

          const gain =
            audioContext.createGain();

          const now =
            audioContext.currentTime;

          oscillator.type =
            "sine";

          oscillator.frequency.setValueAtTime(
            880,
            now
          );

          oscillator.frequency.exponentialRampToValueAtTime(
            660,
            now + 0.16
          );

          gain.gain.setValueAtTime(
            0.0001,
            now
          );

          gain.gain.exponentialRampToValueAtTime(
            0.12,
            now + 0.015
          );

          gain.gain.exponentialRampToValueAtTime(
            0.0001,
            now + 0.22
          );

          oscillator.connect(
            gain
          );

          gain.connect(
            audioContext.destination
          );

          oscillator.start(
            now
          );

          oscillator.stop(
            now + 0.23
          );

        }
        catch {

          /*
           * Browser audio policy must never interrupt
           * GT Connect message delivery.
           */

        }

      },
      [
        getAudioContext,
        soundEnabled
      ]
    );


  useEffect(
    () => {

      if (!soundEnabled) {
        return;
      }

      const unlockAudio =
        () => {

          const audioContext =
            getAudioContext();

          if (
            audioContext &&
            audioContext.state === "suspended"
          ) {
            void audioContext.resume();
          }

        };

      window.addEventListener(
        "pointerdown",
        unlockAudio,
        {
          once: true
        }
      );

      window.addEventListener(
        "keydown",
        unlockAudio,
        {
          once: true
        }
      );

      return () => {

        window.removeEventListener(
          "pointerdown",
          unlockAudio
        );

        window.removeEventListener(
          "keydown",
          unlockAudio
        );

      };

    },
    [
      getAudioContext,
      soundEnabled
    ]
  );


  useEffect(
    () => {

      return () => {

        if (
          audioContextRef.current
        ) {

          void audioContextRef.current.close();

          audioContextRef.current =
            null;

        }

      };

    },
    []
  );


  const toggleSound =
    () => {

      const nextEnabled =
        !soundEnabled;

      setSoundEnabled(
        nextEnabled
      );

      localStorage.setItem(
        CONNECT_SOUND_PREFERENCE_KEY,
        String(nextEnabled)
      );

      /*
       * Enabling sound is an explicit user gesture,
       * allowing the browser audio context to be primed.
       */
      if (nextEnabled) {

        const audioContext =
          getAudioContext();

        if (
          audioContext &&
          audioContext.state === "suspended"
        ) {
          void audioContext.resume();
        }

      }

    };


  const loadSpaces =
    useCallback(
      async () => {

        setError(null);
        setLoadingSpaces(true);

        try {

          const result =
            await loadMyConnectSpaces();

          setSpaces(
            result
          );

          setSelectedSpaceId(
            current => {

              if (
                current &&
                result.some(
                  space =>
                    space.id === current
                )
              ) {
                return current;
              }

              return result[0]?.id ?? null;
            }
          );

        }
        catch {

          setError(
            "Unable to load GT Connect conversations."
          );

        }
        finally {

          setLoadingSpaces(false);

        }

      },
      []
    );


  const loadConversation =
    useCallback(
      async (
        spaceId: string,
        silent = false
      ) => {

        if (!silent) {
          setError(null);
          setLoadingMessages(true);
          setReceiptsByMessage({});
        }

        try {

          const result =
            await loadConnectMessages(
              spaceId
            );

          setMessages(
            result
          );

          if (!user) {
            return;
          }

          const knownMessageIds =
            knownMessageIdsBySpace.current.get(
              spaceId
            );

          /*
           * Initial conversation loading establishes the
           * baseline and must never produce notification sound.
           *
           * Only a later silent synchronization cycle can
           * identify a genuinely new incoming message.
           */
          const newIncomingMessages =
            silent &&
            knownMessageIds
              ? result.filter(
                  message =>
                    message.senderUserId !== null &&
                    message.senderUserId !== user.id &&
                    !message.deletedAt &&
                    !knownMessageIds.has(
                      message.id
                    )
                )
              : [];

          if (
            newIncomingMessages.length > 0
          ) {
            void playIncomingMessageSound();
          }

          knownMessageIdsBySpace.current.set(
            spaceId,
            new Set(
              result.map(
                message =>
                  message.id
              )
            )
          );

          const incomingMessages =
            result.filter(
              message =>
                message.senderUserId !== null &&
                message.senderUserId !== user.id &&
                !message.deletedAt
            );

          /*
           * Receipt semantics:
           *
           * - visible conversation = Read
           * - hidden browser tab = Delivered only
           *
           * Background polling must never falsely claim that
           * the user has actually read a message.
           */
          if (
            document.visibilityState === "visible"
          ) {

            await Promise.allSettled(
              incomingMessages.map(
                message =>
                  acknowledgeConnectMessageRead(
                    spaceId,
                    message.id
                  )
              )
            );

          }
          else {

            await Promise.allSettled(
              newIncomingMessages.map(
                message =>
                  acknowledgeConnectMessageDelivered(
                    spaceId,
                    message.id
                  )
              )
            );

          }

          const ownMessages =
            result.filter(
              message =>
                message.senderUserId === user.id
            );

          const receiptResults =
            await Promise.all(
              ownMessages.map(
                async message => {

                  try {

                    const receipts =
                      await loadConnectMessageReceipts(
                        spaceId,
                        message.id
                      );

                    return [
                      message.id,
                      receipts
                    ] as const;

                  }
                  catch {

                    /*
                     * Receipt visibility is protected
                     * independently by the backend.
                     *
                     * Failure to retrieve one receipt
                     * must not break the conversation.
                     */
                    return [
                      message.id,
                      []
                    ] as const;

                  }

                }
              )
            );

          setReceiptsByMessage(
            Object.fromEntries(
              receiptResults
            )
          );

        }
        catch {

          if (!silent) {
            setMessages([]);

            setError(
              "Unable to load this GT Connect conversation."
            );
          }

        }
        finally {

          if (!silent) {
            setLoadingMessages(false);
          }

        }

      },
      [
        playIncomingMessageSound,
        user
      ]
    );


  useEffect(
    () => {

      void loadSpaces();

    },
    [loadSpaces]
  );


  useEffect(
    () => {

      if (!selectedSpaceId) {

        setMessages([]);
        return;

      }

      void loadConversation(
        selectedSpaceId
      );

    },
    [
      selectedSpaceId,
      loadConversation
    ]
  );


  useEffect(
    () => {

      if (!selectedSpaceId) {
        return;
      }

      const intervalId =
        window.setInterval(
          () => {

            /*
             * Release 1 near-real-time synchronization.
             *
             * Continue polling while this GT Connect view
             * remains mounted, including when the browser tab
             * is hidden. Receipt semantics inside
             * loadConversation distinguish Delivered from Read.
             *
             * Browsers may throttle background timers, so
             * background timing remains best-effort.
             */
            void loadConversation(
              selectedSpaceId,
              true
            );

          },
          CONNECT_POLL_INTERVAL_MS
        );

      return () => {

        window.clearInterval(
          intervalId
        );

      };

    },
    [
      selectedSpaceId,
      loadConversation
    ]
  );


  useEffect(
    () => {

      if (!selectedSpaceId) {
        return;
      }

      const handleVisibilityChange =
        () => {

          /*
           * When the user returns to GT Connect, synchronize
           * immediately instead of waiting for a background-
           * throttled polling timer.
           *
           * loadConversation() will apply the authoritative
           * visible-conversation Read boundary.
           */
          if (
            document.visibilityState === "visible"
          ) {

            void loadConversation(
              selectedSpaceId,
              true
            );

          }

        };

      document.addEventListener(
        "visibilitychange",
        handleVisibilityChange
      );

      return () => {

        document.removeEventListener(
          "visibilitychange",
          handleVisibilityChange
        );

      };

    },
    [
      selectedSpaceId,
      loadConversation
    ]
  );


  const selectedSpace =
    spaces.find(
      space =>
        space.id === selectedSpaceId
    );


  const submitMessage =
    async (
      event: FormEvent<HTMLFormElement>
    ) => {

      event.preventDefault();

      if (
        !selectedSpaceId ||
        sendingMessage
      ) {
        return;
      }

      const body =
        messageBody.trim();

      if (!body) {
        return;
      }

      setError(null);
      setSendingMessage(true);

      try {

        await sendConnectTextMessage(
          selectedSpaceId,
          body
        );

        setMessageBody("");

        await loadConversation(
          selectedSpaceId
        );

      }
      catch {

        setError(
          "Unable to send this GT Connect message."
        );

      }
      finally {

        setSendingMessage(false);

      }

    };


  return (

    <div className="gt-dashboard">

      <GTSection title="GT Connect">

        <div className="gt-connect-layout">

          <aside className="gt-card gt-connect-spaces">

            <div className="gt-connect-panel-heading">

              <div>
                <h3>Conversations</h3>
                <span>
                  Your authorised GT Connect spaces
                </span>
              </div>

              <button
                className="gt-button"
                type="button"
                onClick={() =>
                  void loadSpaces()
                }
              >
                Refresh
              </button>

            </div>


            {
              loadingSpaces && (
                <p>
                  Loading conversations...
                </p>
              )
            }


            {
              !loadingSpaces &&
              spaces.length === 0 && (
                <div className="gt-connect-empty">
                  No GT Connect conversations are available.
                </div>
              )
            }


            <div className="gt-connect-space-list">

              {
                spaces.map(
                  space => (

                    <button
                      key={space.id}
                      type="button"
                      className={
                        "gt-connect-space-item " +
                        (
                          selectedSpaceId === space.id
                            ? "active"
                            : ""
                        )
                      }
                      onClick={() =>
                        setSelectedSpaceId(
                          space.id
                        )
                      }
                    >

                      <strong>
                        {space.name}
                      </strong>

                      <span>
                        {space.spaceType}
                      </span>

                    </button>

                  )
                )
              }

            </div>

          </aside>


          <section className="gt-card gt-connect-conversation">

            <div className="gt-connect-panel-heading">

              <div>

                <h3>
                  {
                    selectedSpace?.name ??
                    "Conversation"
                  }
                </h3>

                {
                  selectedSpace && (
                    <span>
                      {selectedSpace.spaceType}
                    </span>
                  )
                }

              </div>


              {
                selectedSpaceId && (

                  <button
                    className="gt-connect-sound-toggle"
                    type="button"
                    aria-pressed={soundEnabled}
                    aria-label={
                      soundEnabled
                        ? "Mute GT Connect message sounds"
                        : "Enable GT Connect message sounds"
                    }
                    onClick={toggleSound}
                  >

                    <span aria-hidden="true">
                      {
                        soundEnabled
                          ? "🔔"
                          : "🔕"
                      }
                    </span>

                    <span>
                      {
                        soundEnabled
                          ? "Sound On"
                          : "Muted"
                      }
                    </span>

                  </button>

                )
              }

            </div>


            {
              error && (
                <div className="gt-connect-error">
                  {error}
                </div>
              )
            }


            {
              !selectedSpaceId &&
              !loadingSpaces && (
                <div className="gt-connect-empty">
                  Select a conversation to begin.
                </div>
              )
            }


            {
              selectedSpaceId &&
              loadingMessages && (
                <div className="gt-connect-empty">
                  Loading messages...
                </div>
              )
            }


            {
              selectedSpaceId &&
              !loadingMessages &&
              messages.length === 0 && (
                <div className="gt-connect-empty">
                  No messages in this conversation yet.
                </div>
              )
            }


            <div className="gt-connect-message-list">

              {
                messages.map(
                  message => {

                    const mine =
                      message.senderUserId ===
                      user?.id;

                    const deleted =
                      Boolean(
                        message.deletedAt
                      );

                    return (

                      <article
                        key={message.id}
                        className={
                          "gt-connect-message " +
                          (
                            mine
                              ? "mine"
                              : "received"
                          )
                        }
                      >

                        <div className="gt-connect-message-body">

                          {
                            deleted
                              ? (
                                  <em>
                                    Message deleted
                                  </em>
                                )
                              : (
                                  message.body ??
                                  `[${message.messageType}]`
                                )
                          }

                        </div>


                        {
                          message.attachments.length > 0 && (

                            <div className="gt-connect-attachments">

                              {
                                message.attachments.map(
                                  attachment => (

                                    <span
                                      key={
                                        `${attachment.documentId}-${attachment.documentVersion}`
                                      }
                                    >
                                      {attachment.mimeType}
                                    </span>

                                  )
                                )
                              }

                            </div>

                          )
                        }


                        <footer className="gt-connect-message-meta">

                          <span>
                            {
                              formatMessageTime(
                                message.sentAt
                              )
                            }
                          </span>

                          {
                            message.editedAt && (
                              <span>
                                Edited
                              </span>
                            )
                          }

                          {
                            mine && !deleted && (

                              <span
                                className={
                                  "gt-connect-receipt-status " +
                                  receiptLabel(
                                    receiptsByMessage[
                                      message.id
                                    ]
                                  ).toLowerCase()
                                }
                              >
                                {
                                  receiptLabel(
                                    receiptsByMessage[
                                      message.id
                                    ]
                                  )
                                }
                              </span>

                            )
                          }

                        </footer>

                      </article>

                    );

                  }
                )
              }

            </div>


            {
              selectedSpaceId && (

                <form
                  className="gt-connect-composer"
                  onSubmit={submitMessage}
                >

                  <textarea
                    className="gt-input gt-connect-composer-input"
                    aria-label="Message"
                    placeholder="Write a message..."
                    rows={3}
                    value={messageBody}
                    disabled={sendingMessage}
                    onChange={
                      event =>
                        setMessageBody(
                          event.target.value
                        )
                    }
                  />

                  <button
                    className="gt-button"
                    type="submit"
                    disabled={
                      sendingMessage ||
                      messageBody.trim().length === 0
                    }
                  >
                    {
                      sendingMessage
                        ? "Sending..."
                        : "Send"
                    }
                  </button>

                </form>

              )
            }

          </section>

        </div>

      </GTSection>

    </div>

  );

}
