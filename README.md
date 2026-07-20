# GrowTogether Platform

The executable engineering foundation for GrowTogether enterprise services.

## Commit 0002 capabilities

- Standard API success and failure envelopes
- Correlation ID generation and propagation
- Optional UUID tenant context propagation
- Structured JSON console logs with MDC request context
- Global exception handling without client-visible stack traces
- Jakarta Bean Validation error normalization
- Liveness, readiness, metrics, PostgreSQL, Redis, and Flyway foundation

## Prerequisites

- Java 21
- Docker with Docker Compose

## Run tests

```bash
./mvnw test
```

## Run locally

```bash
docker compose up --build
```

## Verify

```bash
curl -i http://localhost:8080/api/v1/system/status
curl -i \
  -H 'X-Correlation-ID: local-check-001' \
  -H 'X-Tenant-ID: b5fe3c82-2ff0-4da0-b6d3-373e9b76c928' \
  http://localhost:8080/api/v1/system/status
curl http://localhost:8080/actuator/health/liveness
curl http://localhost:8080/actuator/health/readiness
```

## Request headers

| Header | Requirement | Behaviour |
|---|---|---|
| `X-Correlation-ID` | Optional | Reused when valid; otherwise generated as a UUID |
| `X-Tenant-ID` | Optional at foundation stage | Must be a valid UUID when supplied |

## API response contract

```json
{
  "success": true,
  "code": "GT-SYSTEM-STATUS-OK",
  "message": "Platform service is operational.",
  "data": {},
  "errors": [],
  "metadata": {
    "correlationId": "local-check-001",
    "tenantId": null,
    "timestamp": "2026-07-17T12:00:00Z"
  }
}
```


## Persistence foundation

Tenant-scoped entities extend `AuditedTenantEntity`. The active tenant is obtained from `X-Tenant-ID`; persistence without a tenant or with a mismatched tenant is rejected. Audit timestamps and actors are populated through Spring Data JPA auditing, and optimistic locking is enabled with the `version` column.


## EIAM user API
Authenticated callers with `eiam.users.create` can create tenant-scoped users at `POST /api/v1/eiam/users`. Callers with `eiam.users.read` can retrieve them. Supply the tenant UUID through `X-Tenant-ID`; tenant ownership is enforced in both application and persistence layers.

## EIAM user lifecycle

Authenticated administrators can update, activate, suspend and deactivate tenant-scoped users through permission-protected APIs. Deactivation is terminal; suspended accounts may be reactivated; and tenant isolation is enforced on every operation.


## EIAM user directory

`GET /api/v1/eiam/users` provides tenant-isolated, permission-protected user listing. It supports `query`, `status`, `page`, and `size` parameters, caps page size at 100, and applies stable display-name ordering.

## EIAM role management

Commit 0008 adds tenant-scoped roles and audited user-role assignments. Role and assignment endpoints are under `/api/v1/eiam`; all operations require explicit `eiam.roles.*` or `eiam.user-roles.*` authorities.

### EIAM permission management (Commit 0009)

The platform supports tenant-scoped permissions and audited role-permission assignments. Effective authorities can be resolved from user roles for future login and token issuance.


## Commit 0010 — Authentication and sessions

Public authentication endpoints:

- `POST /api/v1/eiam/auth/login` (requires `X-Tenant-ID`)
- `POST /api/v1/eiam/auth/refresh`
- `POST /api/v1/eiam/auth/logout`

Access tokens are JWTs. Refresh tokens are opaque credentials; only SHA-256 hashes are persisted. Refresh rotates the credential and logout revokes the persisted session. Five failed logins lock authentication for 15 minutes by default.

## Commit 0012 — Identity recovery

Adds enumeration-resistant password reset and account recovery, single-use hashed recovery tokens, email verification, and automatic session revocation after credential recovery. In production, `RecoveryDispatch.token` must be delivered by the notification service and omitted from public API responses.

## Commit 0011 — Multi-factor authentication and trusted devices

The EIAM authentication flow now supports RFC 6238 TOTP, one-time recovery codes, MFA login challenges, session assurance levels, and revocable trusted devices. TOTP secrets are encrypted with AES-256-GCM and device, challenge, and recovery credentials are stored only as SHA-256 hashes.

### MFA APIs

- `POST /api/v1/eiam/mfa/enroll`
- `POST /api/v1/eiam/mfa/verify`
- `POST /api/v1/eiam/mfa/disable`
- `POST /api/v1/eiam/auth/mfa`
- `GET /api/v1/eiam/mfa/trusted-devices`
- `DELETE /api/v1/eiam/mfa/trusted-devices/{id}`

