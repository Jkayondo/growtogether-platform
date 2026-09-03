# GT-CONNECT-ECOSYSTEM-001
# GrowTogether Unified Intelligent Communication & Collaboration Ecosystem

**Programme:** GrowTogether Enterprise Platform  
**Capability:** GT Connect  
**Classification:** Enterprise Architecture / Product Vision / Continuity Record  
**Status:** CONTROLLED DIRECTION — ACTIVE  
**Release 1 Status:** Foundation under engineering implementation  
**Advanced Capabilities:** Reserved for progressive releases unless separately authorised  
**Recovery Importance:** CRITICAL  

---

## 1. PURPOSE

GT Connect is the unified intelligent communication, collaboration and
engagement ecosystem of the GrowTogether Enterprise Platform.

GT Connect SHALL NOT be designed merely as a chat feature.

Its long-term purpose is to provide one integrated environment for:

- messaging;
- voice;
- video;
- calls;
- meetings;
- groups and communities;
- announcements;
- documents and files;
- notifications;
- workflows;
- collaboration;
- multilingual communication;
- artificial intelligence;
- governed organisational communication;
- secure private communication;
- safeguarding;
- persistent collaboration memory; and
- omnichannel communication.

The intended product experience is ONE GT Connect ecosystem rather than
a collection of disconnected communication applications.

---

## 2. STRATEGIC VISION

GT Connect is intended to become substantially broader than a conventional
consumer messenger.

The strategic objective is not to reproduce WhatsApp.

The objective is to build a GrowTogether-native communication and
collaboration ecosystem that combines communication with institutional
context, workflows, documents, intelligence, governance and enterprise
services.

Conceptually:

Communication
+
Collaboration
+
Voice
+
Video
+
Meetings
+
Documents
+
Workflow
+
Notifications
+
AI
+
Institutional Context
+
Governance
+
Security
=
GT Connect

---

## 3. ENTERPRISE PRINCIPLE

GT Connect follows the GrowTogether enterprise principle:

> Platform First. Products Second. Build Once. Reuse Everywhere.

GT Connect SHALL reuse enterprise capabilities instead of duplicating them.

Examples include:

- EIAM / IAM for identity, roles, permissions and authentication;
- tenant isolation;
- ENS for notification delivery;
- EDS for governed documents/files;
- Enterprise Workflow Engine for workflows;
- enterprise events;
- audit and evidence services;
- enterprise configuration;
- observability;
- security services; and
- future enterprise AI capabilities.

Connect-specific state belongs in GT Connect.

Shared enterprise capability must remain in its authoritative enterprise
service.

---

## 4. ONE ECOSYSTEM — USER EXPERIENCE

The intended user-facing structure is conceptually:

GT Connect
|
+-- Chats
|   +-- Direct conversations
|   +-- Group conversations
|   +-- Class conversations
|   +-- Department conversations
|   +-- Institution conversations
|   +-- Communities
|
+-- Calls
|   +-- Voice
|   +-- Video
|   +-- Group calls
|
+-- Meetings
|   +-- Scheduled meetings
|   +-- Ad-hoc meetings
|   +-- Screen sharing
|   +-- Governed recordings
|
+-- Announcements
|
+-- Channels / Communities
|
+-- Files & Documents
|
+-- Notifications
|
+-- Polls
|
+-- Location
|
+-- Workflows & Actions
|
+-- GT Intelligence
|
+-- Collaboration Memory

The exact navigation may evolve, but the architectural principle of
ONE GT Connect ecosystem is preserved.

---

## 5. COMMUNICATION MODES

GT Connect is intended to support, progressively:

### 5.1 Text

- one-to-one messaging;
- group messaging;
- class communication;
- departmental communication;
- institution-wide communication;
- replies;
- editing;
- deletion subject to governance;
- message history;
- search;
- delivery/read status.

### 5.2 Audio

- recorded voice notes;
- audio message attachments;
- future live voice calling;
- future group voice calling.

### 5.3 Video

- recorded video messages;
- governed video attachments;
- future live video calling;
- future group video;
- future meetings/conferencing.

### 5.4 Rich Communication

- images;
- files;
- documents;
- location;
- polls;
- system messages;
- announcements;
- workflow-generated messages;
- AI-assisted communication.

---

## 6. PARENT-TO-PARENT COMMUNICATION

GT Connect SHALL support a future governed parent-to-parent communication
journey.

The intended private communication capabilities include:

- text messages;
- voice notes;
- video messages;
- images;
- documents/files;
- future voice calls;
- future video calls.

Parent contact information SHALL NOT automatically be exposed simply
because two parents belong to the same institution.

Parent-to-parent discovery and communication must be governed by:

- tenant boundaries;
- institution policy;
- relevant school/class/stream relationships where required;
- privacy preferences;
- consent rules where required;
- blocking;
- reporting;
- safeguarding;
- abuse controls;
- moderation/escalation policy; and
- audit requirements appropriate to the communication class.

---

## 7. SCHOOL COMMUNICATION CONTEXTS

GT School is the first major product consuming GT Connect.

Current/future School communication contexts include:

- parent <-> parent;
- parent <-> teacher;
- parent <-> administration;
- teacher <-> teacher;
- teacher <-> administration;
- learner-related authorised communication;
- class/stream groups;
- departments;
- institution-wide communication;
- announcements;
- events;
- meetings;
- safeguarding workflows.

