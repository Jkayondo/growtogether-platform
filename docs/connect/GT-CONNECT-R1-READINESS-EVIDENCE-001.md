# GT Connect Release 1 Readiness Evidence Pack

**Reference:** `GT-CONNECT-R1-EVIDENCE-001`
**Programme:** GrowTogether Enterprise Platform / GT Connect
**Release:** Release 1
**Status:** READINESS EVIDENCE COMPILED — RELEASE AUTHORITY NOT GRANTED

---

## 1. Purpose

This evidence pack records the controlled engineering evidence supporting
GT Connect Release 1 readiness assessment.

It does not itself grant Release Candidate, pilot, production, deployment,
or operational authority.

---

## 2. Governance Boundary

GT Connect Release 1 has been developed and verified under the controlled
GrowTogether engineering continuity process.

Evidence classifications distinguish implemented and verified capability
from release authority.

Historical verified evidence is preserved and is not downgraded merely
because a later workspace did not reproduce the original execution.

---

## 3. Release 1 Verified Capability Boundary

### 3.1 Messaging and Space Foundation

Release 1 includes the established GT Connect messaging and authorised-space
foundation, including controlled institution and relationship-based access.

### 3.2 Delivery to Read Lifecycle

`GT-CONNECT-VS-001`

Status: **CLOSED / VERIFIED**

Verified current targeted lifecycle boundary includes:

- incoming message receipt;
- Delivered lifecycle handling;
- Read transition when the conversation is visible;
- hidden-tab Delivered behaviour;
- Read transition after browser visibility returns.

Browser/background behaviour remains subject to the documented lifecycle
limitations in Section 10.

### 3.3 Institution Membership Management

`GT-CONNECT-VS-002`

Status: **CLOSED / VERIFIED / COMMITTED**

Commit:

`065df650786972030ed1542e1782264f74850fd1`
`feat(connect): add institution membership management`

Verified capability includes:

- institution-member candidate search;
- tenant-scoped candidate eligibility;
- active-account filtering;
- exclusion of existing active members;
- institution membership creation;
- manager-authorised frontend workflow;
- controlled reuse of EIAM without exposing broader EIAM user-read
  permission to the Connect browser.

### 3.4 Shared Authentication and Session Recovery

`GT-CONNECT-VS-003`

Status: **CLOSED / VERIFIED / COMMITTED**

Commit:

`8e9da44ac82ce33fe04c9331b39d15c48a8cd327`
`fix(connect): implement frontend session recovery`

Verified capability includes:

- refresh-token persistence;
- access-token refresh;
- refresh-token rotation persistence;
- single in-flight refresh protection;
- retry of the original request after successful refresh;
- session clearing when refresh recovery fails;
- redirect to login after failed recovery;
- protection against recursive refresh of login/refresh endpoints.

Non-auth shared `apiClient.ts` changes were deliberately excluded from this
commit.

### 3.5 Responsive and Browser Acceptance

`GT-CONNECT-VS-004`

Status: **CLOSED / VERIFIED / COMMITTED**

Commit:

`4b774fa6d580a807f725e2eea3365f83477e60d9`
`test(connect): establish responsive browser acceptance`

Verified browser boundary includes:

- Playwright infrastructure;
- isolated GT Connect responsive acceptance;
- mobile viewport: 390 x 844;
- single-column Connect layout at mobile widths;
- visible and usable composer;
- no page-level horizontal overflow;
- Vitest exclusion of Playwright E2E tests.

Current controlled browser acceptance result:

- Playwright: 1 / 1 PASS.

### 3.6 Remaining Connect Delta Classification

`GT-CONNECT-VS-005`

Status: **CLOSED**

Previously observed teacher-authorization files were later clean in the
working tree and were incorporated through the controlled School
teacher-assignment engineering.

Focused authorization regression:

- Tests run: 8
- Failures: 0
- Errors: 0
- Skipped: 0
- BUILD SUCCESS

No additional Connect commit was required for that delta.

---

## 4. Current Release 1 Regression

`GT-CONNECT-R1-REG-001`

Status: **CLOSED / VERIFIED at the defined regression boundary**

Verified current regression evidence:

- Connect backend targeted regression: PASS;
- Connect frontend targeted regression: PASS;
- Connect responsive Playwright acceptance: PASS, 1 / 1;
- production frontend build: PASS;
- targeted ESLint for all existing declared Connect Release 1 targets:
  PASS.

The initial combined ESLint command returned RC=2 only because two stale
paths were supplied:

- `src/features/connect/connectApi.ts`
- `src/features/connect/connectService.ts`

The current files reside under the frontend services directory.

Every existing lint target checked individually passed.

---

## 5. Historical Regression Evidence

Historical controlled GT Connect backend regression evidence remains
**VERIFIED**.

The historical 1200 / 1200 Connect regression result remains authoritative
for that historical verified baseline.

It must not be represented as a current test count unless the same suite is
re-run and reproduces that count.

---

## 6. Current Repository Evidence

Controlled branch:

`work/current-integration`

Repository audit baseline HEAD:

`9556e24296617ff722539b72bd3f1c9e7b2d1cbe`

The following key GT Connect commits were verified as ancestors of that
controlled HEAD:

- `065df650786972030ed1542e1782264f74850fd1`
- `8e9da44ac82ce33fe04c9331b39d15c48a8cd327`
- `4b774fa6d580a807f725e2eea3365f83477e60d9`

Completed Connect-owned files were clean at final repository audit:

`COMPLETED_CONNECT_DIRTY_COUNT=0`

No uncommitted Connect-named files were found.

---

## 7. Deliberately Excluded Shared Frontend Delta

A residual `apiClient.ts` working-tree change remained outside the Connect
closure:

Default API URL:

`http://localhost:8080`

to:

`http://${window.location.hostname}:8080`

This is classified as shared deployment/frontend configuration and not as
unfinished GT Connect session-recovery engineering.

It must not be swept into a Connect commit without separate ownership and
verification.

---

## 8. School Workstream Isolation

The final Connect repository audit observed 12 staged GT School
schema-integrity files.

These include migrations V261 through V267 and related tenant-integrity
integration tests.

They are outside the GT Connect Release 1 evidence boundary.

The staged manifest and staged binary-content evidence remained unchanged
during the final Connect repository audit.

GT Connect closure therefore does not grant any lifecycle status or release
authority to those School changes.

---

## 9. Security and Authorization Evidence

Release 1 evidence includes controlled authorization boundaries for:

- tenant isolation;
- Connect permissions;
- institution membership management;
- teacher authorization;
- teacher assignment authorization;
- parent relationship authorization;
- active membership validation;
- current authenticated identity;
- session recovery.

Security-critical capability remains subject to the broader Enterprise
Platform security, IAM, observability, audit and deployment controls.

---

## 10. Known Limitations and Non-Claims

### 10.1 Browser Delivery Semantics

Hidden-tab lifecycle behaviour has been verified at the defined browser
acceptance boundary.

Broader polling/background behaviour remains **BEST-EFFORT / OBSERVED**.

GT Connect Release 1 does not claim guaranteed closed-browser or
closed-application real-time delivery.

### 10.2 Mobile Acceptance Coverage

The controlled Playwright acceptance verifies the 390 x 844 browser
boundary.

This is not evidence of exhaustive testing across every device, browser,
screen size or operating system.

### 10.3 Bundle Size

The current production build reports a Vite advisory that a JavaScript
chunk exceeds 500 kB after minification.

The build succeeds.

The advisory is a non-blocking optimisation item and does not constitute a
current build failure.

### 10.4 Shared Frontend Configuration

The residual shared API-host configuration remains outside this Connect
Release 1 evidence boundary.

### 10.5 Calling Capability

`GT-CONNECT-CALL-001 — Secure Identity-Based Voice, Video & Real-Time Calling`

Status: **APPROVED / NOT YET IMPLEMENTED**

It is not part of the implemented GT Connect Release 1 capability represented
by this evidence pack.

---

## 11. Release Authority

This evidence pack supports readiness assessment only.

Current authority status:

- Release Candidate: **NOT YET AUTHORISED**
- Pilot: **NOT YET AUTHORISED**
- Production: **NOT YET AUTHORISED**

No deployment or operational authority is created by this document.

A separate controlled HQ/EPMO decision is required before any such lifecycle
transition.

---

## 12. Readiness Conclusion

At the defined engineering and regression boundary, GT Connect Release 1 has:

- closed the controlled vertical slices VS-001 through VS-005;
- passed the current combined targeted regression boundary;
- passed current responsive browser acceptance;
- passed the current production frontend build;
- established clean Git ownership for completed Connect work;
- preserved unrelated School work separately;
- recorded explicit known limitations and non-claims.

**Engineering evidence state: READY FOR RELEASE-CANDIDATE READINESS DECISION.**

**Release Candidate authority: NOT YET GRANTED.**
