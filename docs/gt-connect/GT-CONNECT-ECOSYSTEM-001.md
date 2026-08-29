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

### 27.6 Verified evidence

Focused lifecycle regression:

GT-CONNECT-R1-GAP-010H22Q

- tests: 29;
- failures: 0;
- errors: 0;
- BUILD SUCCESS.

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

**BACKEND CLOSED AND RECOVERY-PROTECTED**

This status does not by itself establish frontend or browser end-to-end
completion of administrator user journeys.

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


**END OF CONTROLLED RECORD — GT-CONNECT-ECOSYSTEM-001**