Relationship-sensitive communication SHALL be validated against live
authoritative School relationships where applicable.

---

## 8. SECURITY AND ENCRYPTION

Security is mandatory across GT Connect.

The current Release 1 implementation MUST NOT be described as providing
true end-to-end encryption unless E2EE has actually been implemented and
validated.

### 8.1 Current Position

True WhatsApp/Signal-style end-to-end encryption is NOT currently
implemented in GT Connect Release 1.

### 8.2 Future Direction

Reserved capability:

**GT-CONNECT-E2EE-001 — Governed End-to-End Encrypted Private Communication**

E2EE is a strong candidate for appropriate private communication classes,
including parent-to-parent private conversations.

Future E2EE design must address:

- device identity;
- cryptographic key generation;
- secure key storage;
- key rotation;
- multi-device operation;
- participant/device verification;
- forward secrecy where applicable;
- lost/stolen-device recovery;
- account/device revocation;
- attachment encryption;
- metadata minimisation;
- backup/recovery policy;
- blocking/reporting;
- abuse handling;
- safeguarding boundaries.

### 8.3 Different Communication Classes May Require Different Policies

GT Connect SHALL NOT assume that every communication class must use the
same encryption model.

Examples of intended policy evaluation:

| Communication Class | Security Direction |
|---|---|
| Parent <-> Parent private | E2EE candidate |
| Private user conversations | E2EE candidate |
| Parent <-> Teacher | Governed enterprise encryption / policy-based |
| Parent <-> School Administration | Governed, encrypted, auditable |
| Official announcements | Enterprise encrypted, auditable |
| Safeguarding communication | Highly protected, controlled authorised access |
| Emergency / institutional records | Governed retention and audit |

Final encryption policies require dedicated security architecture and
release authorisation.

---

## 9. SAFEGUARDING

Because GT School communication may involve children, safeguarding is a
first-class architectural requirement.

GT Connect must progressively support:

- relationship-aware access;
- controlled parent/teacher communication;
- blocking;
- reporting;
- abuse reporting;
- authorised moderation;
- escalation;
- evidence preservation according to policy;
- institutional safeguarding workflows;
- privacy controls;
- tenant isolation;
- least-privilege access.

No advanced privacy mechanism may silently defeat mandatory safeguarding
or lawful institutional governance requirements.

---

## 10. GT CONNECT INTELLIGENCE

Reserved capability:

**GT-CONNECT-BRAIN-001 — Intelligent Collaboration Brain & Memory**

Future intelligent capabilities may include:

- conversation summarisation;
- action extraction;
- task generation;
- meeting summaries;
- decision extraction;
- risk identification;
- proposal analysis;
- brainstorming;
- challenge/improvement of ideas;
- multilingual translation;
- drafting assistance;
- semantic conversation search;
- governed persistent collaboration memory;
- knowledge graph relationships;
- conversion of communication into:
  - workflows;
  - documents;
  - tasks;
  - events;
  - announcements;
  - reports.

AI must operate within permission, privacy, safeguarding and tenant
boundaries.

---

## 11. WORKFLOW INTEGRATION

GT Connect is intended to be actionable communication.

A conversation may eventually lead directly to:

- a task;
- an approval;
- a workflow;
- a school event;
- an announcement;
- a document;
- a meeting;
- a payment request where authorised;
- a support case;
- an institutional record.

This differentiates GT Connect from communication systems that treat
messages as isolated content.

---

## 12. DOCUMENT AND FILE GOVERNANCE

GT Connect SHALL reuse EDS for governed file/document capability.

GT Connect messages should reference authoritative EDS documents and
versions rather than creating independent uncontrolled document stores.

Current architecture supports message types including:

- IMAGE;
- FILE;
- AUDIO;
- VIDEO.

---

## 13. NOTIFICATION INTEGRATION

GT Connect SHALL remain distinct from ENS.

Architecture:

Business Event
    ->
Enterprise Communication Orchestration
    ->
GT Connect internal message
and/or
ENS external notification

ENS may deliver through authorised channels such as:

- in-app;
- push;
- email;
- SMS;
- WhatsApp integration;
- other future enterprise channels.

GT_CONNECT is not to be treated as simply another ENS notification
channel because GT Connect maintains its own conversation model.

---

## 14. OMNICHANNEL DIRECTION

GT Connect is governed by the GrowTogether omnichannel principle.

Target user environments include:

- Web;
- Desktop;
- Android;
- Apple/iOS.

The experience should remain recognisably one GT Connect ecosystem across
devices.

---

## 15. RELEASE 1 FOUNDATION

Release 1 intentionally establishes the secure architectural foundation
rather than attempting the entire future ecosystem at once.

Current backend foundation includes or has undergone engineering for:

- Connect spaces;
- memberships;
- message storage;
- tenant isolation;
- server-derived user identity;
- text messaging;
- image/file/audio/video attachment message types;
- EDS-backed attachment references;
- message history;
- editing;
- deletion;
- protected School participant access;
- parent relationship validation;
- teacher assignment validation;
- message delivery/read receipts;
- receipt-status retrieval;
- announcements;
- ENS integration;
- School workflow/event integration;
- conversation search;
- School institution-space provisioning;
- SCHOOL_ADMIN-derived Connect administration;
- conversation discovery / "My Conversations".

