# EIP Set 3 — API Gateway, Webhooks, Transformation, Connector Framework and External Security

This set adds tenant-scoped API gateway route definitions, encrypted webhook signing secrets, payload transformation rules, and reusable external connector definitions. Credentials are encrypted with AES-256-GCM and never returned through administration APIs.

Authorities: `integration.gateway.manage/read`, `integration.webhook.manage/read`, `integration.transformation.manage/read`, `integration.connector.manage/read`.
