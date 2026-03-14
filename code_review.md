# Code Review — Smart Seaman Mobile API

> วันที่ตรวจสอบ: 7 มีนาคม 2569
> เวอร์ชัน: Spring Boot 2.6.2 / Java 11

---

## Executive Summary

ตรวจพบประเด็นที่ควรปรับปรุงทั้งหมด **22 รายการ** แบ่งตาม severity ดังนี้:

| Severity | จำนวน | ความหมาย |
|----------|--------|----------|
| 🔴 Critical | 5 | ต้องแก้ทันที — ความเสี่ยงด้าน Security |
| 🟠 High | 6 | ควรแก้เร็ว — กระทบ Architecture |
| 🟡 Medium | 7 | ควรแก้ใน sprint ถัดไป — Dependencies / Config |
| 🔵 Low | 4 | แก้ได้เมื่อมีเวลา — Code Quality |

---

## 🔴 Critical — Security

### 1. Hardcoded credentials ใน `application.properties`

**ไฟล์:** `src/main/resources/application.properties`

ข้อมูลลับทั้งหมดถูก hardcode ไว้โดยตรง:

| ค่า | บรรทัด |
|-----|--------|
| DB password | 19 |
| S3 secret key | 30 |
| JWT secret | 55 |
| FCM server key | 70 |
| Gmail app password | 81 |

**วิธีแก้:** ย้ายค่าทั้งหมดไปใช้ environment variable ผ่าน `.env` file (project มี `spring-dotenv` อยู่แล้ว)

```properties
# application.properties
smart.seaman.datasource.password=${DB_PASSWORD}
object.store.secret=${S3_SECRET}
jwt.secret=${JWT_SECRET}
fcm.auth=${FCM_AUTH_KEY}
spring.mail.password=${MAIL_PASSWORD}
```

---

### 2. Firebase credential file ในเครื่อง

**ไฟล์:** `src/main/resources/smart-seaman-firebase-adminsdk-5q8as-80c908a305.json`

ไฟล์นี้เป็น service account key ที่มีสิทธิ์เต็มต่อ Firebase project ไม่ควรอยู่ใน codebase เลย

**วิธีแก้:**
1. เพิ่มไฟล์นี้ใน `.gitignore`
2. ใช้ environment variable ส่ง JSON content แทน หรือโหลดจาก secret manager

---

### 3. CORS อนุญาตเฉพาะ `http://`

**ไฟล์:** `src/main/java/com/seaman/config/SecurityConfiguration.java:57`

```java
cors.setAllowedOriginPatterns(Collections.singletonList("http://*"));
```

รับเฉพาะ HTTP origin ทำให้ production client ที่ใช้ HTTPS ถูก block หากมีการ enforce CORS จาก browser

**วิธีแก้:** เปลี่ยนเป็น `https://*` หรือระบุ origin ที่อนุญาตชัดเจนใน property file

---

### 4. JWT Library เวอร์ชันเก่า (jjwt 0.9.1)

**ไฟล์:** `pom.xml:144`

`jjwt 0.9.1` ออกมาปี 2018 มี known issues และ API ที่ deprecated ทั้งหมด

**วิธีแก้:** Upgrade เป็น `io.jsonwebtoken:jjwt-api:0.12.x` พร้อม `jjwt-impl` และ `jjwt-jackson`

---

### 5. Dead code — Commented-out token validation

**ไฟล์:** `src/main/java/com/seaman/filter/TokenFilter.java:71-74`

```java
//        if(jwtTokenService.validateToken(token).equals(Boolean.FALSE)){
//            filterChain.doFilter(servletRequest, servletResponse);
//            return;
//        }
```

Code ที่ comment ออกและไม่ได้ใช้ทำให้สับสนว่า validation ทำงานจริงหรือเปล่า

**วิธีแก้:** ลบ dead code ออก

---

## 🟠 High — Architecture & Design

### 6. `spring.main.allow-circular-references=true`

**ไฟล์:** `src/main/resources/application.properties:5`

การ enable circular references คือการปิดบัง design problem ที่แท้จริง ไม่ใช่การแก้ปัญหา

**วิธีแก้:** หา circular dependency ที่เกิดขึ้นจริงและแก้ด้วย `@Lazy` injection หรือ refactor ให้ dependency ไหลทางเดียว

---

### 7. `WebSecurityConfigurerAdapter` Deprecated

**ไฟล์:** `src/main/java/com/seaman/config/SecurityConfiguration.java:26`

