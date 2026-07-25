# Smart Seaman Mobile API - Security And Performance Review

## Executive Summary

Project นี้เป็น Spring Boot 2.6.2 REST API แบบ stateless JWT สำหรับ mobile API มี layer แยกชัดเจนเป็น controller, service, repository, entity, model, config, filter และ interceptor โดย data access ส่วนใหญ่ใช้ `NamedParameterJdbcTemplate` กับ MySQL และ external service หลักคือ DigitalOcean Spaces, Gmail SMTP และ Firebase Cloud Messaging

จุดที่ควรปรับก่อน production hardening คือ security configuration, token validation flow, error handling, transaction logging, database indexing/query pattern และ file upload/download handling เพราะหลายจุดมีผลทั้ง security และ performance พร้อมกัน

## Review Update - Current Action Items

อัปเดต ณ วันที่ 10 กรกฎาคม 2026 หลัง implement mobile document renewal APIs, Omise payment/webhook, mobile-number change history และ contract tests

### Recently Completed / Reduced Risk

| Status | Area | Update |
| --- | --- | --- |
| Done | Mobile renewal contract | `../task/task_mobile_document_renewal_api.md` ปิด MR-MOB-02 ถึง MR-MOB-12 แล้ว และมี controller/service/repository/model tests ครอบคลุม 10 mobile renewal endpoints |
| Done | Payment source of truth | Omise PromptPay/Mobile Banking ใช้ webhook `charge.complete` + retrieve charge จาก provider ก่อนเปลี่ยน request status; client callback ไม่สามารถ mark success เอง |
| Done | Payment duplicate guard | Payment attempt ใช้ `idempotencyKey`; webhook success ซ้ำไม่ append timeline หรือเปลี่ยนสถานะซ้ำ |
| Done | Mobile number history | `POST /v1/profile-update` update `MOBILE_NUMBER` ภายใต้ transaction + row lock และ insert `m_mobile_number_history` เมื่อเบอร์เปลี่ยน |
| Done | Renewal schema hardening | RUN scripts เพิ่ม FK/check constraints/indexes สำหรับ renewal/payment/delivery/profile request items หลายจุด |
| Done | Test baseline | `./mvnw test` ล่าสุดผ่าน `Tests run: 99, Failures: 0, Errors: 0, Skipped: 0` |

### Current Must-Do Backlog

| Priority | Status | Area | สิ่งที่ต้องทำ |
| --- | --- | --- | --- |
| P0 | To Do | Security config | จำกัด CORS ตาม environment และปิด/จำกัด Swagger + Actuator ใน prod |
| P0 | To Do | Auth errors | เปลี่ยน token/auth failures จาก 500 เป็น 401/403 และลด token validation ซ้ำระหว่าง filter/interceptor |
| P0 | To Do | Secret exposure | ตรวจและลด startup logging ใน `SmartSeamanMobileApiApplication` ไม่ให้ log secret/config sensitive เช่น JWT key, DB URL ที่มี credential, S3 config หรือ plaintext env value |
| P0 | To Do | Error/PII leakage | หยุดส่ง `ex.getMessage()` จาก database/S3/JWT กลับ client และ redact PII ใน transaction logs |
| P0 | To Do | Omise webhook production hardening | บังคับตั้ง `OMISE_WEBHOOK_SECRET` ใน prod, verify signature ให้ fail-closed และตรวจ dashboard webhook URL ก่อนเปิดจริง |
| P1 | To Do | File security/performance | legacy Base64 certificate/document flows ต้องมี size/content validation ชัดเจน หรือย้ายเป็น multipart/streaming/presigned URL |
| P1 | To Do | DB/index performance | validate query plan และ index สำหรับ certificate list, document listing, auth/session, renewal required-item validation และ payment lookup |
| P1 | To Do | Load testing | load test endpoint หลัก: login, profile/document list, missing document validation, renewal create, upload, payment create/status |
| P1 | To Do | Transaction logging | redact request/response JSON และพิจารณา async logging/sampling สำหรับ read endpoint ที่ไม่ critical |
| P2 | To Do | Test infrastructure | เพิ่ม Testcontainers/MySQL 8 disposable schema เพื่อ smoke test RUN scripts, constraints และ migration order |
| P2 | To Do | Platform | วางแผน upgrade Spring Boot/JJWT/dependencies และเพิ่ม Flyway/Liquibase เพื่อลด schema drift |

