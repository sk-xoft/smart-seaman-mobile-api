# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Build
./mvnw clean install

# Run application
./mvnw spring-boot:run

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest=ClassName

# Package JAR
./mvnw clean package

# Docker build
docker build -t xoftspace/smart-seaman-mobile-api:0.2 .

# Docker run
docker run --name smart-seaman-mobile-api -d \
  -e COMPANY='smart-seaman' \
  -e ENV='dev' \
  -it -p 30000:8080/tcp \
  xoftspace/smart-seaman-mobile-api:0.2
```

## Architecture Overview

Spring Boot 2.6.2 REST API (Java 25) for maritime training and certification management. Stateless JWT-based auth.

**Layer structure** under `src/main/java/com/`:

| Package | Role |
|---------|------|
| `controller/` | REST endpoints — Auth, Profile, Documents, Forms, Banners, News, Vouchers, Courses |
| `service/` | Business logic — mirrors controller domains, plus Email and FCM notifications |
| `repository/` | JPA repositories for all entities |
| `entity/` | JPA entities (User, Certificate, Course, Document, Company, etc.) |
| `model/` | Request/Response DTOs |
| `config/` | Spring beans — Security, DataSource, Cache, S3, Crypto, Scheduler, REST |
| `filter/` | JWT token filter applied to all secured requests |
| `exception/` | Global exception handler + custom exceptions |
| `push/` | Firebase Cloud Messaging integration |
| `utils/` | Utility helpers |
| `constant/` | Application-wide constants |

## Key Configuration

`src/main/resources/application.properties` controls all runtime config:
- **Database**: MySQL via HikariCP (3–5 connections), DigitalOcean hosted
- **Object storage**: DigitalOcean Spaces (S3-compatible), bucket `smart-seaman-bucket`
- **Auth**: JWT with key in `application.properties`
- **Email**: Gmail SMTP
- **FCM**: Firebase Cloud Messaging for push notifications
- **Swagger UI**: `/smart-seaman-swagger`
- **Timezone**: Asia/Bangkok

## Security Model

- `SecurityConfiguration.java` — CSRF disabled, CORS enabled, stateless sessions
- `JwtTokenFilter` runs on every request; validates token and sets `SecurityContext`
- Public endpoints: login, registration, master data, app link verification (`apple-app-site-association.json`, `assetlinks.json`)
- BCrypt password encoding

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

Scheduled task in `SchedulingConfig.java` refreshes notification cache daily.

## External Services

- **DigitalOcean Spaces** — file uploads (certificates, documents, images) via AWS SDK v1 client configured in `ObjectStorageConfig.java`
- **FCM** — push notifications via HTTP API in `push/` package
- **Gmail SMTP** — transactional email
- **ZXing** — QR code generation

## Logging

Logback config in `src/main/resources/logback-spring.xml`:
- Rolling daily log files, max 10MB each
- Separate severe log for WARN/ERROR
- Log path: `/apps-logs-service/smart-seaman-mobile-api/logs`
