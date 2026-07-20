# EWE Validation and Integration 001

## Validated executable scope

The available repository contains executable implementations for:

- Workflow definitions and immutable versions
- Version-pinned workflow instances
- Controlled runtime state transitions
- Variables and immutable execution events
- Human, system, approval and review tasks
- User, role, group and expression assignment
- Claim, complete, reject, delegate and escalate operations
- SLA and escalation timestamps
- Tenant-scoped repositories and method authorization

The available archive does not contain the previously described administration, import/export,
notification/document adapters, deployment automation, load-test evidence or disaster-recovery assets.
Those capabilities remain completion work and are not represented as implemented by this package.

## Integration delivered

- Common EIAM `EnterpriseIdentityContext`
- JWT/request tenant-boundary enforcement
- EWE services derive tenant identity from EIAM, not request payloads
- EWE-to-ECS gateway for tenant-scoped configuration resolution
- EWE-to-enterprise-audit adapter
- Workflow start, completion, failure and task decisions emit immutable audit events
- Session and correlation context are retained by the shared audit service

## Platform contract

EWE consumers must use workflow APIs. They must not query workflow tables, parse JWT claims,
resolve configuration independently or create product-specific workflow engines.