## Project Structure Assessment

| Area | Current State | Assessment |
| --- | --- | --- |
| API Layer | `controller/` มี endpoint แยก domain เช่น auth, profile, document, banner, news | โครงสร้างอ่านง่าย แต่ validation ยังไม่สม่ำเสมอในทุก endpoint |
| Business Logic | `service/` ทำทั้ง business flow, transaction logging, S3 I/O | Service บางตัวรับผิดชอบหลายเรื่อง ควรแยก file/storage concerns เมื่อ flow โตขึ้น |
| Data Access | `repository/` ใช้ raw SQL + `NamedParameterJdbcTemplate` | ควบคุม SQL ได้ดี แต่ต้องดู index และ query plan เอง |
| Security | Spring Security + custom token filter + MVC auth interceptor | มี validation ซ้ำและ status code บางเคสไม่ถูกต้อง |
| Config | env var สำหรับ DB, JWT, S3, mail, FCM และ Omise | ดีที่ secrets มาจาก env แต่ต้อง harden startup logging และ production defaults |
| Observability | logback + transaction log table | มี trace log แต่ error logging อาจ expose รายละเอียดและเขียน DB มากเกิน |

## Security Findings

### High Priority

1. **CORS อนุญาตกว้างเกินไป**
   - Current: `cors.setAllowedOriginPatterns(Collections.singletonList("http://*"))` และ `allowCredentials(true)`
   - Risk: browser client ใด ๆ บน HTTP origin สามารถส่ง credential ไป API ได้ หาก deploy ใช้ cookie/header credential อาจเพิ่ม attack surface
   - Recommendation: จำกัด origin ตาม environment เช่น `https://mobile.smartseaman.com`, admin domain และ localhost เฉพาะ dev

2. **Swagger และ actuator เปิด public**
   - Current public paths: `/actuator/**`, swagger UI, api docs
   - Risk: expose endpoint metadata, health/internal details
   - Recommendation: ปิดใน prod หรือจำกัดด้วย auth/IP allowlist; อย่างน้อยเปิดเฉพาะ health minimal endpoint

3. **JWT validation ซ้ำและ error code ไม่เหมาะสม**
   - `TokenFilter` verify token แล้ว `AuthInterceptor` parse/validate token ซ้ำอีกครั้ง
   - `TokenFilter` ตอบ token error เป็น `500 INTERNAL_SERVER_ERROR`
   - Risk: performance เสียทุก request, client แยก auth error ไม่ได้, monitoring เห็น 500 ปลอม
   - Recommendation: ให้ Spring Security filter เป็นจุดเดียวสำหรับ authentication และตอบ `401/403`; interceptor ใช้เฉพาะ session/business checks หรือย้าย session check เข้า filter เดียวกัน

4. **Error response และ log อาจ expose internal details**
   - หลาย catch ส่ง `ex.getMessage()` กลับไปใน response/data และ log transaction
   - Risk: leak SQL error, S3 key, JWT parsing detail, internal class behavior
   - Recommendation: client response ใช้ generic message; internal error detail อยู่ใน structured logs เท่านั้น และ mask token/file path/PII

5. **Startup logging อาจ expose sensitive configuration**
   - `SmartSeamanMobileApiApplication` มี startup log ที่แสดงค่าหลาย config เช่น DB/JWT/object-store related values
   - Risk: secret/config หลุดใน log aggregation หรือ support bundle แม้บาง field mask แล้ว
   - Recommendation: log เฉพาะ environment/profile และ masked health metadata; ห้าม log secret key, JWT key, DB password, access key หรือ full connection string

