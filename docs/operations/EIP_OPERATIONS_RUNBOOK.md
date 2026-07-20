# EIP Operations Runbook

Monitor message backlog, dead letters, connector health, payment failures, settlement mismatches, circuit state and certification expiry. Escalate sustained delivery failures, open circuits, reconciliation mismatches and webhook signature failures. Replay dead-letter messages only after correcting the root cause and preserving idempotency.

## Production gates

Production activation requires successful CI build, unit/integration tests, vulnerability scanning, provider sandbox certification, webhook contract tests, broker failover tests, database backup/restore exercise, reconciliation validation and load tests.
