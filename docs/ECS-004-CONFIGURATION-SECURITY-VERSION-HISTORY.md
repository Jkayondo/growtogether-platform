# ECS-004 — Configuration Security, Secret Encryption and Version History

## Released capability

ECS now encrypts secret configuration values with AES-256-GCM, stores key identifiers and random IVs, hashes values for change comparison, and creates immutable history records after every write and rollback.

## Security rules

- Plaintext secrets are never stored in configuration value rows or history.
- Secret API responses are masked unless the caller has `platform.configuration.secret.read`.
- History exposes hashes and metadata, never secret plaintext.
- Rollback creates a new current version; historical rows are never edited.
- Production deployments must replace the development encryption key through `GT_ECS_ENCRYPTION_KEY`.

## Authorities

- `platform.configuration.manage`
- `platform.configuration.read`
- `platform.configuration.secret.read`
- `platform.configuration.history.read`
- `platform.configuration.rollback`
