# GT-AI-CAPABILITY-REGISTER-001 — AI Capability Master Register

**Programme:** GrowTogether Enterprise Platform
**Authority:** GT Headquarters / Enterprise Programme Management Office
**Reference:** GT-AI-CAPABILITY-REGISTER-001
**Classification:** Enterprise AI Governance / Capability Continuity
**Status:** ESTABLISHED — FOUNDATION BASELINE
**Parent Continuity Framework:** GT-KPC-001

---

## 1. Purpose

GT-AI-CAPABILITY-REGISTER-001 is the controlled master register for
GrowTogether AI capabilities.

Its purpose is to preserve AI programme continuity across releases,
repositories, products, workstreams, successor chats and future implementation
cycles.

Absence of source code is not evidence that an approved or preserved AI
capability was cancelled.

The register distinguishes requirement authority, implementation evidence,
verification evidence and lifecycle status.

---

## 2. Governing evidence chain

Each capability shall progressively trace:

Requirement
→ Original Decision
→ Owning Programme
→ Shared Enterprise Foundation
→ Product Implementation
→ Release
→ Backend State
→ Frontend State
→ End-to-End State
→ Repository Path
→ Tests
→ Commit SHA
→ Recovery Evidence
→ Current Lifecycle Status

Where evidence is missing, the register records the gap explicitly.

No missing field may be silently inferred as implemented or verified.

---

## 3. Lifecycle interpretation

| Status | Controlled meaning |
|---|---|
| VERIFIED | Appropriate implementation and evidence have been proven. |
| IMPLEMENTED | Engineering exists, but verification must still be recorded separately. |
| PARTIAL | Some engineering or evidence exists; full closure has not been proven. |
| APPROVED / PRESERVED | Controlled requirement exists and remains active. |
| RESERVED | Future governed capability; not forced into the current release. |
| DISCOVERY | Controlled exploration or definition remains active. |
| NOT YET VERIFIED | Current repository evidence does not prove implementation or completion. |
| NOT IMPLEMENTED | Requirement is controlled but engineering has not been established. |
| SUPERSEDED | Replaced only through explicit governed authority while retaining traceability. |

---

## 4. Repository conformity result at establishment

A repository-wide controlled search was performed before establishment.

Result:

- no pre-existing `GT-AI-CAPABILITY-REGISTER-001` implementation was found;
- no tracked repository record for the early School AI capability set was found;
- `GT-KPC-001` contains the controlled requirement to establish this register;
- current absence from source code is therefore classified as
  **NOT YET VERIFIED**, not rejected or cancelled.

The initial capability records below are recovered from the controlled
`HQ-GT-CONT-HO-001` Revision 1.1 handover baseline.

Original decision artefacts and detailed repository paths must be linked when
they are recovered or implemented.

---

## 5. Early GrowTogether School AI capability register

### 5.1 GT-ILE-DISC-001 — Intelligent Learning Ecosystem

**Intent:** Umbrella programme for a comprehensive AI-powered learning
ecosystem serving learners, teachers and school leadership.

**Owning programme:** GT School / Intelligent Learning Ecosystem

**Shared enterprise relationship:** May consume reusable GT-AIP enterprise AI
foundations where appropriate.

**Current controlled status:** APPROVED / PRESERVED

**Implementation status:** INCOMPLETE

**Repository verification:** NOT YET VERIFIED

**Release position:** Advanced / governed future School release

**Continuity rule:** Must not be reduced to a single chatbot capability.

---

### 5.2 AI Teacher / Tutor

**Intent:** Conversational AI teaching support able to explain learning
content, answer learner questions and support guided learning in an age- and
curriculum-aware manner.

**Owning programme:** GT School / Intelligent Learning Ecosystem

**Current controlled status:** PRESERVED REQUIREMENT

**Implementation status:** NOT VERIFIED AS COMPLETE R1 IMPLEMENTATION

**Backend:** NOT YET VERIFIED

**Frontend:** NOT YET VERIFIED

**End-to-End:** NOT YET VERIFIED

**Repository path:** NOT YET RECOVERED / VERIFIED

**Tests:** NOT YET VERIFIED

**Commit:** NOT YET VERIFIED

---

### 5.3 AI Learning Assistant

**Intent:** Learner-facing assistance for explanation, revision, guided
practice, homework support and continued question-and-answer learning
workflows.

**Owning programme:** GT School / Intelligent Learning Ecosystem

**Current controlled status:** PRESERVED REQUIREMENT

