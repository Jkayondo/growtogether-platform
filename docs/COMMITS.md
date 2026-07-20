# GrowTogether Platform Commit Register

## Commit 0001

`feat(repository): bootstrap GrowTogether platform`

Initial executable Spring Boot foundation with PostgreSQL, Redis, Flyway, Actuator, Docker Compose, OpenAPI, tests, and CI.

## Commit 0002

`feat(platform): standardize request context and API errors`

Adds correlation ID generation and propagation, optional UUID tenant context, structured JSON logging, standard API envelopes, global exception handling, validation normalization, OpenAPI updates, and automated tests.

## Commit 0003

`feat(persistence): add audited multi-tenant base entity and PostgreSQL integration tests`

Adds a reusable audited tenant entity, tenant-scope enforcement, optimistic locking, entity lifecycle status, Spring Data auditing, Flyway schema migration, tenant-aware repository example, and PostgreSQL Testcontainers integration tests.

## Commit 0004
`feat(security): implement enterprise authentication foundation`

## Commit 0005
`feat(eiam): implement User domain aggregate`

Adds tenant-scoped user accounts, password hashing, lifecycle rules, Flyway migration, repository, application service, secured API endpoints, and PostgreSQL integration tests.

## Commit 0006

`feat(eiam): implement user update, activation, suspension and deactivation`

- Added tenant-scoped profile updates with uniqueness enforcement.
- Added explicit activation, suspension and terminal deactivation transitions.
- Added lifecycle conflict responses and permission-protected endpoints.
- Added domain and PostgreSQL integration tests for lifecycle and tenant isolation.



## Commit 0007

`feat(eiam): implement tenant-scoped user search and pagination`

Adds user directory search across username, email and display name, lifecycle filtering, bounded pagination, stable sorting, tenant isolation and PostgreSQL integration coverage.

## Commit 0008

`feat(eiam): implement role management and user-role assignment`

- Added tenant-scoped role aggregate and audited user-role assignment entity.
- Added role CRUD and user-role replacement, lookup, and removal APIs.
- Enforced unique tenant role codes/names, system-role protection, assigned-role deletion protection, and cross-tenant isolation.
- Added Flyway V004 migration, indexes, API error handling, and lifecycle tests.

## Commit 0009

`feat(eiam): implement permission management and role-permission assignment`

Introduces tenant-scoped permission CRUD, protected system permissions, audited role-permission assignments, effective authority resolution, Flyway migration V005, and secured REST APIs.


## Commit 0010
`feat(eiam): implement authentication, login, refresh tokens and session management`

### 0011 — feat(eiam): implement multi-factor authentication (MFA) and trusted device management
