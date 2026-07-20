# GrowTogether Enterprise Platform — Canonical Baseline 001

## Purpose

This package consolidates the strongest executable repository lineage into one official baseline and repairs known omissions caused by earlier ZIP branches.

## Corrections completed

- Adopted the cumulative EAIF validation/integration repository as the canonical base.
- Compared all earlier executable ZIP packages against the canonical base.
- Restored two missing integration tests:
  - `EnterpriseIdentityContextTest`
  - `ConfigurationIdentityBoundaryTest`
- Restored the previously described but missing EIAM capabilities:
  - Organization invitations
  - Secure invitation tokens
  - Invitation expiry, resend and revocation
  - Invitation role assignments
  - New-user onboarding
  - Tenant membership lifecycle
  - Membership audit events
- Added Flyway migration `V031__restore_invitations_memberships.sql`.
- Advanced the repository version to `0.38.0-SNAPSHOT`.

## Structural validation completed

- Continuous Flyway sequence `V001` through `V031`
- No duplicate migration versions
- No duplicate Java fully-qualified class names
- No unresolved internal GrowTogether imports detected
- No duplicate HTTP method/path mappings detected by static scan
- Maven `pom.xml` parses successfully
- Java brace-integrity checks pass
- ZIP archive integrity passes

## Validation limitation

The Maven compile/test suite could not run in this environment because the Maven wrapper could not resolve `repo.maven.apache.org`. This is an external network/DNS limitation. Compilation and automated tests must be executed in GitHub Actions or another connected CI environment before production certification.

## Canonical rule

All future implementation must start from this baseline or from its Git successor. Independent ZIP branches must not be used as new implementation bases.

## Step 2 CI correction

The duplicate EIP-specific workflow was removed. The repository now has one canonical CI workflow with PostgreSQL, Redis, Java 21, compile, test, artifact, and Docker stages.