**Implementation status:** ADVANCED-RELEASE IMPLEMENTATION PENDING

**Backend:** NOT YET VERIFIED

**Frontend:** NOT YET VERIFIED

**End-to-End:** NOT YET VERIFIED

---

### 5.4 AI Visual Learning

**Intent:** AI-supported visual and multimedia explanation of difficult
learning concepts, including science, biology and physics examples such as
digestion, blood circulation and motion.

**Owning programme:** GT School / Intelligent Learning Ecosystem

**Current controlled status:** PRESERVED REQUIREMENT

**Implementation status:** INCOMPLETE

**Backend:** NOT YET VERIFIED

**Frontend / visual experience:** NOT YET VERIFIED

**End-to-End:** NOT YET VERIFIED

---

### 5.5 Teacher Assistant

**Intent:** Teacher-facing AI assistance intended to reduce teacher workload
and support lesson preparation, instructional support and related classroom
workflows.

**Owning programme:** GT School

**Current controlled status:** DISCOVERY PRESERVED

**Detailed scope:** PENDING RECOVERY / GOVERNANCE

**Implementation status:** NOT YET VERIFIED

---

### 5.6 Personalized / Adaptive Learning

**Intent:** Adapt explanations, practice and learning support to learner
context and progress while retaining human and teacher oversight.

**Owning programme:** GT School / Intelligent Learning Ecosystem

**Current controlled status:** PRESERVED ADVANCED CAPABILITY

**Implementation status:** NOT VERIFIED AS CURRENT R1 CODE

**Human accountability:** REQUIRED

---

### 5.7 AI Assessment / Quiz Assistance

**Intent:** AI-assisted learning checks, quizzes and assessment-support
workflows linked to the wider intelligent-learning ecosystem.

**Owning programme:** GT School / Intelligent Learning Ecosystem

**Current controlled status:** PRESERVED ADVANCED CAPABILITY

**Implementation status:** NOT YET VERIFIED

**Teacher / governance control:** REQUIRED

---

## 6. Advanced GT School integration

### 6.1 GT-SCH-ADV-001

**Intent:** Advanced GrowTogether School programme integrating prior AI
Teacher, AI Learning Assistant, AI Visual Learning and related discoveries.

**Current controlled status:** APPROVED CONTINUITY RULE

**Release position:** POST-R1 / GOVERNED ADVANCED RELEASE

**Duplication rule:** Existing School AI discoveries must be incorporated,
reused or explicitly superseded through governance rather than recreated
without conformity review.

---

## 7. Enterprise AI foundation

### 7.1 GT-AIP-001 — Advanced Intelligence & Project Execution Platform

**Role:** Enterprise AI and project-execution foundation capable of providing
reusable intelligence services to GrowTogether products.

**Current controlled status:** CONTROLLED PROGRAMME

**Production authority:** GOVERNED / NOT INFERRED FROM THIS REGISTER

**Reuse rule:** Generic AI foundations should be built once at enterprise
level where appropriate and reused by product programmes.

**Education ownership boundary:** School-specific pedagogy, curriculum
behaviour, learner workflows and teacher accountability remain under GT School
and the Intelligent Learning Ecosystem.

---

## 8. Specialist and future AI programmes

### 8.1 WP-AIP-13 — Built Environment AI

**Relationship:** Specialist GT-AIP workstream for Built Environment
Intelligence.

**Current controlled status:** RESERVED SPECIALIST CAPABILITY

**Operational boundary:** Human professional approval required for
safety-critical professional outputs.

**Relationship to School AI:** Does not replace or absorb education AI
ownership.

---

### 8.2 WP-AIP-14 — Defence & Public Safety Intelligence

**Relationship:** Separate specialist GT-AIP discovery programme.

**Current controlled status:** DISCOVERY / RESERVED

**Engineering authority:** NOT ESTABLISHED BY THIS REGISTER

**Operational authority:** NOT GRANTED

**Safety boundary:** No autonomous lethal targeting.

**Relationship to School AI:** Must not distort or absorb education AI
ownership.

---

### 8.3 GT-AIP-FPI-001 — Founder & Project Intelligence

**Intent:** Founder / HQ continuity and intelligence environment supporting
programme memory, research, decisions and execution.

**Current controlled status:** PRESERVED FUTURE CAPABILITY

**Implementation status:** NOT YET VERIFIED

**Relationship to GT-KPC-001:** May consume governed continuity records but
must not replace the authoritative continuity system.

