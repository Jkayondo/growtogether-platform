# Teacher Workspace validation — Chat3C

Reference: HQ-GS-HO-TEACHER-001
Verification date: 2026-09-10
Status: Scoped source changes committed; focused backend tests, local runtime and exported frontend source snapshot verified.

## Verified behavior

- Signed-in teacher assignment loading through the current-teacher endpoint.
- Authentication and assignment-read route checks.
- Empty assignments skip supporting lookups.
- Supporting lookup failures retain assignments with fallback reference IDs.
- Class names resolve across education-level lookups in automated tests.
- Local browser displays English assignments for Baby Class and Top Class.
- Role-permission replacement handles overlapping permissions and rollback
  in PostgreSQL regression tests.

## Test evidence

- Backend focused suite: 31 passed, zero failures, errors or skipped tests.
- Frontend focused suite: 11 passed across two test files.
- Frontend production build: passed.
- Vite reported a non-blocking bundle-size warning.

Reported start and end revision:
`ed59417115369ef1a143c61a31dd7ec3d53caa2a`

These results describe the working tree used for verification.
The revision alone does not identify its uncommitted contents.

Evidence directory:
`/Users/jkayondo/Documents/GT/Backups/Chat3C/final-verification-20260910-100608-333369`

SHA256 hashes captured when writing this record:
- `backend.log`: `5b4c7be919d465b6a898a7f6b1e7d465ad7d91d2349837737f9ef1ca3f89b140`
- `frontend-tests.log`: `cb59462fce53f7791ca4747cfbf13d4603549428faf9ee4ab8bf888ce6d970ea`
- `frontend-build.log`: `936432f95dca56809e98248eca980f20529dbe25dd5ceccb59bd1afb35a9898e`

## Local runtime evidence

Validation account: `gt.teacher.validation`

Verified assignment references:
- `CHAT3C-VALIDATION-ENG-BABY-001`
- `CHAT3C-VALIDATION-ENG-TOP-001`

Browser evidence: Screenshot 2026-09-10 at 8.28.11 AM.png,
provided in the working conversation.

Teacher role, supporting permissions and validation fixtures were provisioned
through local APIs. Their provisioning is not represented by this document
or by an application source commit.

## Remaining scope

- Scoped source capture completed; commit references are recorded below.
- Fresh dependency installation and isolated backend verification remain pending.
- Decide retention or cleanup of local validation fixtures.
- Today's Programme, Curriculum Progress and Teaching Tools remain placeholders.
- The separate teacher workspace summary endpoint is outside this validation.
- This record does not certify platform-wide authorization or production readiness.

## Scoped repository capture and staged-source verification

- Role-permission replacement fix: `b1c0d171e1c0e533a07fd731f26a28f6edf081c3`
- Current-teacher assignment endpoint: `0a64fd94ef03aa442a5188750a50a47c65b65b02`
- Teacher Workspace frontend: `5f721a9f32d31e5b8761af63c877d2574559fc5c`

Frontend committed tree: `decae501aed2db3caf3d2d44818557d7039ab6f7`

On 2026-09-10 at approximately 16:35 EAT, an export of this staged tree
passed all 11 focused frontend tests and the production build.
The resulting frontend commit was checked against that exact tree.

The exported source snapshot reused installed node_modules from the original
workspace. This verifies the captured source with those dependencies; it is
not a fresh dependency-install verification.

The bundle-size warning remains. Earlier browser runtime evidence applies
to the working tree tested at that time, not a separately deployed build
of this commit. Backend tests were verified in the working tree.

Evidence directory: `/Users/jkayondo/Documents/GT/Backups/Chat3C/staged-frontend-verification-20260910-163546-867327`

Evidence SHA256:
- `frontend-tests.log`: `4db7ecdd620d498f5a5b6a4ecb4f6443483a3e6a0e323724103f13b78fda3a41`
- `frontend-build.log`: `62b62a4fee9ba8b6c92c2aa1432c39416555397fab3d1ef1e7f2c083c285c8b4`
- `manifest.txt`: `562fb8d67e4d9167899c915bdc600e4a5bdc0b66c58a60a97310f03cda14f282`
- `results.txt`: `33f12cde39ed5f1d188623ec68c3fa9515e220001afa6f5fbccc9efef96f5610`

Other workstreams' route, permission and API-hostname edits were excluded
from the scoped frontend commit. The shared working tree may remain dirty.
No production deployment or push is recorded by this validation.