6. **File upload legacy บาง flow ยังเป็น Base64 string และไม่มี explicit size/content validation ชัดเจน**
   - Renewal correction endpoint ใหม่ใช้ multipart แล้ว แต่ certificate/document legacy flow ยังมี Base64 risk
   - Risk: memory pressure, oversized payload, invalid content, stored malware
   - Recommendation: enforce max request size, validate MIME/signature, allowlist content types, stream upload แทนเก็บ payload ใหญ่ใน heap

7. **Omise webhook ต้อง harden สำหรับ production**
   - มี config `omise.webhook-secret` แล้ว แต่ default เป็นค่าว่างเพื่อให้ dev/test รันได้
   - Risk: prod misconfiguration ทำให้ webhook endpoint รับ payload ที่ไม่ได้ verify
   - Recommendation: บังคับ fail-fast/fail-closed ใน prod เมื่อไม่ตั้ง `OMISE_WEBHOOK_SECRET`; verify dashboard URL และ alert เมื่อ webhook verification fail

### Medium Priority

1. **Spring Boot 2.6.2 และ jjwt 0.9.1 เก่า**
   - Risk: dependency CVEs และ unsupported lifecycle
   - Recommendation: วางแผน upgrade เป็น Spring Boot 2.7.x อย่างน้อย หรือ 3.x หากพร้อม migrate Java/Jakarta; เปลี่ยน jjwt เป็น maintained version

2. **CSRF disabled เหมาะกับ bearer API แต่ต้องยืนยันว่าไม่มี cookie auth**
   - Recommendation: คง disabled ได้สำหรับ pure bearer token API; ถ้ามี web admin cookie/session ต้องแยก security chain

3. **HSTS header hardcoded แต่ API อาจถูกเรียกผ่าน HTTP**
   - Recommendation: enforce HTTPS ที่ reverse proxy/load balancer และตั้ง `server.forward-headers-strategy` ให้ถูกต้องเมื่ออยู่หลัง proxy

4. **Transaction log เก็บ request/response JSON**
   - Risk: PII, document metadata, email, address อาจถูกเก็บยาวใน DB
   - Recommendation: redact PII และจำกัด response logging เฉพาะ code/status/trace id

## Performance Findings

### High Priority

1. **Database connection pool อาจเล็กเกินไปสำหรับ production**
   - Current Hikari max pool size = 5
   - Impact: API ที่มี auth + transaction log + business query อาจใช้หลาย DB calls ต่อ request ทำให้ queue ง่าย
   - Recommendation: load test แล้วปรับ pool เช่น 10-20 ตาม DB capacity; วัด p95 connection wait time

2. **Auth flow มีหลาย DB calls ต่อ request**
   - `AuthInterceptor` validate token, load session, update session status, load user
   - Impact: ทุก secured endpoint มี DB read/write overhead ก่อนถึง business logic
   - Recommendation: ลด session update frequency เช่น update last-active แบบ throttled, cache session/user ช่วงสั้น, หรือรวม query session+user

3. **Pagination ใช้ offset และ count แยก query**
   - `findByPage` + `countByPageByUserUid` รัน 2 queries ต่อ list request
   - Offset pagination ช้าเมื่อ offset สูง
   - Recommendation: ใช้ keyset pagination สำหรับ mobile list, หรือ cache/count เฉพาะที่จำเป็น

4. **Base64 file I/O ผ่าน memory**
   - S3 upload/download ใช้ string payload
   - Impact: heap memory โตตามขนาดไฟล์ และ GC pressure สูง
   - Recommendation: multipart upload/streaming, presigned URL, หรือ binary upload endpoint