Frontend implementation is being developed vertically with backend
capabilities and must not be assumed complete merely because backend
source exists.

---

## 16. MESSAGE TYPES

GT Connect Release 1 domain recognises:

- TEXT
- IMAGE
- FILE
- AUDIO
- VIDEO
- LOCATION
- POLL
- SYSTEM

Support in the domain model does not automatically mean every associated
end-user journey is complete.

Each capability requires its own Backend / Frontend / E2E evidence.

---

## 17. CURRENT SCHOOL ADMINISTRATION GOVERNANCE

Canonical School institution conversation:

- Connect Space Type: INSTITUTION
- Context Type: SCHOOL_PROFILE
- Context Reference: SchoolProfile ID

SCHOOL_ADMIN remains authoritative in EIAM.

The architectural direction is:

EIAM SCHOOL_ADMIN
    ->
derived ACTIVE Connect ADMIN membership
    ->
canonical SCHOOL_PROFILE institution conversation.

Privileged membership in this canonical School conversation must not
become an independent shadow-authority system disconnected from EIAM.

---

## 18. ENGINEERING METHODOLOGY

GT Connect follows vertical end-to-end engineering:

Requirement
    ->
Database
    ->
Domain
    ->
Service
    ->
Security
    ->
API
    ->
Frontend
    ->
User Journey
    ->
E2E Test
    ->
Responsive / Mobile Check
    ->
Regression
    ->
Formal Closure

Completion must be reported separately as:

- Backend
- Frontend
- End-to-End

Source-file existence alone is not proof of completion.

---

## 19. STATUS CLASSIFICATION

Use these evidence classifications:

- DONE / VALIDATED
- IN PROGRESS
- RESERVED / FUTURE
- NOT VERIFIED
- NOT IMPLEMENTED

Future vision documented in this record MUST NOT be interpreted as
already implemented functionality.

In particular:

- live voice calling is future;
- live video calling is future;
- meetings are future;
- complete parent-to-parent user journey is future;
- true E2EE is future;
- full GT Connect Brain is future.

---

## 20. ADVANCED GT CONNECT RESERVATIONS

The following are preserved as controlled future directions:

### GT-CONNECT-E2EE-001
Governed End-to-End Encrypted Private Communication.

### GT-CONNECT-BRAIN-001
Intelligent Collaboration Brain & Memory.

### GT-CONNECT-BIOMETRIC-ID-001
Future multimodal protected identity capability incorporating appropriate
voice/facial/device/session/risk signals and MFA.

Biometric identity must not be the sole security factor for highly
sensitive information.

---

## 21. PRODUCT SCOPE

GT Connect is an ENTERPRISE capability.

It may ultimately be reused by:

- GT School;
- GT SACCO;
- GT Hospital;
- GT Restaurant;
- GT Supermarket;
- GT Church;
- GT AIP;
- other future GrowTogether products.

Product-specific rules remain in their respective product/domain layers.

The shared communication engine belongs to GT Connect.

---

## 22. RECOVERY RULE

This document exists specifically to prevent loss of GT Connect
institutional knowledge when:

- a ChatGPT conversation reaches its context limit;
- a conversation is deleted;
- a chat cannot be recovered;
- an engineer changes;
- an AI system changes;
- a device is lost;
- a temporary working note disappears.

This repository record SHALL take precedence over recollection from a
conversation when recovering the GT Connect programme.

AI-generated summaries may assist recovery but MUST NOT replace controlled
repository evidence.

---

## 23. CHANGE GOVERNANCE

Any material change to:

- GT Connect's enterprise responsibility;
- encryption model;
- safeguarding model;
- identity model;
- tenant boundary;
- document ownership;
- AI governance;
- parent/teacher relationship rules;
- shared-enterprise-service ownership;

must be explicitly reviewed before implementation.

Release 1 must remain protected from uncontrolled future-scope expansion.

---

## 24. CONTINUITY STATEMENT

The enduring vision is:

> GT Connect is GrowTogether's unified intelligent communication and
> collaboration ecosystem — bringing together chat, voice, video,
> meetings, communities, announcements, documents, workflows,
> notifications, intelligence and governed secure communication within
> one coherent GrowTogether experience.

The objective is not merely to match existing messaging applications.

The objective is to create a broader communication and collaboration
ecosystem deeply integrated with the wider GrowTogether Enterprise
Platform.

---


---

## 25. ECOSYSTEM-TO-ENGINEERING SYNCHRONIZATION RULE

GT Connect ecosystem architecture and GT Connect engineering SHALL move
together.

The ecosystem record is not a static future-vision document.

Every material GT Connect engineering capability must be checked against
the ecosystem direction during implementation.

The controlled cycle is:

Ecosystem Direction
    ->
Engineering Requirement
    ->
Backend
    ->
Frontend
    ->
End-to-End Validation
    ->
Formal Engineering Closure
    ->
Controlled Ecosystem Record Update
    ->
Git Evidence
    ->
Next Capability

This rule exists to ensure that:

- Release 1 implementation does not unintentionally block future GT
  Connect capabilities;
