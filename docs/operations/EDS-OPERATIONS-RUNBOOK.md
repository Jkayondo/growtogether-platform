# EDS Operations Runbook

## Health
Use `/actuator/health/readiness`, `/actuator/health/liveness`, and `/actuator/prometheus`.

## Critical alerts
- Service unavailable for 5 minutes.
- Database connection failures.
- Document integrity validation failures.
- Retention or legal-hold processing failures.
- Storage capacity above 80%.

## Incident response
Preserve correlation IDs, stop destructive governance jobs, validate database and object-store consistency, and record all operator actions through the enterprise audit service.