5. **Logging และ transaction logging ทุก request เพิ่ม latency**
   - Service หลาย method insert/update transaction log ใน synchronous path
   - Recommendation: ทำ async log/event queue หรือ sampling สำหรับ non-critical read endpoints

### Medium Priority

1. **Query บางจุดใช้ `select *`**
   - เช่น master/document/certificate repositories
   - Recommendation: select เฉพาะ columns ที่ response ใช้ ลด network/mapper overhead

2. **`BeanPropertyRowMapper` สะดวกแต่มี reflection overhead**
   - Recommendation: สำหรับ hot paths ให้ใช้ explicit `RowMapper`

3. **Index coverage ต้อง align กับ query จริง**
   - Query validate document items ควรมี composite index บน `m_document_setting_requires(document_code, is_required, is_active, sort_order, document_master_request_item_code)`
   - `m_document_profile_request_item` ปัจจุบันมี unique `(mobile_user_uuid, document_master_request_item_code, document_type, slot_code)` เหมาะกับ slot-based lookup แต่ยังต้อง validate query plan สำหรับ special case `MRI001`
   - Payment lookup ควร validate index สำหรับ `(request_id, created_at)`, unique `idempotency_key`, และ `(provider, provider_charge_id)` เพื่อรองรับ retry/webhook

4. **RestTemplate timeout 60s สูง**
   - Impact: thread ค้างนานเมื่อ external service ช้า
   - Recommendation: แยก timeout ตาม service; FCM/email/external API ควรมี retry/backoff และ circuit breaker

5. **Caffeine ใช้ `softValues()`**
   - Impact: cache eviction ขึ้นกับ GC ทำให้ hit rate คาดเดายาก
   - Recommendation: ใช้ `maximumSize` + expire policy ที่ชัดเจน

## Database And SQL Recommendations

### Existing Good Practices

- ใช้ named parameters ลด SQL injection risk
- มี FK/check constraints ใน MVP script หลายจุด
- มี unique key สำหรับ mapping ที่ไม่ควรซ้ำ เช่น document settings และ profile request item
- Renewal/payment/delivery flow ใหม่ใช้ constraints และ indexes ใน RUN scripts ช่วยกัน duplicate state และ owner lookup หลายจุด

### Verified Index Recommendations

| Area | Current verified state | Recommendation |
| --- | --- | --- |
| `m_certificates` lookup/list | non-prod schema มีแค่ primary key `CERT_ID`; query ใช้ `CERT_MOBILE_UUID`, `CERT_DOCUMENT_CODE`, `CERT_END_DATE` | ควรเพิ่ม 2 indexes ด้านล่าง |
| `m_documents` list | มี primary key `DOCUMENT_ID` และ unique `DOCUMENT_CODE`; query list filter ด้วย `DOCUMENT_STATUS`, `DOCUMENT_TYPE`, `DOCUMENT_DEFAULT_FLAG` แล้ว order by `DOCUMENT_SEQ` | ควรเพิ่ม index สำหรับ status/type list; default list ให้ validate ด้วย `EXPLAIN` ก่อนเพิ่ม |
| `t_session` auth/session | `03_create_core_indexes.sql` มี `ux_t_session_client_session_id (CLIENT_SESSION_ID)` และ `ix_t_session_user_id (USER_ID)` แล้ว | ไม่ต้องเพิ่ม index ใหม่ตอนนี้; `TOKEN != :TOKEN` เป็น inequality บน `TEXT` ไม่เหมาะทำ normal index |
| `t_transaction_logs` update | schema มีแค่ primary key `IN_LOG_ID`; repository update ด้วย `TRANS_ID` | ควรเพิ่ม index บน `TRANS_ID` |
| `m_document_setting_requires` validation | `03_create_core_indexes.sql` มี `idx_doc_setting_requires_validate (document_code, is_required, is_active, sort_order, document_master_request_item_code)` แล้ว | ไม่ต้องเพิ่มใหม่; ให้ verify ด้วย `EXPLAIN` บนข้อมูลจริง |
| `m_payment_transaction` retry/webhook | `01_create_mvp1_tables.sql` มี `uq_payment_idempotency_key`, `uq_payment_provider_charge`, `idx_payment_request_created` แล้ว | ไม่ต้องเพิ่มใหม่; ให้ verify ด้วย `EXPLAIN` บนข้อมูลจริง |