`WebSecurityConfigurerAdapter` deprecated ตั้งแต่ Spring Security 5.7 (Spring Boot 2.7) และถูกลบใน Spring Security 6

**วิธีแก้:** Migrate เป็น `SecurityFilterChain` bean pattern:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // ...
    return http.build();
}
```

---

### 8. Duplicate `ExceptionResponse`

มี class ชื่อเดียวกันอยู่ 2 ที่:
- `src/main/java/com/seaman/model/common/ExceptionResponse.java`
- `src/main/java/com/seaman/model/response/ExceptionResponse.java`

ทำให้ import สับสนและอาจใช้ผิด class โดยไม่รู้ตัว

**วิธีแก้:** เหลือไว้แค่ `model/common/ExceptionResponse.java` และลบอีกอันทิ้ง

---

### 9. Entity ไม่ใช้ JPA Annotation

Entity ทั้งหมด (เช่น `UsersEntity`, `CertificateEntity`) เป็น plain POJO ที่ใช้ raw JDBC ผ่าน `JdbcTemplate` และ `RowMapper` ทำให้:
- ไม่มี type-safe query
- Query SQL กระจายอยู่ใน service/repository layer ยากต่อการ maintain
- ไม่มี lazy loading / relationship management

**วิธีแก้ระยะยาว:** Migrate เป็น Spring Data JPA พร้อม `@Entity`, `@Table`, `@Column`

---

### 10. `CommonEntity` ใช้ `java.util.Date`

**ไฟล์:** `src/main/java/com/seaman/entity/CommonEntity.java:13-16`

```java
protected Date createDate;
protected Date updateDate;
```

`java.util.Date` เป็น legacy API มีปัญหาเรื่อง timezone และ mutability

**วิธีแก้:** เปลี่ยนเป็น `LocalDateTime` หรือ `Instant`

---

### 11. `NotificationModel` อยู่ผิด Package

**ไฟล์:** `src/main/java/com/seaman/model/request/NotificationModel.java`

ชื่อ `NotificationModel` แต่อยู่ใน `request/` package ทำให้โครงสร้างสับสน

**วิธีแก้:** ย้ายไปที่ `model/` หรือเปลี่ยนชื่อให้สื่อความหมายชัดเจน

---

## 🟡 Medium — Configuration & Dependencies

### 12. Spring Boot 2.6.2 — EOL

Spring Boot 2.x หมดอายุ support แล้ว (November 2024) ไม่มี security patch ใหม่

**วิธีแก้:** Upgrade เป็น Spring Boot 3.3.x (ต้องการ Java 17+)

---

### 13. Java 11 — ไม่ใช่ LTS ปัจจุบัน

Java 11 EOL แล้วสำหรับ community support, Java 21 คือ LTS ล่าสุด

**วิธีแก้:** Upgrade เป็น Java 21

---

### 14. `spring-boot-admin 3.0.0-M4` Version Mismatch

**ไฟล์:** `pom.xml:27`

ใช้ Spring Boot Admin 3.x (milestone) กับ Spring Boot 2.6.x ซึ่งไม่ compatible กัน

**วิธีแก้:** ถ้าใช้ Spring Boot 2.x ให้ใช้ `spring-boot-admin 2.7.x`

---

### 15. `springdoc-openapi-ui 1.2.32` — เก่ามาก

**ไฟล์:** `pom.xml:121`

เวอร์ชัน 1.2.32 ออกมาปี 2020 ล้าหลังมากและมี bug fixes จำนวนมากใน version หลังจากนั้น

**วิธีแก้:** Upgrade เป็น `1.8.0` (Spring Boot 2) หรือ `springdoc-openapi-starter-webmvc-ui:2.x` (Spring Boot 3)

---

### 16. `spring-cloud-starter-sleuth` Deprecated

**ไฟล์:** `pom.xml:156`

Spring Cloud Sleuth ถูก deprecated และไม่รองรับ Spring Boot 3

**วิธีแก้:** ถ้า upgrade เป็น Spring Boot 3 ให้ใช้ Micrometer Tracing แทน:
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
```

---

### 17. Scheduled Task — Cron Expression อาจผิดจาก Intent

**ไฟล์:** `src/main/resources/application.properties:58`

```properties
cache.scheduled.notification=0 0/1 * ? * *
```

Cron นี้รัน **ทุก 1 นาที** แต่ CLAUDE.md ระบุว่า "refreshes notification cache **daily**"

