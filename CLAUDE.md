# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
./mvnw clean install

# Run application (prod profile, default — needs DB_URL/DB_USERNAME/DB_PASSWORD etc. as env vars, see README.md)
./mvnw spring-boot:run

# Run application against local/dev DB (application-local.properties), serves on :8081
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Package JAR
./mvnw clean package

# Docker build
docker build -t xoftspace/smart-seaman-mobile-api:0.2 .
```

Java 17 (`pom.xml` `<java.version>`), Spring Boot 2.6.2, Maven. Root package is `com.seaman` (i.e. `src/main/java/com/seaman/...`), mirrored under `src/test/java/com/seaman/...`.

## Architecture Overview

REST API for maritime training and certification management (mobile app backend). Stateless JWT-based auth.

**Layer structure** under `src/main/java/com/seaman/`:

| Package | Role |
|---------|------|
| `controller/` | REST endpoints — Auth, Profile, Documents, DocumentRenewal, Forms, Banners, News, Vouchers, School, Master data, Fcm, Omise webhook, App activation, Delivery address, Policy. `controller/advice/` holds `@ControllerAdvice` exception handling |
| `service/` | Business logic — generally mirrors controller domains, plus Email/FCM/Session services |
| `repository/` | JPA repositories for all entities |
| `entity/` | JPA entities (User, Certificate, Course, Document, Company, TransactionLogs, etc.) |
| `model/` | Request/Response DTOs |
| `config/` | Spring beans — `SecurityConfiguration`, `DataSourceSmartSeaman`, `CacheConfig`, `ObjectStorageConfig` (S3), `CryptographyConfig`, `SchedulingConfig`, `RestTemplateConfig`, `OpenApiConfig`, `GoogleAuthConfig`, `WebMvcConfig` |
| `filter/` | JWT token filter applied to all secured requests |
| `interceptor/` | `APIInterceptor` (validates common headers on every request), `AuthInterceptor` (validates Authorization/session on protected requests); registered via `WebMvcConfig` |
| `event/` | Spring application events + async `EventHandler` — FCM notification sending, transaction log insert/update, document-renewal payment/resubmission events |
| `component/` | Cross-cutting helper beans, e.g. `FcmSendNotificationComponent` |
| `validate/` | Custom Jackson deserializers (e.g. `StringOnlyDeserializer`) |
| `exception/` | Custom exceptions (`BusinessException`, etc.) |
| `push/` | Firebase Cloud Messaging integration |
| `utils/` | Utility helpers |
| `constant/` | Application-wide constants (`AppStatus`, `AppSys`, `Routes`, ...) |

Request flow: `APIInterceptor` → `JwtTokenFilter`/Spring Security chain → `AuthInterceptor` (for endpoints requiring a session) → controller → service → repository. Async side-effects (FCM push, transaction logging, renewal events) go through `ApplicationEventPublisher` → `event/EventHandler`.

## Key Configuration

Runtime config is split across `src/main/resources/application.properties` (prod defaults, reads secrets from env vars like `${DB_URL}`, `${DB_USERNAME}`, `${DB_PASSWORD}`, `${DO_SPACES_KEY}`, `${DO_SPACES_SECRET}`, `${ENCRYPT_KEY}`, `${JWT_SECRET}`, `${MAIL_PASSWORD}`, `${FCM_CREDENTIAL_FILE}`) and `application-local.properties` (`local` profile, points at a dev/local MySQL instance).

- **Database**: MySQL via HikariCP (3–5 connections), DigitalOcean hosted
- **Object storage**: DigitalOcean Spaces (S3-compatible), bucket `smart-seaman-bucket`
- **Auth**: JWT (secret via `JWT_SECRET`)
- **Email**: Gmail SMTP
- **FCM**: Firebase Cloud Messaging for push notifications
- **Payments**: Omise webhook integration (`OmiseWebhookController`)
- **Swagger UI**: `/smart-seaman-swagger`
- **Timezone**: Asia/Bangkok
- See `README.md` for the full local-dev setup (profiles, ports, Docker run commands per environment).

## Security Model

- `SecurityConfiguration.java` — CSRF disabled, CORS enabled, stateless sessions, public paths driven by `SecurityProperties` (`publicPaths()`; toggles actuator/docs exposure)
- `JwtTokenFilter` runs on every request; validates token and sets `SecurityContext`
- Public endpoints: login, registration, master data, health/actuator (env-dependent), app link verification (`apple-app-site-association.json`, `assetlinks.json`)
- BCrypt password encoding (strength 8)

## API Change Rule

Every time an API is created or updated:
- Update the related documentation under `documents/`, especially task/API files such as `documents/mvp1/task/task_mobile_document_renewal_api.md` when the change belongs to MVP1 document renewal.
- Document the endpoint method/path, required headers, path/query parameters, request body, response shape, validation rules, authorization/owner-scope behavior, error behavior, and cURL example using `${base_url}` and `${access_token}`.
- Update the task status/progress/evidence sections when the API task is done, including the focused test class and latest test command/result.
- Add or update focused tests for controller/service/repository behavior before marking the documentation task as `[x]`.
- If the API changes database tables, columns, indexes, constraints, seed data, or migration scripts, also update the matching schema docs and SQL task references.

## Database

MySQL with no Flyway/Liquibase — schema managed manually. SQL view definitions live in `src/main/resources/SQL/CreateView.sql`:
- Course listing view
- User notification view aggregating certificate expiry dates (18/12/6/3 months ahead)

Scheduled task in `SchedulingConfig.java` refreshes notification cache daily. `documents/non_prod_db_schema.md` and `documents/mvp1/db_script_new_feature_v1.md` track schema changes for in-progress features.

## External Services

- **DigitalOcean Spaces** — file uploads (certificates, documents, images) via AWS SDK v1 client configured in `ObjectStorageConfig.java`
- **FCM** — push notifications via HTTP API in `push/` package, dispatched asynchronously via `event/EventHandler`
- **Gmail SMTP** — transactional email
- **Omise** — payment webhook handling (`OmiseWebhookController`)
- **ZXing** — QR code generation

## Naming Conventions

| Scope | Convention | Example |
|-------|-----------|---------|
| REST URL path | kebab-case | `/school-trainings`, `/refresh-token` |
| `@RequestParam` name | camelCase | `certCode`, `documentCode`, `courseCode` |
| Java class | PascalCase | `DocumentController`, `DocumentService` |
| Java method / variable | camelCase | `validateDocumentItems`, `documentCode` |
| JSON request/response field | camelCase | `certCode`, `mobileUserUuid` |
| DB column | snake_case | `document_code`, `mobile_user_uuid` |
| Constants (`Routes`, `AppStatus`) | UPPER_SNAKE_CASE | `VALIDATE_DOCUMENT_ITEMS` |

## Logging

Logback config in `src/main/resources/logback-spring.xml`:
- Rolling daily log files, max 10MB each
- Separate severe log for WARN/ERROR
- Log path: `/apps-logs-service/smart-seaman-mobile-api/logs`