1. เพิ่ม index สำหรับ certificate list:

```sql
CREATE INDEX idx_cert_mobile_doc ON m_certificates (CERT_MOBILE_UUID, CERT_DOCUMENT_CODE);
CREATE INDEX idx_cert_mobile_end_date ON m_certificates (CERT_MOBILE_UUID, CERT_END_DATE);
```

เหตุผล:

- `CertificateRepository.findBy`, `findByUsersAndCertCodeList` และ `documentDelete` filter ด้วย `CERT_MOBILE_UUID` + `CERT_DOCUMENT_CODE`
- `DocumentRepository.findByPage` join `m_certificates` ด้วย `CERT_DOCUMENT_CODE` และ filter `CERT_MOBILE_UUID`
- `DocumentRepository.findCloseToExpiration` filter `CERT_MOBILE_UUID`, `CERT_END_DATE` และ order by `CERT_END_DATE`

2. เพิ่ม index สำหรับ document listing:

```sql
CREATE INDEX idx_documents_status_type_seq ON m_documents (DOCUMENT_STATUS, DOCUMENT_TYPE, DOCUMENT_SEQ);
```

Optional หลังดู `EXPLAIN`:

```sql
CREATE INDEX idx_documents_type_seq ON m_documents (DOCUMENT_TYPE, DOCUMENT_SEQ);
CREATE INDEX idx_documents_default_seq ON m_documents (DOCUMENT_DEFAULT_FLAG, DOCUMENT_SEQ);
```

เหตุผล:

- `idx_documents_status_type_seq` เหมาะกับ `DocumentRepository.findByPage/countByPageByUserUid`
- `idx_documents_type_seq` เหมาะกับ `findByType` ที่ไม่มี `DOCUMENT_STATUS`
- `idx_documents_default_seq` เหมาะกับ `findDefault`
- เนื่องจาก `m_documents` มีจำนวนแถวน้อยใน non-prod schema ปัจจุบัน ควรเพิ่ม optional indexes หลังเห็น `EXPLAIN`/load test เท่านั้น

3. สำหรับ transaction/session:

```sql
-- t_session มี 03_create_core_indexes.sql แล้ว:
-- ALTER TABLE t_session ADD UNIQUE INDEX ux_t_session_client_session_id (CLIENT_SESSION_ID);
-- ALTER TABLE t_session ADD INDEX ix_t_session_user_id (USER_ID);
-- จึงยังไม่แนะนำ index ใหม่สำหรับ t_session

-- Transaction log repository update ด้วย TRANS_ID ไม่ใช่ TRACE_ID
CREATE INDEX idx_transaction_logs_trans_id ON t_transaction_logs (TRANS_ID);
```

หมายเหตุ: schema จริงใช้ `t_session` และ `t_transaction_logs`; ไม่มี `m_sessions`, `transaction_logs` หรือ column `TRACE_ID` ใน transaction log table ตาม repository ปัจจุบัน

4. Validate renewal/payment indexes ที่เพิ่มแล้วใน RUN scripts ด้วย `EXPLAIN` บนข้อมูลจริง:

```sql
-- required item validation / create request item
EXPLAIN SELECT * FROM m_document_setting_requires
WHERE document_code = 'DOC001' AND is_required = 1 AND is_active = 'YES';

-- payment retry/status/webhook lookup
EXPLAIN SELECT * FROM m_payment_transaction WHERE idempotency_key = '...';
EXPLAIN SELECT * FROM m_payment_transaction
WHERE provider = 'OMISE' AND provider_charge_id = 'chrg_test_...';
```