- new verified engineering developments are recoverable independently of
  conversation history;
- future capabilities are not falsely represented as implemented;
- architecture and implementation cannot silently diverge;
- another engineer or future AI session can determine both the intended
  destination and the verified implementation position.

Future ecosystem capability SHALL continue to be staged appropriately.

Moving the ecosystem and engineering together does NOT mean that every
reserved capability must be implemented in Release 1.

Release protection remains mandatory.

---

## 26. VERIFIED SCHOOL INSTITUTION MEMBERSHIP GOVERNANCE

Engineering evidence established a stronger governance rule for canonical
GT School institution conversations.

Canonical School institution space:

- Space Type: INSTITUTION
- Context Type: SCHOOL_PROFILE
- Context Reference: SchoolProfile identifier

For the canonical SCHOOL_PROFILE space:

- ordinary MEMBER membership may use the governed membership path;
- manual ADMIN assignment is prohibited;
- manual MODERATOR assignment is prohibited;
- manual OWNER assignment is prohibited;
- privileged institutional administration is derived from authoritative
  EIAM School roles.

The purpose is to prevent GT Connect from becoming an independent
shadow-authority system separate from EIAM.

Validated engineering gate:

**GT-CONNECT-R1-GAP-010H18**

Evidence:

- ConnectServiceTest: 37 tests;
- failures: 0;
- errors: 0;
- BUILD SUCCESS.

Status:

**BACKEND VALIDATED**

This evidence does not by itself establish completion of the corresponding
frontend or full end-to-end administrator lifecycle.

---

## 27. SCHOOL ADMINISTRATOR MEMBERSHIP DERIVATION

The authoritative architectural rule is:

EIAM SCHOOL_ADMIN
    ->
ACTIVE eligible EIAM user
    ->
canonical SCHOOL_PROFILE institution space
    ->
authoritatively derived Connect ADMIN membership.

The complete backend assignment and revocation lifecycle is now
implemented and verified.

### 27.1 Assignment lifecycle

When SCHOOL_ADMIN authority is assigned through an authoritative EIAM
role mutation:

- EIAM publishes a neutral UserRolesChangedEvent;
- GT School resolves the canonical SCHOOL_PROFILE institution space;
- a tenant without an onboarded School profile is handled as a safe
  no-op;
- an eligible active user with no active Connect membership receives an
  authoritative ADMIN membership;
- an existing ordinary MEMBER is promoted to ADMIN while preserving the
  previous ordinary role;
- repeated reconciliation is idempotent;
- Connect does not become an independent source of institutional
  administrator authority.

### 27.2 Authority provenance

gt_connect_space_members records reversible privileged-role provenance
using:

- role_authority_source;
- previous_member_role.

The current authoritative source identifier is:

EIAM_SCHOOL_ADMIN

This provenance distinguishes an ordinary Connect membership from a
privilege derived from an authoritative enterprise role.

### 27.3 Revocation lifecycle

When SCHOOL_ADMIN authority is removed:

- an authority-derived ADMIN membership that previously represented an
  ordinary membership is restored to its previous member role;
- an ADMIN membership created solely because of SCHOOL_ADMIN authority
  is removed from active membership;
- unrelated ordinary memberships are preserved;
- authority cannot be released by a mismatched authority source;
- historical provenance is retained where required for auditability.

The reconciliation service also excludes ineligible inactive users when
reconciliation occurs.

Account-status mutation events are not represented by this evidence as
an independently completed lifecycle trigger. The verified trigger in
this release is authoritative EIAM role mutation.

### 27.4 Initial provisioning alignment

Initial School administrator provisioning and later live role-change
reconciliation use the same authoritative provenance model.

This prevents onboarding from creating a second, incompatible
administrator-membership rule.

### 27.5 Database migration

V172__add_gt_connect_member_role_authority_provenance.sql introduces
the authority-provenance persistence foundation and performs controlled
historical normalization.

PostgreSQL/Testcontainers validation confirmed that:

- the migration applies successfully;
- the provenance columns and constraints are created;
- an eligible historical SCHOOL_ADMIN Connect ADMIN is normalized;
- an ordinary ADMIN without authoritative SCHOOL_ADMIN is not
  normalized;
- a suspended SCHOOL_ADMIN user is not normalized;
- an existing MEMBER is not incorrectly converted by the historical
  backfill.

### 27.6 Historical existing-administrator reconciliation

Browser end-to-end validation exposed an historical-data condition not
covered by the original role-change lifecycle:

- the School Profile already existed;
- V168 had correctly provisioned its canonical SCHOOL_PROFILE
  institution space;
- the EIAM user already held SCHOOL_ADMIN and was ACTIVE;
- no active GT Connect membership existed for that administrator;
- no new EIAM role mutation occurred after live reconciliation was
  introduced, so no UserRolesChangedEvent was available to trigger
  membership creation.

V172 could normalize qualifying existing ADMIN memberships, but it did
not create a missing historical membership.

The durable repair is:

V173__backfill_existing_school_admin_connect_memberships.sql

V173 aligns historical data with the verified runtime authority model.

For each canonical SCHOOL_PROFILE institution space it:

- creates an authoritative ACTIVE ADMIN membership for an ACTIVE
  SCHOOL_ADMIN who has no active Connect membership;
