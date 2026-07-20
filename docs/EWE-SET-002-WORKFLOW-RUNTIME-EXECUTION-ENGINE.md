# EWE Set 2 — Workflow Runtime Execution Engine

## Release
`feat(ewe): implement workflow runtime execution engine`

## Scope
This release includes the missing executable Set 1 definition foundation and Set 2 runtime engine so runtime code is not built on a fictional base.

## Runtime guarantees
- A workflow instance is pinned to an immutable published definition version.
- Tenant isolation is enforced through the common persistence foundation.
- State changes occur only through domain transition methods.
- Execution events are append-only.
- Restart creates a new instance linked to the source instance.
- Retry is permitted only for failed instances.
- Sensitive variables are masked by default.

## State model
`CREATED → RUNNING ↔ WAITING → COMPLETED`

`RUNNING/WAITING → FAILED → RUNNING (retry)`

`CREATED/RUNNING/WAITING/FAILED → CANCELLED`

## Excluded from Set 2
Human tasks, assignments, role routing, parallel gateways, escalation timers and delegation belong to EWE Set 3.