## Operational Recommendations

### Immediate Actions

Immediate actions ชุดนี้ควรทำก่อน production hardening หรือก่อนเปิด traffic จริงที่มี payment/document upload ปริมาณสูง เป้าหมายคือปิด security exposure ที่เห็นชัด, ลด PII/secret leakage และเตรียม performance baseline ให้ตรวจสอบได้

| Priority | Action | Why | Implementation target | Acceptance criteria | Verification |
| --- | --- | --- | --- | --- | --- |
| P0 | Restrict CORS และปิด/จำกัด Swagger + Actuator ใน prod | CORS ปัจจุบันอนุญาต `http://*` พร้อม credentials; swagger/actuator อยู่ใน public paths ทำให้ expose API metadata และ internal health details | `SecurityConfiguration`, `application.properties` หรือ env-driven config | prod อนุญาตเฉพาะ origin ที่กำหนด เช่น mobile/admin domains; dev ยังอนุญาต localhost ได้; swagger/api-docs/actuator ไม่ public ใน prod ยกเว้น health minimal endpoint ถ้าจำเป็น | เพิ่ม/รัน security tests สำหรับ allowed/disallowed origin และ public/protected paths; manual curl ตรวจ `/smart-seaman-swagger`, `/v3/api-docs`, `/actuator/**` ใน prod profile |
| P0 | เปลี่ยน token/auth errors จาก 500 เป็น 401/403 และลด token validation ซ้ำ | token invalid/expired ตอนนี้มี risk ตอบ 500 ทำให้ monitoring เพี้ยนและ client handle auth error ยาก; filter/interceptor มี validation ซ้ำเพิ่ม DB/CPU overhead | token filter/configurer, `AuthInterceptor`, `CustomAuthenticationEntryPoint` | missing/invalid/expired token ตอบ `401`; authenticated แต่ไม่มีสิทธิ์ตอบ `403`; JWT parse/verify เกิดใน filter เป็นหลัก; interceptor ใช้ request attributes/session checks เท่าที่จำเป็น | tests สำหรับ missing token, invalid token, expired token, valid token; assert status code และ response body ไม่ leak exception detail |
| P0 | Mask error detail และ PII ใน response/transaction logs | หลาย repository/service โยน `ex.getMessage()` และ transaction log เก็บ request/response JSON; เสี่ยง leak SQL, JWT, S3 key, email, phone, address, Base64 file | `ExceptionAdvice`, repository/service catch blocks, `LoggerService`, `TransactionLogsRepository`, transaction log event handlers | client ได้ generic error code/message; internal exception detail อยู่เฉพาะ structured application logs; token/Base64/email/mobile/address ถูก mask หรือไม่ถูก persist ใน transaction log | unit tests สำหรับ exception response redaction; tests/fixtures ยืนยัน transaction log request/response mask sensitive fields เช่น `token`, `fileCert`, `imageProfile`, `email`, `mobileNumber` |
| P0 | ลบ/ลด startup logging ที่มี secret/config sensitive | `SmartSeamanMobileApiApplication` log config หลายตัวตอน startup เช่น DB URL, username, object store key prefix, JWT/encrypt key prefix; secret อาจหลุดใน log aggregation/support bundle | `SmartSeamanMobileApiApplication` | startup log เหลือเฉพาะ app name, profile, timezone, charset และ masked non-secret metadata; ห้าม log full DB URL, DB username/password, object-store key, JWT secret, encrypt key, FCM credential path ถ้าถือเป็น sensitive | review startup log string; unit/smoke test ถ้าทำได้ว่าค่า secret/env ไม่ปรากฏใน output |
| P0 | บังคับ Omise webhook signature verification ใน prod | webhook endpoint เป็น public; ตอนนี้ secret ว่างจะ skip verification เพื่อให้ dev/test รันง่าย แต่ prod ต้อง fail-closed | `OmiseWebhookService`, Omise config properties, deployment env | prod profile ต้อง fail-fast หรือ reject webhook ถ้า `OMISE_WEBHOOK_SECRET` ว่าง; invalid/missing signature ไม่ update payment/request; dev/test ยังใช้ secret ว่างได้ตาม profile policy | tests: no secret in prod, invalid signature, valid signature, duplicate webhook; deployment checklist ตรวจ Omise dashboard webhook URL และ secret |
| P1 | เพิ่มหรือ validate indexes สำหรับ hot queries | verified แล้วว่าบาง hot table ยังมีแค่ primary key; list/auth/log/payment paths ต้องมี index ตรง query จริงก่อน load สูง | new RUN script หรือ DB migration plan; `m_certificates`, `m_documents`, `t_transaction_logs`; existing `03_create_core_indexes.sql`/`01_create_mvp1_tables.sql` indexes | เพิ่มเฉพาะ verified indexes: `m_certificates(CERT_MOBILE_UUID, CERT_DOCUMENT_CODE)`, `m_certificates(CERT_MOBILE_UUID, CERT_END_DATE)`, `m_documents(DOCUMENT_STATUS, DOCUMENT_TYPE, DOCUMENT_SEQ)`, `t_transaction_logs(TRANS_ID)`; ไม่เพิ่ม `t_session` index ใหม่ตอนนี้เพราะ `03_create_core_indexes.sql` มี `CLIENT_SESSION_ID` และ `USER_ID` แล้ว | `EXPLAIN` ก่อน/หลังบน staging data; migration smoke test; rollback SQL; p95 latency/DB CPU comparison หลัง deploy |
| P1 | ตั้ง request size/content validation สำหรับ Base64/file endpoints | renewal multipart flow ใหม่มี validation แล้ว แต่ legacy certificate/profile/banner/news/voucher/document flows ยังมี Base64 decode/read ผ่าน memory และบางจุด preview/download เป็น Base64 string | legacy upload/download services/controllers, `DocumentUploadRequest`, `ProfileRequest`, file utilities, multipart config | reject oversized payload ก่อน decode เท่าที่ทำได้; validate decoded size, MIME/signature allowlist และ invalid Base64; allow only PNG/JPEG/PDF ตาม endpoint; response ไม่คืน storage key/path ตรง | tests สำหรับ oversized file, invalid Base64, unsupported MIME, valid PNG/JPEG/PDF; heap/memory smoke test สำหรับ payload ใหญ่ |

