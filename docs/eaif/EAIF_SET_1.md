# EAIF Set 1 — Core Foundation

Provides a reusable, tenant-scoped AI platform foundation with provider abstraction, model registry, prompt versioning, request governance, safety validation, auditable request states, and integration-ready APIs. It intentionally stores input/output references and hashes rather than raw sensitive payloads.

## Enterprise boundaries

- EIAM owns identity and tenant security.
- ECS owns model/provider policy and feature configuration.
- EIP owns external provider connectivity, retries, and webhook delivery.
- EDS owns source and generated document artifacts.
- EAP owns metric and outcome analytics.
- EAIF owns AI orchestration contracts, prompt governance, model selection, and safety policy.
