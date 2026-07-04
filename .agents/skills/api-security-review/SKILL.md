---
name: api-security-review
description: Review security-sensitive changes in this Spring Boot API. Use for JWT authentication and authorization, public routes, native SQL, file upload/download, DigitalOcean Spaces access, CORS, cryptography, secrets, logs, payment data, or when explicitly asked for a security review.
---

# API Security Review

Review first; do not implement fixes unless requested. Prioritize exploitable findings and cite exact files and lines.

## Review Procedure

1. Establish the changed data flow from request entry to storage and response.
2. Verify authentication and authorization separately:
   - confirm JWT validation, expiry, signature, and error handling;
   - confirm object ownership or role checks for every read and mutation;
   - inspect public-route matchers for overbroad patterns.
3. Inspect repository SQL for concatenated input, unsafe dynamic identifiers, missing limits, and authorization gaps.
4. Inspect uploads and downloads for size limits, content/type validation, filename/path traversal, object-key predictability, and access control.
5. Inspect configuration and logs for JWT keys, passwords, Firebase credentials, provider tokens, full card data, personal data, and raw sensitive payloads.
6. Inspect CORS, CSRF assumptions, error responses, rate-sensitive endpoints, and replay/idempotency behavior.
7. Check dependency or platform risk only when supported by the lock/build metadata or a requested scan; do not guess vulnerability status.

## Project-Specific Boundaries

- Treat `SecurityConfiguration`, `TokenFilter`, `JwtTokenService`, `CryptographyConfig`, and `AuthInterceptor` as one authentication surface.
- Treat S3-compatible object storage, FCM, Gmail SMTP, and payment integrations as trust boundaries.
- Preserve full audit trails for payment and document-state changes.
- Never print or reproduce plaintext secrets found during review.

## Output

- List findings by severity, highest first.
- For each finding, state the attack path, impact, evidence, and minimal remediation.
- Distinguish confirmed findings from defense-in-depth suggestions.
- Say explicitly when no actionable findings are present and note material areas not tested.
