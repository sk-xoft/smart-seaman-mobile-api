---
name: spring-api-feature
description: Implement or modify REST API features in this Java 11 Spring Boot 2.6 repository. Use for controllers, services, JDBC repositories, entities or DTOs, validation, exception handling, OpenAPI annotations, security routing, and focused tests under src/main and src/test.
---

# Spring API Feature

Follow the repository's existing controller-service-repository architecture and conventions. Preserve unrelated changes.

## Workflow

1. Trace the closest existing endpoint end-to-end before designing the change.
2. Confirm routes, request and response shapes, validation, authorization, database effects, and failure behavior from requirements or existing patterns.
3. Implement the smallest coherent vertical slice:
   - route constants and controller mapping;
   - request/response model validation;
   - service transaction and business rules;
   - repository SQL and parameter binding;
   - entity mapping;
   - exception translation and OpenAPI annotations.
4. Review `SecurityConfiguration`, token filters, and interceptors whenever adding a route. Keep endpoints secured unless public access is explicitly required.
5. Add focused tests at the narrowest useful layer, then run broader tests when risk justifies it.

## Project Conventions

- Stay compatible with Java 11 and Spring Boot 2.6.2; do not introduce APIs requiring newer versions.
- Prefer constructor injection and existing Lombok patterns.
- Use named parameters for SQL values. Never concatenate untrusted input into SQL.
- Reuse global response and exception conventions instead of creating a parallel envelope.
- Keep business logic out of controllers and database access out of services.
- Annotate endpoints consistently with springdoc OpenAPI.
- Keep timestamps and Asia/Bangkok behavior consistent with existing configuration.
- Do not expose secrets, provider payloads, internal paths, or stack traces in API responses.

## Verification

- Run the focused test class with `./mvnw test -Dtest=ClassName`.
- Run `./mvnw test` for cross-cutting security, configuration, or shared-service changes when feasible.
- Run `./mvnw clean package` when packaging or dependency behavior changes.
- Inspect the final diff for accidental configuration secrets and unrelated formatting churn.
- Report tests run and any environment-dependent checks that remain.
