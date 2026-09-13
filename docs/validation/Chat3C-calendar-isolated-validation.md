# Chat3C calendar isolated validation

Reference: HQ-GS-HO-TEACHER-001
Recorded: 2026-09-11

## Source capture

- Calendar fix: 8499a9f50dbf521a3792c1cd163c189baab928d4.
- Shared dependency capture: e18e1e8.
- HEAD observed when recording: b5a82cfeafe304dd548dff57dd4b0fde47312f3e.
- Ten Java dependency files and unchanged migrations V180/V188
  matched the isolated candidate byte-for-byte in the reported comparison.
- No duplicate dependency commit was created by Chat3C.

## Verification

The isolated candidate comprised calendar commit 8499a9f plus the
ten reviewed Java dependency files and existing V180/V188 migrations.

- Service, HTTP security and notification tests: 18 passed in the
  earlier isolated run. PostgreSQL startup was blocked in that run.
- After restoring V188 and V180 to the isolated checkout:
  AcademicCalendarEventPostgresIntegrationTest ran 3 tests,
  with zero failures, errors or skips.
- PostgreSQL run completed at 2026-09-11 10:46:50 EAT.
- Checks cover tenant-filtered calendar queries and committed
  optional event end-time persistence.
- Main HEAD and staging were reported unchanged during verification.

These are results from separate runs, not a single 21-test execution.
The latest integration HEAD was not independently rerun by this check.

## Evidence

Base evidence directory:
`/Users/jkayondo/Documents/GT/Backups/Chat3C/calendar-dependency-verification-20260911-004101-778641`

Successful PostgreSQL evidence:
`/Users/jkayondo/Documents/GT/Backups/Chat3C/calendar-dependency-verification-20260911-004101-778641/v180-verification-20260911-104600-152889`

Earlier failed runs are retained in the evidence directory.

## Remaining work

- Today's Programme frontend integration remains pending.
- Full-suite, existing-database upgrade and production validation
  are not established by these focused checks.