---

### 8.4 GT-AIP-DISC-ENT-002 — Enterprise AI Operations & Management Assistant

**Intent:** Future enterprise operational intelligence covering payments,
subscriptions, onboarding, customer success and executive reporting.

**Current controlled status:** DISCOVERY PRESERVED

**Implementation status:** NOT YET VERIFIED

---

## 9. Capability summary

| Capability | Owning programme | Controlled status | Repository verification |
|---|---|---|---|
| GT-ILE-DISC-001 | GT School | APPROVED / PRESERVED | NOT YET VERIFIED |
| AI Teacher / Tutor | GT School | PRESERVED REQUIREMENT | NOT YET VERIFIED |
| AI Learning Assistant | GT School | PRESERVED REQUIREMENT | NOT YET VERIFIED |
| AI Visual Learning | GT School | PRESERVED REQUIREMENT | NOT YET VERIFIED |
| Teacher Assistant | GT School | DISCOVERY PRESERVED | NOT YET VERIFIED |
| Personalized / Adaptive Learning | GT School | PRESERVED ADVANCED CAPABILITY | NOT YET VERIFIED |
| AI Assessment / Quiz Assistance | GT School | PRESERVED ADVANCED CAPABILITY | NOT YET VERIFIED |
| GT-SCH-ADV-001 | GT School | APPROVED CONTINUITY RULE | NOT YET VERIFIED |
| GT-AIP-001 | Enterprise / GT-AIP | CONTROLLED PROGRAMME | NOT YET VERIFIED HERE |
| WP-AIP-13 | GT-AIP | RESERVED SPECIALIST CAPABILITY | NOT YET VERIFIED |
| WP-AIP-14 | GT-AIP | DISCOVERY / RESERVED | NOT YET VERIFIED |
| GT-AIP-FPI-001 | GT-AIP / HQ | PRESERVED FUTURE CAPABILITY | NOT YET VERIFIED |
| GT-AIP-DISC-ENT-002 | Enterprise / GT-AIP | DISCOVERY PRESERVED | NOT YET VERIFIED |

---

## 10. Duplication and reuse rule

Before creating a new AI service or capability:

1. search this register;
2. inspect the owning programme;
3. inspect existing GT-AIP/shared enterprise capability;
4. inspect product-specific implementation;
5. determine whether reuse, extension or new capability is required;
6. preserve original traceability;
7. record explicit supersession if HQ authorises replacement.

No AI capability shall be duplicated merely because its earlier engineering is
not visible in the current release.

---

## 11. Evidence recovery rule

Where an original AI decision, implementation, test or repository reference is
not yet recovered:

- retain the capability;
- preserve its controlled classification;
- mark the missing evidence;
- recover from authoritative programme records;
- do not infer implementation;
- do not infer cancellation;
- do not redesign from scratch before conformity review.

Repository evidence takes precedence over conversational memory for
implementation and verification state.

Controlled HQ / EPMO programme decisions remain authoritative for capability
existence and governance status.

---

## 12. Human accountability rule

AI capabilities must remain subject to appropriate human accountability.

Examples include:

- teachers for education decisions and learning oversight;
- school leadership for institutional governance;
- professionals for specialist technical outputs;
- authorised government or organisational officials for public-sector
  decisions;
- HQ / EPMO for programme and release authority.

AI assistance does not independently create authoritative approval.

---

## 13. Integration with GT-KPC-001

GT-AI-CAPABILITY-REGISTER-001 is a specialised controlled register under the
wider `GT-KPC-001` continuity framework.

GT-KPC-001 governs:

- continuity architecture;
- evidence provenance;
- lifecycle preservation;
- handovers;
- recovery;
- authoritative-record boundaries.

This register governs:

- AI capability identity;
- AI ownership;
- AI lifecycle state;
- AI implementation evidence;
- AI verification evidence;
- AI reuse and duplication control.

---

## 14. Establishment state

At foundation establishment:

- the early GrowTogether School AI programme is explicitly preserved;
- missing current repository implementation is not classified as rejection;
- enterprise GT-AIP relationships are preserved;
- specialist AI workstreams retain their separate ownership;
- no capability is promoted to VERIFIED without evidence;
- no capability is cancelled because current Release 1 code is absent;
- future repository paths, tests and commits must be added as evidence is
  recovered or engineering proceeds.

---

**END OF CONTROLLED RECORD — GT-AI-CAPABILITY-REGISTER-001**
