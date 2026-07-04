# Smart Seaman Mobile API - Security And Performance Review

## Executive Summary

Project นี้เป็น Spring Boot 2.6.2 REST API แบบ stateless JWT สำหรับ mobile API มี layer แยกชัดเจนเป็น controller, service, repository, entity, model, config, filter และ interceptor โดย data access ส่วนใหญ่ใช้ `NamedParameterJdbcTemplate` กับ MySQL และ external service หลักคือ DigitalOcean Spaces, Gmail SMTP และ Firebase Cloud Messaging

จุดที่ควรปรับก่อน production hardening คือ security configuration, token validation flow, error handling, transaction logging, database indexing/query pattern และ file upload/download handling เพราะหลายจุดมีผลทั้ง security และ performance พร้อมกัน

## Project Structure Assessment

| Area | Current State | Assessment |
| --- | --- | --- |
| API Layer | `controller/` มี endpoint แยก domain เช่น auth, profile, document, banner, news | โครงสร้างอ่านง่าย แต่ validation ยังไม่สม่ำเสมอในทุก endpoint |
| Business Logic | `service/` ทำทั้ง business flow, transaction logging, S3 I/O | Service บางตัวรับผิดชอบหลายเรื่อง ควรแยก file/storage concerns เมื่อ flow โตขึ้น |
| Data Access | `repository/` ใช้ raw SQL + `NamedParameterJdbcTemplate` | ควบคุม SQL ได้ดี แต่ต้องดู index และ query plan เอง |
| Security | Spring Security + custom token filter + MVC auth interceptor | มี validation ซ้ำและ status code บางเคสไม่ถูกต้อง |
| Config | env var สำหรับ DB, JWT, S3, mail, FCM | ดีที่ secrets มาจาก env แต่บาง public/config defaults ต้อง harden |
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

5. **File upload เป็น Base64 string และไม่มี explicit size/content validation ชัดเจน**
   - Document flow รับ Base64 แล้ว upload เป็น string ไป S3
   - Risk: memory pressure, oversized payload, invalid content, stored malware
   - Recommendation: enforce max request size, validate MIME/signature, allowlist content types, stream upload แทนเก็บ payload ใหญ่ใน heap

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
   - `m_document_profile_request_item` มี unique `(mobile_user_uuid, document_master_request_item_code)` แล้วเหมาะกับ join

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

### Improvements

1. เพิ่ม index สำหรับ certificate list:

```sql
CREATE INDEX idx_cert_mobile_doc ON m_certificates (CERT_MOBILE_UUID, CERT_DOCUMENT_CODE);
CREATE INDEX idx_cert_mobile_end_date ON m_certificates (CERT_MOBILE_UUID, CERT_END_DATE);
```

2. เพิ่ม index สำหรับ document listing:

```sql
CREATE INDEX idx_documents_status_type_seq ON m_documents (DOCUMENT_STATUS, DOCUMENT_TYPE, DOCUMENT_SEQ);
```

3. สำหรับ transaction/session:

```sql
CREATE INDEX idx_session_client_online ON m_sessions (CLIENT_SESSION_ID, IS_ONLINE);
CREATE INDEX idx_transaction_logs_trace ON transaction_logs (TRACE_ID);
```

ต้องปรับชื่อ table/column ให้ตรง schema จริงก่อนรัน เพราะ repository entity บางตัวยังใช้ naming แบบ legacy uppercase/lowercase ผสมกัน

## Operational Recommendations

### Immediate Actions

1. Restrict CORS และปิด Swagger/Actuator ใน prod
2. เปลี่ยน token/auth errors จาก 500 เป็น 401/403
3. Mask error detail และ PII ใน response/transaction logs
4. เพิ่มหรือ validate index สำหรับ hot queries
5. ตั้ง request size limit สำหรับ Base64/file endpoints

### Next Iteration

1. Refactor auth ให้เหลือ authentication source เดียว
2. ทำ async transaction logging
3. เปลี่ยน file flow เป็น streaming หรือ presigned URL
4. เพิ่ม integration tests สำหรับ auth, upload, document validation
5. ทำ load test endpoint หลัก: login, document list, validate request items, upload document

### Longer Term

1. Upgrade Spring Boot/dependencies
2. แยก config profile dev/staging/prod ให้ชัด
3. เพิ่ม centralized metrics: DB pool, request latency, external service latency, S3 latency
4. ใช้ migration tool เช่น Flyway/Liquibase แทน manual SQL script

## Suggested Priority Matrix

| Priority | Area | Work Item | Expected Impact |
| --- | --- | --- | --- |
| P0 | Security | Lock down CORS, Swagger, Actuator | ลด exposure ทันที |
| P0 | Security | Fix auth error status and token validation duplication | ลด 500 ปลอมและ DB overhead |
| P1 | Performance | Tune DB indexes and Hikari pool after load test | ลด p95 latency |
| P1 | Security | Redact error/transaction logs | ลด PII leakage |
| P1 | Performance | Avoid Base64 memory-heavy file flow | ลด memory/GC pressure |
| P2 | Maintainability | Add Flyway/Liquibase | ลด schema drift |
| P2 | Platform | Dependency upgrade plan | ลด CVE/compat risk |

## Notes For Current MVP Work

- `m_document_setting_requires` และ `m_document_profile_request_item` design เหมาะกับ validate missing document items แล้ว
- Query validation ควรคง `LEFT JOIN` เพื่อให้หา missing rows ได้ อย่าย้าย condition ของ profile item ไปไว้ใน `WHERE` แบบที่ทำให้ left join กลายเป็น inner join
- `m_delivery_address` แยกจาก `m_delivery` เป็นทิศทางที่ดี เพราะ user มีหลาย address ได้ และ delivery สามารถอ้าง address ที่เลือกได้