#### Immediate Action Execution Order

1. **Phase 1: ปิด exposure ที่ไม่กระทบ business logic**
   - Restrict CORS และปิด/จำกัด Swagger + Actuator ใน prod
   - ลบ/ลด startup logging ที่มี secret/config sensitive
2. **Phase 2: harden auth, error handling และ payment callback**
   - เปลี่ยน token/auth errors เป็น `401/403`
   - Mask error detail และ PII ใน response/transaction logs
   - บังคับ Omise webhook signature verification ใน prod
3. **Phase 3: performance และ file hardening**
   - เพิ่มหรือ validate indexes สำหรับ hot queries ด้วย `EXPLAIN`
   - ตั้ง request size/content validation สำหรับ Base64/file endpoints

#### Immediate Action Output Artifacts

- Config changes สำหรับ environment-specific CORS, public paths และ Omise webhook secret policy
- Focused tests สำหรับ security config, auth errors, exception/log redaction, Omise webhook signature และ file validation
- DB migration/RUN script สำหรับ verified indexes พร้อม rollback notes
- Deployment checklist สำหรับ prod:
  - allowed origins ถูกต้อง
  - swagger/actuator ไม่ public
  - `OMISE_WEBHOOK_SECRET` ถูกตั้ง
  - Omise dashboard webhook URL ตรงกับ `/v1/payments/omise/webhook`
  - startup log ไม่มี secret/config sensitive