- promotes an eligible existing ordinary membership to ADMIN while
  preserving the previous ordinary role for later restoration;
- attaches EIAM_SCHOOL_ADMIN provenance to an eligible ordinary ADMIN;
- excludes inactive EIAM users;
- excludes users without SCHOOL_ADMIN;
- does not overwrite a membership governed by another authority source.

The migration was first executed against the live development schema
inside an explicit transaction and rolled back. During that simulation
the missing administrator membership was created correctly and the
post-rollback active membership count returned to zero.

Automated PostgreSQL/Testcontainers validation then confirmed V173
against a database starting deliberately at V172.

The migration was subsequently applied normally through Flyway to the
development database and recorded successfully as version 173.

Live database verification established:

- member_role = ADMIN;
- membership_status = ACTIVE;
- role_authority_source = EIAM_SCHOOL_ADMIN;
- previous_member_role = NULL for the newly created historical
  membership;
- created_by = system.

### 27.7 School administrator browser end-to-end validation

The SCHOOL_ADMIN user journey has now been browser-validated through:

SCHOOL_ADMIN
    ->
School Administration Portal
    ->
Communication
    ->
GT Connect
    ->
authorised Conversations
    ->
GT School INSTITUTION space
    ->
conversation selection.

Before V173, the browser correctly displayed no authorised GT Connect
conversations because the historical SCHOOL_ADMIN had no active Connect
membership.

After V173 was applied through Flyway, the same browser journey displayed
the GT School INSTITUTION conversation and allowed it to be selected.

The resulting empty message panel is a valid conversation state because
the institution space currently contains no messages.

Engineering gate:

GT-CONNECT-R1-E2E-001D-R8

Status:

**MY CONVERSATIONS SCHOOL_ADMIN BROWSER E2E CLOSED.**

### 27.8 Verified evidence

Original focused lifecycle regression:

GT-CONNECT-R1-GAP-010H22Q

- tests: 29;
- failures: 0;
- errors: 0;
- BUILD SUCCESS.

V173 PostgreSQL/Flyway migration validation:

GT-CONNECT-R1-E2E-001D-R6

- tests: 1;
- failures: 0;
- errors: 0;
- skipped: 0;
- BUILD SUCCESS.

Strengthened SCHOOL_ADMIN lifecycle regression including V173:

GT-CONNECT-R1-E2E-001D-R9

- tests: 30;
- failures: 0;
- errors: 0;
- skipped: 0;
- BUILD SUCCESS.

Live V173 application and verification:

GT-CONNECT-R1-E2E-001D-R7A

- Flyway version: 173;
- migration success: true;
- authoritative ADMIN membership: verified;
- authority source: EIAM_SCHOOL_ADMIN.

Browser end-to-end validation:

GT-CONNECT-R1-E2E-001D-R8

- SCHOOL_ADMIN authentication: verified;
- Communication -> GT Connect navigation: verified;
- authorised GT School INSTITUTION conversation discovery: verified;
- conversation selection: verified;
- My Conversations browser E2E: CLOSED.

V172 PostgreSQL/Flyway validation:

GT-CONNECT-R1-GAP-010H22P

- tests: 1;
- failures: 0;
- errors: 0;
- BUILD SUCCESS.

Integrated backend regression:

GT-BACKEND-CHECKPOINT-001F

- tests: 883;
- failures: 0;
- errors: 0;
- BUILD SUCCESS.

Controlled Git recovery checkpoint:

edce4d5 — checkpoint(backend): preserve integrated green baseline

Status:

**SCHOOL_ADMIN MEMBERSHIP LIFECYCLE BACKEND CLOSED.**

**MY CONVERSATIONS SCHOOL_ADMIN BROWSER E2E CLOSED.**

The controlled recovery commit containing this record also contains the
V173 historical-data repair and its PostgreSQL regression test. This
preserves the verified implementation independently of conversation
history.

---

## 28. ENGINEERING EVIDENCE DISCIPLINE

GT Connect capability status SHALL use separate evidence columns:

- Backend;
- Frontend;
- End-to-End.

A capability is not considered fully implemented simply because:

- a source file exists;
- a database table exists;
- compilation succeeds;
- a backend unit test succeeds.

Formal completion requires the evidence appropriate to the capability.

The engineering team SHALL preserve green test gates and update this
controlled record when material implementation state changes.


## 29. RELEASE 1 BROWSER MESSAGING AND NOTIFICATION LIFECYCLE

GT Connect Release 1 browser messaging has progressed from backend/API
availability to a verified two-user browser communication lifecycle.

The verified Release 1 browser capability now includes:

- authenticated SCHOOL_ADMIN and PARENT browser sessions;
- authorised GT School INSTITUTION conversation discovery;
- browser message composition and sending;
- persisted two-user message exchange;
- delivery and read receipts;
- bidirectional near-real-time conversation synchronization;
- background-tab synchronization while GT Connect remains mounted;
- correct Delivered versus Read semantics;
- audible incoming-message notification;
- Sound On / Muted user control;
- persistent mute preference across browser refresh;
- localhost development CORS support for the verified Vite origins;
- username-or-email browser login alignment.

This section records only capabilities that were actually exercised through
the browser and/or supported by authoritative database evidence.

