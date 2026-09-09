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
  sendConnectTextMessage,
  addConnectInstitutionMember,
  findConnectInstitutionMemberCandidates
} from "../../services/connectService";

import type {
  ConnectMessage,
  ConnectReceipt,
  ConnectSpace,
  ConnectInstitutionMemberCandidate
} from "../../types/connect";

import "./connect.css";


type ReceiptMap =
  Record<string, ConnectReceipt[]>;


const CONNECT_POLL_INTERVAL_MS =
  10_000;

const CONNECT_SOUND_PREFERENCE_KEY =
  "gt_connect_sound_enabled";


const CONNECT_MANAGE_PERMISSION =
  "core.connect.manage";


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


  const [memberSearchQuery, setMemberSearchQuery] =
    useState("");

  const [
    memberCandidates,
    setMemberCandidates
  ] =
    useState<ConnectInstitutionMemberCandidate[]>([]);

  const [searchingMembers, setSearchingMembers] =
    useState(false);

  const [addingMemberId, setAddingMemberId] =
    useState<string | null>(null);

  const [memberNotice, setMemberNotice] =
    useState<string | null>(null);

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

          /*
           * If the authorised space list becomes empty after a
           * refresh, retire any conversation state that belonged
           * to the previously selected space.
           *
           * This occurs after the external API synchronization
           * completes rather than synchronously inside an effect.
           */
          if (result.length === 0) {
            setMessages([]);
            setReceiptsByMessage({});
          }

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

      /*
       * Schedule the initial external synchronization outside the
       * synchronous effect body. The cleanup also prevents a queued
       * initial load from surviving an unmount/remount cycle.
       */
      const timeoutId =
        window.setTimeout(
          () => {
            void loadSpaces();
          },
          0
        );

      return () => {
        window.clearTimeout(
          timeoutId
        );
      };

    },
    [loadSpaces]
  );


  useEffect(
    () => {

      if (!selectedSpaceId) {
        return;
      }

      /*
       * Synchronize the selected conversation from the external
       * Connect API outside the synchronous effect body.
       *
       * Cancelling the queued task prevents an obsolete selection
       * from starting a load if the user changes spaces immediately.
       */
      const timeoutId =
        window.setTimeout(
          () => {
            void loadConversation(
              selectedSpaceId
            );
          },
          0
        );

      return () => {
        window.clearTimeout(
          timeoutId
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


  const canManageInstitutionMembers =
    Boolean(
      selectedSpace &&
      selectedSpace.contextType === "SCHOOL_PROFILE" &&
      user?.permissions?.includes(
        CONNECT_MANAGE_PERMISSION
      )
    );


  const searchInstitutionMembers =
    async (
      event: FormEvent<HTMLFormElement>
    ) => {

      event.preventDefault();

      if (
        !selectedSpaceId ||
        !canManageInstitutionMembers ||
        searchingMembers
      ) {
        return;
      }

      const query =
        memberSearchQuery.trim();

      if (query.length < 2) {
        setMemberCandidates([]);
        setMemberNotice(
          "Enter at least 2 characters to search."
        );
        return;
      }

      setSearchingMembers(true);
      setMemberNotice(null);

      try {

        const result =
          await findConnectInstitutionMemberCandidates(
            selectedSpaceId,
            query
          );

        setMemberCandidates(
          result
        );

        if (result.length === 0) {
          setMemberNotice(
            "No eligible GT people matched this search."
          );
        }

      }
      catch {

        setMemberCandidates([]);
        setMemberNotice(
          "Unable to search GT people."
        );

      }
      finally {

        setSearchingMembers(false);

      }
    };


  const addInstitutionMember =
    async (
      candidate: ConnectInstitutionMemberCandidate
    ) => {

      if (
        !selectedSpaceId ||
        !canManageInstitutionMembers ||
        addingMemberId
      ) {
        return;
      }

      setAddingMemberId(
        candidate.userId
      );

      setMemberNotice(null);

      try {

        await addConnectInstitutionMember(
          selectedSpaceId,
          candidate.userId
        );

        setMemberCandidates(
          current =>
            current.filter(
              item =>
                item.userId !== candidate.userId
            )
        );

        setMemberNotice(
          `${candidate.displayName ?? candidate.username} added to this GT Connect institution.`
        );

      }
      catch {

        setMemberNotice(
          "Unable to add this GT person."
        );

      }
      finally {

        setAddingMemberId(null);

      }
    };


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


            {
              canManageInstitutionMembers && (
                <section className="gt-connect-member-manager">

                  <div className="gt-connect-member-manager-heading">
                    <div>
                      <strong>Institution members</strong>
                      <span>
                        Search existing GT people and add them to this institution.
                      </span>
                    </div>
                  </div>

                  <form
                    className="gt-connect-member-search"
                    onSubmit={searchInstitutionMembers}
                  >
                    <input
                      className="gt-input"
                      type="search"
                      aria-label="Search GT people"
                      placeholder="Search by name, username or email..."
                      value={memberSearchQuery}
                      disabled={searchingMembers}
                      onChange={
                        event =>
                          setMemberSearchQuery(
                            event.target.value
                          )
                      }
                    />

                    <button
                      className="gt-button"
                      type="submit"
                      disabled={
                        searchingMembers ||
                        memberSearchQuery.trim().length < 2
                      }
                    >
                      {
                        searchingMembers
                          ? "Searching..."
                          : "Search"
                      }
                    </button>
                  </form>

                  {
                    memberNotice && (
                      <div
                        className="gt-connect-member-notice"
                        role="status"
                      >
                        {memberNotice}
                      </div>
                    )
                  }

                  {
                    memberCandidates.length > 0 && (
                      <div className="gt-connect-member-results">

                        {
                          memberCandidates.map(
                            candidate => (

                              <div
                                className="gt-connect-member-candidate"
                                key={candidate.userId}
                              >
                                <div>
                                  <strong>
                                    {
                                      candidate.displayName ??
                                      candidate.username
                                    }
                                  </strong>

                                  <span>
                                    @{candidate.username}
                                  </span>
                                </div>

                                <button
                                  className="gt-button"
                                  type="button"
                                  disabled={
                                    addingMemberId !== null
                                  }
                                  onClick={
                                    () =>
                                      void addInstitutionMember(
                                        candidate
                                      )
                                  }
                                >
                                  {
                                    addingMemberId === candidate.userId
                                      ? "Adding..."
                                      : "Add"
                                  }
                                </button>
                              </div>

                            )
                          )
                        }

                      </div>
                    )
                  }

                </section>
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