**วิธีแก้:** ตรวจสอบ intent จริง ถ้าต้องการ daily ควรเปลี่ยนเป็น `0 0 0 * * ?`

---

### 18. ไม่มี Database Migration Tool

Schema จัดการแบบ manual ผ่าน SQL script ใน `src/main/resources/SQL/` ทำให้:
- ไม่มี version tracking
- Schema อาจ drift ระหว่าง dev/staging/prod

**วิธีแก้:** เพิ่ม Flyway หรือ Liquibase

---

## 🔵 Low — Code Quality & Maintainability

### 19. ไม่มี Unit/Integration Test จริง

Test ที่มีอยู่:
- `PocTest.java` — POC scratch test
- `PocData.java` — test data helper
- `SmartSeamanBosApiApplicationTests.java` — context load test เท่านั้น

ไม่มี test สำหรับ Service, Controller, หรือ business logic ใด ๆ

**วิธีแก้:** เขียน unit test สำหรับ Service layer โดยเริ่มจาก critical path (Auth, Document, Notification)

---

### 20. ไม่มี Rate Limiting

ไม่มีการจำกัด request rate บน endpoint ใด ๆ โดยเฉพาะ:
- `POST /v1/login` — เสี่ยง brute force
- `POST /v1/register` — เสี่ยง spam account
- `POST /v1/forgot-password` — เสี่ยง email flooding

**วิธีแก้:** เพิ่ม rate limiting ด้วย Bucket4j หรือ Spring Cloud Gateway

---

### 21. `jmimemagic` Library ไม่ได้รับการ Maintain

**ไฟล์:** `pom.xml:169`

`jmimemagic 0.1.2` ไม่มี update มากกว่า 10 ปีและมี known issues

**วิธีแก้:** เปลี่ยนเป็น Apache Tika:
```xml
<dependency>
    <groupId>org.apache.tika</groupId>
    <artifactId>tika-core</artifactId>
    <version>2.9.x</version>
</dependency>
```

---

### 22. AWS SDK v1

**ไฟล์:** `pom.xml:161`

`aws-java-sdk-s3 1.12.261` คือ SDK v1 ที่อยู่ใน maintenance mode AWS แนะนำให้ใช้ SDK v2

**วิธีแก้ระยะยาว:** Migrate เป็น `software.amazon.awssdk:s3`

---

## Recommended Upgrade Path

### Quick Wins (แก้ได้ใน 1–2 วัน)

| # | งาน | ผลกระทบ |
|---|-----|---------|
| 1 | ย้าย credentials ออกจาก `application.properties` ไป `.env` | 🔴 Security |
| 2 | เพิ่ม Firebase JSON ใน `.gitignore` | 🔴 Security |
| 3 | ลบ dead code ใน `TokenFilter.java` | 🔴 Code clarity |
| 4 | ลบ `ExceptionResponse` ที่ duplicate | 🟠 Maintainability |
| 5 | แก้ Cron expression ให้ตรงกับ intent จริง | 🟡 Correctness |

### Medium-term (1–2 sprint)

| # | งาน | ผลกระทบ |
|---|-----|---------|
| 6 | Upgrade `jjwt` เป็น `0.12.x` | 🔴 Security |
| 7 | แก้ CORS ให้รองรับ HTTPS | 🔴 Security |
| 8 | Migrate `WebSecurityConfigurerAdapter` เป็น `SecurityFilterChain` | 🟠 Architecture |
| 9 | Upgrade `springdoc-openapi-ui` | 🟡 Dependency |
| 10 | แก้ `spring-boot-admin` version ให้ตรงกับ Spring Boot | 🟡 Dependency |
| 11 | เพิ่ม Flyway/Liquibase | 🟡 DB Management |
| 12 | เพิ่ม Rate Limiting | 🔵 Security |

### Long-term (Roadmap)

| # | งาน | ผลกระทบ |
|---|-----|---------|
| 13 | Upgrade Java 11 → Java 21 | Performance / LTS |
| 14 | Upgrade Spring Boot 2.6 → 3.3 | Security / LTS |
| 15 | Migrate Entity เป็น Spring Data JPA | Maintainability |
| 16 | เพิ่ม Unit/Integration Test suite | Quality |
| 17 | Migrate AWS SDK v1 → v2 | Dependency |
| 18 | เปลี่ยน `jmimemagic` เป็น Apache Tika | Dependency |