### 29.1 Browser authentication and CORS alignment

The GT School login experience now accepts either username or email in the
browser while preserving the EIAM `usernameOrEmail` API contract.

The controlled development CORS configuration was also extended to permit:

- `http://localhost:5173`;
- `http://localhost:5174`.

A dedicated CORS regression test was added.

Browser authentication was subsequently proven using the real PARENT user
journey.

Status:

**GT CONNECT BROWSER AUTHENTICATION AND CORS ALIGNMENT VERIFIED.**

### 29.2 Real two-user browser messaging

The controlled two-user journey was exercised using:

- SCHOOL_ADMIN;
- PARENT;
- the authorised GT School INSTITUTION conversation.

The verified browser lifecycle included:

- administrator message creation;
- parent receipt and read acknowledgement;
- parent reply;
- administrator receipt and read acknowledgement;
- browser rendering of both directions.

Relevant controlled engineering gates included:

- GT-CONNECT-R1-E2E-002;
- GT-CONNECT-R1-E2E-003D;
- GT-CONNECT-R1-E2E-003E;
- GT-CONNECT-R1-E2E-003F;
- GT-CONNECT-R1-E2E-003G.

Status:

**REAL TWO-USER BROWSER MESSAGING VERIFIED.**

### 29.3 Near-real-time conversation synchronization

Initial browser behaviour required manual Refresh to discover a message sent
by another participant.

Repository conformity review confirmed that no polling, WebSocket, SSE or
other automatic refresh mechanism existed in the GT Connect browser
component.

Release 1 therefore introduced a deliberately small synchronization
mechanism using a 10-second silent polling interval.

The implementation:

- reloads the currently selected conversation;
- avoids normal loading-state flicker during silent synchronization;
- cleans up its interval when the component lifecycle changes;
- does not require manual Refresh for newly received messages.

Controlled two-browser validation proved both:

- SCHOOL_ADMIN -> PARENT automatic appearance;
- PARENT -> SCHOOL_ADMIN automatic appearance.

Relevant gates included:

- GT-CONNECT-R1-E2E-003H16;
- GT-CONNECT-R1-E2E-003H17;
- GT-CONNECT-R1-E2E-003H18;
- GT-CONNECT-R1-E2E-003H19.

Status:

**GT-CONNECT-R1-LIVESYNC-001 — BIDIRECTIONAL NEAR-REAL-TIME
SYNCHRONIZATION CLOSED.**

The Release 1 polling implementation is not represented as the final
long-term real-time architecture. Future WebSocket, SSE, push or equivalent
architecture may supersede it through normal GT change governance.

### 29.4 Background Delivered and visible Read semantics

Background synchronization introduced an important receipt-governance
requirement.

GT Connect SHALL NOT mark a message Read merely because a hidden browser tab
received it.

The Release 1 browser implementation therefore distinguishes:

- hidden browser tab -> Delivered;
- visible selected conversation -> Read.

The frontend now consumes the existing backend Delivered endpoint separately
from the Read endpoint.

Controlled browser and PostgreSQL evidence proved that a message received
while the parent GT School tab was hidden produced:

- an authoritative `delivered_at` timestamp;
- `read_at = NULL`.

After the parent returned to the visible GT School conversation, the same
receipt transitioned to:

- the original `delivered_at` timestamp retained;
- a later authoritative `read_at` timestamp.

The final controlled evidence message had:

- message ID:
  `965a75f1-ed2d-4f78-a626-be18881796fa`;
- Delivered:
  `2026-09-01 14:31:37.496255+03`;
- Read:
  `2026-09-01 14:54:59.337741+03`.

Relevant gates included:

- GT-CONNECT-R1-NOTIFY-001H;
- GT-CONNECT-R1-NOTIFY-001I;
- GT-CONNECT-R1-NOTIFY-001J;
- GT-CONNECT-R1-NOTIFY-001L.

Status:

**BACKGROUND DELIVERED -> VISIBLE READ LIFECYCLE VERIFIED.**

### 29.5 Audible incoming-message notification

GT Connect Release 1 now provides a short browser-generated audible tone for
a genuinely new incoming message.

The implementation deliberately avoids notification sound for:

- the user's own outgoing message;
- messages already known to the current browser session;
- initial conversation baseline loading.

The browser audio implementation is non-blocking. Failure or browser policy
restriction affecting audio SHALL NOT interrupt message delivery.

Controlled E2E validation proved:

- foreground incoming-message beep;
- background-tab incoming-message beep while GT Connect remains open;
- message synchronization continues independently of the audio preference.

Status:

**GT-CONNECT-R1-NOTIFY-001 — AUDIBLE MESSAGE NOTIFICATION VERIFIED.**

### 29.6 Sound preference

GT Connect exposes:

- Sound On;
- Muted.

The preference is stored locally in the browser and survives browser refresh.

Controlled validation proved that while Muted:

- incoming messages continue to synchronize normally;
- the notification tone is suppressed.

After browser refresh, the Muted preference remained active.

Relevant gates included:

- GT-CONNECT-R1-NOTIFY-001M;
- GT-CONNECT-R1-NOTIFY-001N.

Status:

**SOUND ON / MUTED AND PERSISTENT MUTE PREFERENCE VERIFIED.**

