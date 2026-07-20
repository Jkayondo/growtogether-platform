# EAP Set 2 — Analytics Processing Runtime

Implements tenant-scoped event ingestion, metric definitions, time-bucket aggregation, dashboard runtime queries, report execution records, retry/dead-letter event processing, and export-format contracts.

## Authorities
- analytics.event.ingest
- analytics.metric.manage
- analytics.processing.manage
- analytics.dashboard.manage
- analytics.report.execute
- analytics.read

## Important boundary
EAP owns analytics processing and runtime queries. Products publish events and consume dashboards/reports; they do not build independent analytics engines.