A password-authenticated login returns an MFA challenge when MFA is enabled. Access and refresh tokens are issued only after the challenge succeeds. Sessions record AAL1 or AAL2 assurance, and disabling MFA revokes all active sessions.

## Commit 0013 — Organization and tenant provisioning

The EIAM module can now provision an organization and tenant atomically, seed the tenant's protected permissions, create the `TENANT_ADMIN` system role, assign every seeded authority to that role, and activate a bootstrap administrator. Tenant lifecycle commands support activation, suspension, and terminal deactivation.

Provisioning endpoint: `POST /api/v1/eiam/tenants/provision` (`platform.tenants.provision`).
Tenant retrieval and lifecycle endpoints require `platform.tenants.read` and `platform.tenants.manage` respectively.


## Commit 0015 — Enterprise audit and security events

Adds immutable tenant-scoped audit events, severity/outcome classification, correlation and session linkage, actor/resource metadata, and permission-protected search.

## Commit 0016 — Enterprise Authorization Engine

Adds tenant-scoped authorization policies and a centralized `/api/v1/eiam/authorize` decision endpoint. Evaluation uses explicit-deny precedence, default deny, optional role/permission requirements, resource ownership, minimum authentication assurance, and immutable decision auditing.


## ECS-003
Enterprise Configuration Domain Model implemented with reusable definitions, scoped values and inheritance resolution. See `docs/ECS-003-ENTERPRISE-CONFIGURATION-DOMAIN-MODEL.md`.

## Enterprise Workflow Engine — Set 2

The platform now includes a tenant-safe workflow definition foundation and runtime execution engine. Published workflow versions can be started as immutable, version-pinned instances with execution context, variables, transition history, waiting/resume, completion, failure/retry, cancellation and linked restart.


## EWE Set 3 — Task and Routing Engine
Human/system/approval/review tasks, configurable routing, assignment, claim, delegation, SLA timers, escalation and immutable task events.

## EWE validation and shared-platform integration

EWE is integrated with the common EIAM tenant-security contract, ECS runtime resolution contract,
and Enterprise Audit. See `docs/EWE-VALIDATION-AND-INTEGRATION-001.md`.


## ENS validation and integration
Introduces the executable ENS foundation integrated with EIAM tenant security, ECS retry configuration, EWE notification intent, and immutable enterprise audit.


## EDS Set 2
Enterprise document lifecycle management includes immutable versions, checkout/check-in, retention, archival, restore, legal holds, duplicate detection, integrity checks, soft deletion and policy-controlled disposal.

## EDS Set 5
Administration and governance APIs are available under `/api/v1/documents/admin`. Prometheus metrics and Kubernetes/Helm deployment foundations are included. Production certification remains conditional on successful CI, security, performance, restore and failover evidence.


## EDS Validation & Integration 001
EDS is integrated with EIAM, ECS, EWE, ENS, enterprise audit, analytics metrics and AI request/outbox contracts. See `docs/EDS-VALIDATION-AND-INTEGRATION-001.md`.


## EIP Set 2

Enterprise Integration Platform runtime with durable messages, idempotency, routing, retries, dead-letter handling, replay and circuit-breaker persistence.

## EIP Set 3
API gateway route administration, encrypted webhook subscriptions, transformation rules, and external connector contracts are available under `/api/v1/integration`.

## EIP Set 5

Adds integration administration summaries, connector certification evidence, operational indexes, Kubernetes and Helm foundations, CI workflow and operations runbook. Production certification remains conditional on successful provider sandbox tests, CI, load, failover and backup/restore evidence.


## EIP Validation and Integration
EIP is integrated with EIAM, ECS, EWE, ENS, EDS, enterprise audit and analytics contracts. See `docs/EIP-VALIDATION-AND-INTEGRATION-001.md`.

## Enterprise Analytics Platform — Set 2

EAP now includes event ingestion, metric aggregation, dashboard runtime and report execution contracts. See `docs/commits/EAP-SET-002.md`.


## EAP Set 3
Advanced analytics, scheduled reports, alerting, trend analysis and enterprise data integration are implemented under `africa.growtogether.platform.eap.advanced`.

## EAIF validation and shared-platform integration

EAIF 0.37.1 integrates with the common EIAM, ECS, EIP, EDS, EAP, EWE and enterprise-audit contracts. Provider execution is disabled by default and must be enabled through ECS after provider credentials, safety evaluation and production controls are approved.