### 29.7 Notification boundary and mobile reservation

The current Release 1 notification implementation is browser based.

It does NOT yet constitute:

- operating-system push notification when GT is completely closed;
- native mobile push notification;
- Service Worker / PWA push delivery;
- guaranteed vibration when the phone is in silent mode.

The following capability is therefore reserved:

GT-CONNECT-R1-NOTIFY-VIB-001

**Mobile Vibration Notification**

Status:

**RESERVED — NOT IMPLEMENTED.**

Future mobile/PWA notification work SHALL respect operating-system sound,
silent-mode, vibration and user-permission policies rather than attempting to
bypass them.

### 29.8 Controlled recovery evidence

The verified browser messaging milestone was preserved in the controlled Git
commit:

`871570c — feat(connect): complete browser messaging lifecycle`

The commit was created surgically using only the seven verified target files
so that unrelated staged GrowTogether engineering work was not swept into
the checkpoint.

The preserved scope includes:

- browser login alignment;
- GT Connect browser dashboard;
- message composer and browser API/service path;
- near-real-time synchronization;
- Delivered / Read browser handling;
- audible notification and persistent mute preference;
- CORS development-origin repair;
- CORS regression test.

Engineering status:

| Capability | Backend | Frontend | End-to-End |
|---|---|---|---|
| Browser authentication | VERIFIED | VERIFIED | VERIFIED |
| Message send / persistence | VERIFIED | VERIFIED | VERIFIED |
| Bidirectional near-real-time synchronization | VERIFIED | VERIFIED | VERIFIED |
| Delivered / Read lifecycle | VERIFIED | VERIFIED | VERIFIED |
| Foreground audible notification | N/A | VERIFIED | VERIFIED |
| Background audible notification | N/A | VERIFIED | VERIFIED |
| Sound mute / persistence | N/A | VERIFIED | VERIFIED |
| Mobile vibration | RESERVED | NOT IMPLEMENTED | NOT VERIFIED |

Formal milestone:

**GT-CONNECT-R1-BROWSER-MSG-001 — COMPLETE BROWSER MESSAGING &
NOTIFICATION LIFECYCLE — CLOSED.**

Recovery commit:

**871570c**

---



## 30. Release 1 Runtime and Authentication Security Hardening

This section records the controlled production-readiness hardening completed
after the Release 1 browser messaging milestone.

The work covered runtime dependency health, browser session continuity,
failed-login security persistence, and rejected-refresh session revocation.

These capabilities are enterprise EIAM/runtime foundations consumed by
GT Connect and other GrowTogether products.

### 30.1 Governed Redis runtime and backend health

GT runtime conformity established that Redis was already defined in the
authoritative repository through the governed Compose configuration:

- service: `redis`;
- image: `redis:8-alpine`;
- host port: `6379`;
- container health check: `redis-cli ping`.

Before Redis was running, the application showed:

- aggregate `/actuator/health` = `DOWN`;
- HTTP status = `503`;
- liveness/readiness remained independently available.

The existing governed Redis service was started through Docker Compose.

Controlled runtime evidence then proved:

- Redis container status = running;
- Redis Docker health = healthy;
- port `6379` = listening;
- aggregate `/actuator/health` = `UP`;
- aggregate health HTTP status = `200`;
- `/actuator/health/readiness` = `UP`;
- readiness HTTP status = `200`.

No Redis software was installed directly on the host and no application
source modification was required.

Status:

**GT-CONNECT-R1-OPS-008 — REDIS RUNTIME & BACKEND HEALTH INCIDENT —
CLOSED.**

### 30.2 Live browser session refresh and token rotation

GT Connect browser session continuity was validated against the real EIAM
refresh flow.

The authoritative configuration provides:

- access-token lifetime: 900 seconds by default;
- refresh-token lifetime: 2,592,000 seconds by default;
- server-side refresh-session storage using refresh-token hashes;
- refresh-token rotation on successful refresh.

The frontend already contained controlled 401 recovery logic that:

1. detects an eligible unauthorized request;
2. invokes `/api/v1/eiam/auth/refresh`;
3. stores the rotated access and refresh credentials;
4. retries the original request once;
5. falls back to login only when refresh cannot recover the session.

Controlled browser validation deliberately replaced only the parent browser
access token with an invalid test value while preserving the refresh token.

The browser subsequently replaced the invalid access token automatically
without requiring the parent to sign in again.

Server-side evidence proved that the same session:

`41574d5c-e6f6-43d5-a426-bb8e0ebe782d`

advanced from:

- `last_used_at = 2026-09-01 22:19:02.419144+03`;
- `expires_at = 2026-10-01 22:19:02.419144+03`;

to:

- `last_used_at = 2026-09-02 23:11:43.810539+03`;
- `expires_at = 2026-10-02 23:11:43.810539+03`.

No access token, refresh token or password was exposed in controlled
evidence.

Status:

**GT-CONNECT-R1-AUTH-009 — LIVE SESSION REFRESH & ROTATION — CLOSED.**

### 30.3 Failed-login persistence and lockout security

A production-readiness defect was confirmed in the original authentication
transaction.

On an incorrect password the authentication service:

1. mutated `failed_login_attempts`;
2. immediately threw `AuthenticationException`;
3. caused the surrounding transaction to roll back.

