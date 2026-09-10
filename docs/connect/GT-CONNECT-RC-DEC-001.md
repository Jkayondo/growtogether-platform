# GT HEADQUARTERS / EPMO — RC, PILOT & CONTROLLED PRODUCTION AUTHORITY DECISION

**Reference:** `GT-CONNECT-RC-DEC-001`
**Programme:** GrowTogether Enterprise Platform / GT Connect
**Release:** GT Connect Release 1
**Authority:** HQ/EPMO
**Decision:** APPROVED WITH CONTROLLED DEPLOYMENT AUTHORITY

## 1. Release Candidate Authority

**RELEASE CANDIDATE: APPROVED**

HQ/EPMO approves GT Connect Release 1 to enter Release Candidate status.

The approved Release Candidate is limited to the verified GT Connect
Release 1 vertical slice represented by the governed engineering and
readiness evidence.

## 2. Controlled Pilot Deployment Authority

**CONTROLLED PILOT DEPLOYMENT: AUTHORISED**

Engineering is authorised to deploy the verified GT Connect Release 1
vertical slice into the designated controlled pilot environment for
controlled real-world use.

The controlled pilot may exercise the real end-to-end path including:

- authenticated users;
- tenant boundaries;
- messaging and conversation lifecycle;
- Delivered and Read state;
- persistence;
- institution membership;
- authorised relationship boundaries;
- session recovery;
- responsive browser operation;
- other capabilities already contained within the approved Release 1 slice.

The pilot is intended to generate real operational evidence rather than
merely another simulated engineering test.

## 3. Controlled Production Authority

**CONTROLLED PRODUCTION DEPLOYMENT: CONDITIONALLY AUTHORISED**

Engineering may promote the same verified GT Connect Release 1 vertical
slice from controlled pilot to controlled production without requiring a
new HQ decision solely for that promotion when the established evidence
gates demonstrate all of the following:

- security remains intact;
- tenant isolation remains intact;
- critical end-to-end workflows pass;
- deployment and recovery capability are verified;
- no unresolved release-blocking Critical or High defect exists;
- observability and operational evidence are adequate;
- the deployed artefact is traceable to the approved Release Candidate.

Failure of any required condition automatically places production
promotion on HOLD until corrected and revalidated.

## 4. Scope Boundary

This authority applies only to the approved GT Connect Release 1 vertical
slice.

It does not authorise:

- unfinished GT Connect capabilities;
- unrelated GT School functionality;
- uncontrolled scope expansion;
- bypass of security or tenant-isolation controls;
- automatic promotion of future releases;
- experimental capabilities merely because they exist in the repository.

## 5. Production Operating Principle

The authorised lifecycle is:

**Engineering verified → RC → Controlled Pilot → Evidence → Controlled Production → Observe → Improve**

Controlled production deployment of this vertical slice does not mean that
the entire GrowTogether Enterprise Platform is production-complete.

It means only that the specifically verified and traceable GT Connect
Release 1 vertical slice may progress to controlled real-world operation
under the approved gates.

## 6. Authority Status

- Release Candidate: **APPROVED**
- Controlled Pilot Deployment: **AUTHORISED**
- Controlled Production Deployment: **CONDITIONALLY AUTHORISED**
- Unrestricted General Production Release: **NOT YET AUTHORISED**

## 7. Governing Readiness Evidence

This decision follows:

`GT-CONNECT-R1-READINESS-EVIDENCE-001`

Engineering is authorised to proceed through Release Candidate validation,
controlled pilot deployment and, upon satisfaction of the defined
production-readiness evidence gates, controlled production deployment
without requiring a new HQ decision solely for that promotion.

## 8. Next Engineering Gate

The next controlled gate is:

`GT-CONNECT-PILOT-DEP-001 — Controlled Pilot Deployment Preflight`

No uncontrolled deployment is authorised by this decision.
