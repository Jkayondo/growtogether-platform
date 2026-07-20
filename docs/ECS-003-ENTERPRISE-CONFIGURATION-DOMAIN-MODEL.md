# ECS-003 — Enterprise Configuration Domain Model

Status: Implemented

## Aggregate model

- `ConfigurationDefinition` is the global registry entry and defines code, category, type, default, validation metadata, allowed scopes, secrecy and lifecycle.
- `ConfigurationValue` stores one active value at Platform, Country, Organization or Tenant scope.
- `ConfigurationService` validates and resolves effective values in the order Tenant → Organization → Country → Platform → Definition default.

## Build Once. Reuse Everywhere

The model contains no school, SACCO, hospital, restaurant or supermarket logic. Product-specific settings are registry data, not new code.

## Security

Management requires `platform.configuration.manage`; runtime reads require `platform.configuration.read`. Secret values are masked by the API. Production secret encryption and external key management remain in ECS-004.