The HTTP response therefore correctly returned `401`, while the intended
security-state mutation could be lost.

The repair introduced an independent authentication security-state
transaction using the existing GrowTogether `REQUIRES_NEW` pattern.

The hardened implementation now:

- records failed-login state in an independent transaction;
- uses a locked account lookup for atomic security updates;
- preserves configured lockout state;
- allows the outer authentication request to fail normally;
- retains normal successful-login reset behavior.

Controlled PostgreSQL regression proved:

- one failed login persists one failed attempt;
- the configured failure threshold persists lockout;
- a locked account rejects even the correct password;
- successful authentication resets the failed-attempt state.

Targeted and broader authentication regression gates passed:

- initial targeted security suite: 5/5;
- broader authentication/EIAM regression: 16/16.

Live HTTP and PostgreSQL validation against the running backend proved:

- initial `failed_login_attempts = 0`;
- deliberately incorrect password returned `GT-EIAM-AUTH-401`;
- HTTP status = `401`;
- `failed_login_attempts` persisted as `1`;
- the account remained ACTIVE;
- after a subsequent legitimate login the counter returned to `0`;
- `locked_until` remained clear;
- `last_login_at` advanced normally.

The controlled repair was preserved in:

`e5c7b3a — fix(eiam): persist failed login security state`

Status:

**GT-CONNECT-R1-AUTH-010 — FAILED-LOGIN PERSISTENCE & LOCKOUT SECURITY —
CLOSED.**

### 30.4 Rejected-refresh session revocation security

A second rollback defect was identified in rejected refresh handling.

When an existing refresh session became unacceptable because:

- the account was no longer available; or
- MFA became required for a session that had not been MFA verified;

the original refresh implementation mutated the session with:

- `ACCOUNT_UNAVAILABLE`; or
- `MFA_REQUIRED`;

and then threw `AuthenticationException`.

A PostgreSQL regression first reproduced the defect and proved that
`revoked_at` remained NULL after the rejected refresh.

The repair extended the independent authentication security-state mechanism
so security-triggered session revocation is persisted through a
`REQUIRES_NEW` transaction before the outer refresh request is rejected.

PostgreSQL regression then proved both branches:

- `ACCOUNT_UNAVAILABLE` revocation persists;
- `MFA_REQUIRED` revocation persists.

The focused revocation suite passed 2/2.

The broader authentication, MFA and account regression gate passed:

- 22 tests;
- 0 failures;
- 0 errors;
- 0 skipped.

A governed live validation used a disposable EIAM account created and
activated through the normal user lifecycle APIs.

The test established refresh session:

`886f3d37-b9b3-4036-a546-8a46e821682e`

while the account was ACTIVE and the session was unrevoked.

The disposable account was then suspended through the governed EIAM
suspension API.

Before refresh:

- account status = SUSPENDED;
- `revoked_at = NULL`;
- `revoke_reason = NULL`.

The subsequent real refresh request returned:

- HTTP `401`;
- code `GT-EIAM-AUTH-401`;
- message `Account is not available for authentication.`

Server-side PostgreSQL evidence then showed:

- `revoked_at = 2026-09-03 09:10:49.649327+03`;
- `revoke_reason = ACCOUNT_UNAVAILABLE`;
- the prior `last_used_at` was retained.

The disposable test account was subsequently deactivated through the
governed EIAM lifecycle API and its temporary private credential file was
removed.

The controlled repair was preserved in:

`4dc3f22 — fix(eiam): persist rejected refresh revocations`

Status:

**GT-CONNECT-R1-AUTH-011 — REJECTED REFRESH SESSION SECURITY — CLOSED.**

### 30.5 Production-readiness recovery chain

The browser, runtime and authentication hardening sequence is recoverable
through the following controlled Git checkpoints:

- `871570c — feat(connect): complete browser messaging lifecycle`;
- `b0766f8 — docs(connect): record browser messaging lifecycle`;
- `e5c7b3a — fix(eiam): persist failed login security state`;
- `4dc3f22 — fix(eiam): persist rejected refresh revocations`.

Engineering status:

| Capability | Backend | Frontend | End-to-End |
|---|---|---|---|
| Redis runtime dependency | VERIFIED | N/A | VERIFIED |
| Aggregate backend health | VERIFIED | N/A | VERIFIED |
| Browser session refresh | VERIFIED | VERIFIED | VERIFIED |
| Refresh-token rotation | VERIFIED | VERIFIED | VERIFIED |
| Failed-login persistence | VERIFIED | N/A | VERIFIED |
| Account lockout persistence | VERIFIED | N/A | VERIFIED |
| Successful-login security reset | VERIFIED | VERIFIED | VERIFIED |
| ACCOUNT_UNAVAILABLE refresh revocation | VERIFIED | N/A | VERIFIED |
| MFA_REQUIRED refresh revocation | VERIFIED | N/A | VERIFIED |

Formal security milestone:

**GT-CONNECT-R1-AUTH-009 / AUTH-010 / AUTH-011 — RELEASE 1
AUTHENTICATION SESSION & LOGIN SECURITY HARDENING — CLOSED.**

---


**END OF CONTROLLED RECORD — GT-CONNECT-ECOSYSTEM-001**
