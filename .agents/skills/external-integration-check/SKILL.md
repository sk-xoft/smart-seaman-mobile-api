---
name: external-integration-check
description: Implement, troubleshoot, or review external service integrations in this API, including DigitalOcean Spaces via AWS SDK, Firebase Cloud Messaging, Gmail SMTP, and payment providers such as Omise. Use for clients, credentials, timeouts, retries, idempotency, webhooks, provider errors, and integration tests.
---

# External Integration Check

Treat every provider call as unreliable and every callback as untrusted. Follow existing configuration and service patterns.

## Workflow

1. Identify the provider operation, local business outcome, credentials, request identifiers, and sensitive fields.
2. Inspect the existing client configuration and closest service/component before changing behavior.
3. Define success, retryable failure, permanent failure, timeout, duplicate request, and delayed callback behavior.
4. Implement explicit timeouts and bounded retries only for safe or idempotent operations. Never blindly retry a charge, email, upload, or notification that may already have succeeded.
5. Persist provider IDs and internal idempotency keys where reconciliation is required.
6. For webhooks, authenticate when supported, validate schema, deduplicate events, verify final state with the provider when required, and update local state transactionally.
7. Sanitize provider errors and logs; retain useful correlation IDs without credentials or sensitive payloads.

## Provider Checks

- **DigitalOcean Spaces:** validate content and object keys, close streams, set safe metadata, and avoid public ACLs unless explicitly required.
- **FCM:** handle invalid tokens separately from transient delivery failures; avoid logging full tokens.
- **Gmail SMTP:** separate connection/authentication errors from message validation; prevent header injection and duplicate sends.
- **Payments:** use `BigDecimal`, convert units explicitly, never reuse single-use tokens/sources, reconcile callbacks, and keep refunds linked to the original charge.

## Verification

- Prefer mocked client tests for success, timeout, provider rejection, malformed response, and duplicate callback paths.
- Do not call production or send real email, notification, upload, charge, or refund without explicit authorization.
- Run focused Maven tests and inspect logs to confirm secrets and sensitive payloads are redacted.
- Report verified behavior, mocked assumptions, and any check requiring sandbox credentials or provider access.