- Verification notes หลัง deploy: sample curl, `EXPLAIN` result, และ p95 latency/DB metric baseline

### Next Iteration

1. Refactor auth ให้เหลือ authentication source เดียว
2. ทำ async transaction logging
3. เปลี่ยน file flow เป็น streaming หรือ presigned URL
4. เพิ่ม integration tests สำหรับ auth, upload, document validation, renewal payment webhook และ schema constraints
5. ทำ load test endpoint หลัก: login, document list, validate request items, renewal create, upload document, payment create/status

### Longer Term

1. Upgrade Spring Boot/dependencies
2. แยก config profile dev/staging/prod ให้ชัด
3. เพิ่ม centralized metrics: DB pool, request latency, external service latency, S3 latency
4. ใช้ migration tool เช่น Flyway/Liquibase แทน manual SQL script
5. เพิ่ม Testcontainers/MySQL 8 disposable schema เพื่อ smoke test RUN scripts และ migration order

## Suggested Priority Matrix

| Priority | Area | Work Item | Expected Impact | Evidence/Target |
| --- | --- | --- | --- | --- |
| P0 | Security | Lock down CORS, Swagger, Actuator | ลด exposure ทันที | `SecurityConfiguration.PUBLIC`, CORS allowlist |
| P0 | Security | Fix auth error status and token validation duplication | ลด 500 ปลอมและ DB overhead | `TokenFilter`, `AuthInterceptor` |
| P0 | Security | Remove sensitive startup/config logging | ลด secret leakage | `SmartSeamanMobileApiApplication` |
| P0 | Security | Enforce Omise webhook signature in prod | ป้องกัน forged payment callback | `OMISE_WEBHOOK_SECRET`, dashboard webhook |
| P1 | Security | Redact error/transaction logs | ลด PII leakage | global exception + transaction log service |
| P1 | Performance | Tune DB indexes and Hikari pool after load test | ลด p95 latency | `EXPLAIN`, Hikari metrics |
| P1 | Performance | Avoid Base64 memory-heavy file flow | ลด memory/GC pressure | legacy certificate/document upload |
| P1 | Observability | Add metrics for DB pool/external latency/webhook failures | เห็น bottleneck ก่อน production incident | Micrometer/actuator protected endpoint |
| P2 | Test Infra | Add MySQL 8 Testcontainers schema smoke tests | ลด migration/runtime drift | RUN scripts + CI |
| P2 | Maintainability | Add Flyway/Liquibase | ลด schema drift | migration baseline |
| P2 | Platform | Dependency upgrade plan | ลด CVE/compat risk | Spring Boot/JJWT upgrade plan |

## Notes For Current MVP Work

- `m_document_setting_requires` และ `m_document_profile_request_item` design เหมาะกับ validate missing document items แล้ว
- Query validation ควรคง `LEFT JOIN` เพื่อให้หา missing rows ได้ อย่าย้าย condition ของ profile item ไปไว้ใน `WHERE` แบบที่ทำให้ left join กลายเป็น inner join
- `m_delivery_address` แยกจาก `m_delivery` เป็นทิศทางที่ดี เพราะ user มีหลาย address ได้ และ delivery สามารถอ้าง address ที่เลือกได้
- Mobile renewal APIs ปัจจุบันมี focused tests ครอบคลุม contract หลักแล้ว แต่ยังไม่มี MySQL disposable integration test สำหรับ RUN scripts/constraints จริง
- Omise payment flow ลด risk จาก client-side success callback แล้ว แต่ production readiness ยังขึ้นกับ webhook secret, dashboard setup และ operational alerting
- `m_mobile_number_history` เป็น audit direction ที่ดี แต่ควรเพิ่ม policy retention/PII access control ในรอบ security hardening ถัดไป

## Validation Evidence

ล่าสุดหลังอัปเดต mobile renewal/payment/test work:

```bash
./mvnw test
```

ผลลัพธ์:

```text
Tests run: 99, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```
