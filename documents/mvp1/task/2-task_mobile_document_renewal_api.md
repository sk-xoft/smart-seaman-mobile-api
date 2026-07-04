# Mobile Document Renewal API Tasks

อ้างอิง: `documents/mvp1/document_renewal_figma_spec.md` ส่วน `Mobile APIs`

ตรวจสถานะจาก source code ณ วันที่ 4 กรกฎาคม 2026 โดยถือว่า task เป็น **ทำแล้ว** เมื่อมี route, controller, service, repository/SQL mapping, request/response model, validation และ test ที่เกี่ยวข้องครบตาม acceptance criteria

## Status Legend

- [x] ทำแล้ว
- [~] ทำบางส่วน
- [ ] ยังไม่ได้ทำ
- [?] มี script/design แล้ว แต่ยังยืนยัน runtime/deployment ไม่ได้

## Current Status

| ลำดับ | Status | Method | API | หมายเหตุ |
|---:|:---:|---|---|---|
| 1 | [ ] | GET | `/v1/document-renewals/statuses` | มี schema และ seed `m_document_status` แล้ว แต่ยังไม่มี Java API |
| 2 | [ ] | GET | `/v1/document-renewals/prices?documentCode={code}` | มี schema `m_document_prices_setting` แล้ว แต่ยังไม่มี Java API และยังไม่พบ seed ราคา |
| 3 | [ ] | POST | `/v1/document-renewals` | ยังไม่มี controller/service/repository/model สำหรับสร้าง renewal request |
| 4 | [ ] | GET | `/v1/document-renewals/my?offSet={n}` | ยังไม่มี API รายการคำขอของ user |
| 5 | [ ] | GET | `/v1/document-renewals/{requestId}` | ยังไม่มี API รายละเอียดคำขอ |
| 6 | [ ] | GET | `/v1/document-renewals/{requestId}/timeline` | ยังไม่มี API timeline |
| 7 | [ ] | POST | `/v1/document-renewals/{requestId}/items/{itemId}/file` | ยังไม่มี API upload/replace file สำหรับ renewal item |
| 8 | [ ] | POST | `/v1/document-renewals/{requestId}/resubmit` | ยังไม่มี API resubmit และ state transition |
| 9 | [ ] | POST | `/v1/document-renewals/{requestId}/payments` | มี schema payment แล้ว แต่ยังไม่มี payment service/provider integration/API |
| 10 | [ ] | GET | `/v1/document-renewals/{requestId}/payments/{transactionId}` | ยังไม่มี API ตรวจ payment status |

สรุป Mobile APIs: **ทำแล้ว 0/10, ทำบางส่วน 0/10, ยังไม่ได้ทำ 10/10**

## Prerequisites And Supporting Work

| Status | งาน | หลักฐาน/หมายเหตุ |
|:---:|---|---|
| [x] | API ตรวจ required document ที่ขาด/ต้องแก้ไข | `GET /v1/documents/request-items/validate`; มี controller, service, repository query และ response model แล้ว |
| [?] | Schema สำหรับ renewal flow | มี DDL ใน `RUN1_2026_create_table.sql`; ไม่สามารถสรุปว่า deploy แล้วจาก source code |
| [?] | Status master seed | มี upsert 7 statuses ใน `RUN2_2026_insert_master.sql`; ไม่สามารถสรุปว่า run แล้วจาก source code |
| [~] | Required document master/setting | มี schema, seed ตัวอย่าง `DOC001` และ validation API แล้ว แต่ยังไม่ยืนยัน master จริงครบทุก `document_code` |
| [ ] | Renewal price master data | มี table แต่ยังไม่พบ seed/config ราคาที่ใช้งานจริง |
| [ ] | Delivery address decision | schema มี `m_delivery_address` แล้ว แต่ spec ยังต้องยืนยัน UX/API สำหรับเลือกหรือ snapshot ที่อยู่ |
| [ ] | Create/update delivery address APIs | เพิ่มเป็น MR-MOB-14; ยังไม่มี route/controller/service/repository/request/response model |
| [x] | Thailand address master APIs | implement route/controller/service/repository/response model แล้ว; focused tests ผ่าน 5/5 และ pin Lombok ให้รองรับ JDK 21 แล้ว |
| [ ] | Payment provider decision | ต้องยืนยัน channel, charge flow และ webhook ก่อนปิด payment tasks |
| [ ] | Automated tests สำหรับ renewal flow | ยังไม่พบ test ของ renewal API |

## Task Breakdown

### MR-MOB-01: Shared Renewal Foundation

Status: [x] ทำแล้ว

- เพิ่ม route constants กลุ่ม `/document-renewals`
- เพิ่ม entity/DTO/repository สำหรับ `m_document_status`, `m_document_request`, `m_document_prices_setting`, `m_document_request_items`, `m_document_transaction`, `m_payment_transaction`, `m_dept_submission`, `m_delivery` และ `m_delivery_address` เท่าที่ mobile API ใช้
- สร้าง service กลางสำหรับตรวจ ownership ด้วย `mobile_user_uuid`
- กำหนด enum/constant ของ status และ action โดยไม่ผูก business logic กับชื่อภาษาไทย
- กำหนด transaction boundary เพื่อให้การเปลี่ยน status กับ append timeline สำเร็จหรือ rollback พร้อมกัน
- เพิ่ม unit/integration test foundation

Acceptance criteria:

- user อ่านหรือแก้ไขได้เฉพาะ request ของตัวเอง
- ทุก query ใช้ชื่อ table/column ตรงกับ RUN1
- การเปลี่ยนสถานะทุกครั้งเพิ่ม `m_document_transaction` ใน transaction เดียวกัน

### MR-MOB-02: Get Renewal Status Master

Status: [ ] ยังไม่ได้ทำ

API: `GET /v1/document-renewals/statuses`

- ดึงเฉพาะ `is_active = 'YES'`
- map `id`, `nameTh`, `nameEn`, `cssColor` และ mobile progress step
- กำหนดลำดับ 5 main steps และแยก correction/cancel ให้ตรง spec

Acceptance criteria:

- response ครบ 7 statuses จาก spec เมื่อ master data ครบ
- `รอผู้ยื่นแก้ไข` map เป็น step 1 และ `ยกเลิก` ไม่เป็น normal progress step

หมายเหตุ: schema ปัจจุบันไม่มี column `step`; ต้องกำหนดว่าจะเก็บใน DB หรือ map ใน application ก่อน implement

### MR-MOB-03: Get Renewal Price

Status: [ ] ยังไม่ได้ทำ

API: `GET /v1/document-renewals/prices?documentCode={code}`

- validate `documentCode`
- ดึง active/effective price ของเอกสาร
- ส่ง fee breakdown และยอดรวมด้วย `DECIMAL`/`BigDecimal`
- กำหนด behavior เมื่อไม่พบราคา หรือมีช่วง effective date ซ้อนกัน

Acceptance criteria:

- ไม่ใช้ floating-point กับจำนวนเงิน
- คืนราคาเฉพาะ document ที่เปิด renewal และราคาอยู่ในช่วงใช้งาน
- มี test กรณีพบราคา, ไม่พบราคา และ config ซ้ำ

### MR-MOB-04: Create Renewal Request

Status: [ ] ยังไม่ได้ทำ

API: `POST /v1/document-renewals`

- validate document เปิดให้ renewal
- validate required document items จาก profile
- snapshot ราคาและข้อมูลที่จำเป็นลง request
- generate `request_no` แบบ concurrency-safe
- สร้าง request items จาก required document setting
- กำหนด initial status ตาม payment decision
- append transaction action `CREATE` เมื่อเข้า `รอตรวจเอกสาร`

Acceptance criteria:

- ไม่เชื่อถือ `mobileUserUuid`, ราคา หรือ status จาก request body
- `request_no` unique แม้สร้างพร้อมกัน
- ไม่เข้า `รอตรวจเอกสาร` ก่อน payment success เว้นแต่ product ยืนยัน unpaid draft flow
- rollback ทั้ง request/items/transaction เมื่อขั้นตอนใดล้มเหลว

Blocked decisions:

- รูปแบบ running number
- unpaid draft หรือ create หลัง payment success
- delivery address snapshot/selection

### MR-MOB-05: List My Renewal Requests

Status: [ ] ยังไม่ได้ทำ

API: `GET /v1/document-renewals/my?offSet={n}`

- ดึงรายการด้วย user จาก JWT/request context เท่านั้น
- paginate และเรียงล่าสุดก่อน
- map request summary, current status, amount และ `isResubmit`

Acceptance criteria:

- response มี `itemTotal`, `isLast` และ items
- วันที่แสดงเป็น `DD/MM/YYYY HH:mm`
- ไม่คืน request ของ user อื่น

### MR-MOB-06: Get Renewal Request Detail

Status: [ ] ยังไม่ได้ทำ

API: `GET /v1/document-renewals/{requestId}`

- ตรวจ ownership
- คืน header, current status, supporting items, current status detail, department submission และ delivery เฉพาะส่วนที่เกี่ยวข้อง
- แสดง `checkNote` สำหรับ item ที่เป็น `fix`
- สร้าง file access ที่ไม่เปิด object storage path แบบถาวรโดยไม่มี authorization

Acceptance criteria:

- mobile เห็นเฉพาะรายละเอียดของ current status ไม่รวม future status detail
- response แสดง correction state และ rejected item note ชัดเจน
- delivery data แสดงเมื่อ status เกี่ยวกับการจัดส่ง

### MR-MOB-07: Get Renewal Timeline

Status: [ ] ยังไม่ได้ทำ

API: `GET /v1/document-renewals/{requestId}/timeline`

- ตรวจ ownership
- อ่าน append-only rows จาก `m_document_transaction`
- map action/from/to status, timestamp และ display detail

Acceptance criteria:

- เรียงตามเวลาที่เกิดเหตุการณ์อย่างแน่นอน
- วันที่แสดงเป็น `DD/MM/YYYY HH:mm`
- ไม่ expose internal/admin-only note ที่ไม่ควรแสดงบน mobile

### MR-MOB-08: Upload Or Replace Supporting File

Status: [ ] ยังไม่ได้ทำ

API: `POST /v1/document-renewals/{requestId}/items/{itemId}/file`

- ตรวจ ownership และ item ต้องอยู่ใน request
- รองรับ upload format ที่ product เลือก
- validate MIME type, size และ empty payload
- upload object storage แล้ว update metadata อย่างปลอดภัย
- เมื่อ replace ใน correction flow ให้ตั้ง `is_updated = 1` และ reset review fields ตามกติกา

Acceptance criteria:

- user แก้ file ได้เฉพาะ state ที่อนุญาต
- DB ไม่ชี้ไฟล์ใหม่หาก upload ล้มเหลว และจัดการ orphan object เมื่อ DB update ล้มเหลว
- ไม่ใช้ชื่อไฟล์จาก client เป็น storage key โดยตรง

Blocked decision: multipart, base64 หรือ pre-signed URL

### MR-MOB-09: Resubmit Corrected Documents

Status: [ ] ยังไม่ได้ทำ

API: `POST /v1/document-renewals/{requestId}/resubmit`

- อนุญาตเฉพาะ status `รอผู้ยื่นแก้ไข`
- validate ว่า item ที่เป็น `fix` ถูก replace ครบ
- เปลี่ยน status เป็น `รอตรวจเอกสาร`
- append transaction action `RESUBMIT`
- trigger FCM/in-app notification ตาม spec

Acceptance criteria:

- concurrent/double submit ไม่สร้าง transition ซ้ำ
- status update และ timeline เป็น atomic transaction
- item ที่แก้แล้วแสดง `is_updated = 1`

### MR-MOB-10: Create Payment Attempt

Status: [ ] ยังไม่ได้ทำ

API: `POST /v1/document-renewals/{requestId}/payments`

- ตรวจ ownership และ request ยังชำระได้
- สร้างหนึ่ง `m_payment_transaction` ต่อ attempt
- เรียก payment provider แบบ idempotent
- คืน QR/redirect/card payload ตาม channel
- อนุญาต attempt ใหม่เมื่อรายการเดิม expired/failed ตามกติกา

Acceptance criteria:

- amount มาจาก server-side request snapshot
- client retry ไม่สร้าง charge ซ้ำเมื่อ idempotency key เดิม
- ไม่ log credential หรือข้อมูล payment sensitive
- success จาก client callback อย่างเดียวไม่สามารถเปลี่ยน request status ได้

Blocked decision: payment provider/channel และ credential setup

### MR-MOB-11: Get Payment Status

Status: [ ] ยังไม่ได้ทำ

API: `GET /v1/document-renewals/{requestId}/payments/{transactionId}`

- ตรวจ ownership และ transaction ต้องอยู่ใน request
- คืน internal normalized status พร้อม provider reference/expiry ที่ mobile ต้องใช้
- ใช้ webhook เป็น source of truth หรือ reconcile provider เมื่อจำเป็น

Acceptance criteria:

- ไม่ expose raw provider payload หรือข้อมูล sensitive
- เมื่อ payment success ให้ request เข้า `รอตรวจเอกสาร` เพียงครั้งเดียว
- payment success append transaction และส่ง notification ตาม spec

Dependency: ต้องทำ payment webhook/system flow ร่วมกับ task นี้ แม้ webhook ไม่ใช่ Mobile API โดยตรง

### MR-MOB-12: Mobile Renewal API Tests And Contract

Status: [ ] ยังไม่ได้ทำ

- controller contract tests สำหรับทั้ง 10 endpoints
- service tests สำหรับ ownership, validation และ state transition
- repository integration tests กับ MySQL 8 disposable schema
- concurrency tests สำหรับ request number, resubmit และ payment success
- OpenAPI examples ให้ตรง response shapes ใน spec

Acceptance criteria:

- ครอบคลุม happy path, invalid state, forbidden ownership, not found และ duplicate callback/retry
- test ยืนยันทุก status change มี timeline row
- test ยืนยัน date format และ money precision

### MR-MOB-13: Thailand Address Master APIs

Status: [ ] ยังไม่ได้ทำ

APIs:

| Method | API | Purpose |
|---|---|---|
| GET | `/v1/master/provinces` | ดึงรายการจังหวัดทั้งหมด |
| GET | `/v1/master/districts?provinceCode={provinceCode}` | ดึงรายการอำเภอ/เขตตามจังหวัด |
| GET | `/v1/master/subdistricts?districtCode={districtCode}` | ดึงรายการตำบล/แขวงตามอำเภอ/เขต |

- import และกำหนด source of truth ของข้อมูลเขตการปกครองจากแหล่งที่ระบุใน `documents/mvp1/script/RUN4_external_data`
- ตรวจชื่อ table, column, primary key, foreign key, charset และ collation ของ external data ก่อนเขียน repository query
- เพิ่ม route, controller, service, repository, entity และ response model
- เรียงข้อมูลตามชื่อภาษาไทย
- รองรับ `Accept-Language` โดยคืน `name` ตามภาษา และคง `nameTh`/`nameEn` ใน response หาก mobile ต้องใช้ทั้งสองภาษา
- คืน postal code ใน subdistrict response ถ้าชุดข้อมูลต้นทางมีค่า
- validate `provinceCode` และ `districtCode`; ไม่รับ parent code ที่ว่าง
- กำหนด behavior ให้ชัดเจนระหว่างคืน empty list กับ not found เมื่อ parent code ไม่มีอยู่จริง

Response fields ขั้นต่ำ:

```json
{
  "code": "10",
  "name": "กรุงเทพมหานคร",
  "nameTh": "กรุงเทพมหานคร",
  "nameEn": "Bangkok"
}
```

สำหรับ subdistrict เพิ่ม `postalCode` เมื่อข้อมูลต้นทางรองรับ

Acceptance criteria:

- province API คืนจังหวัดที่ active/ใช้งานได้ทั้งหมดโดยไม่ซ้ำ
- district API คืนเฉพาะอำเภอ/เขตที่อยู่ใต้ `provinceCode` ที่ระบุ
- subdistrict API คืนเฉพาะตำบล/แขวงที่อยู่ใต้ `districtCode` ที่ระบุ
- query ใช้ index บน parent code และไม่มี full table scan สำหรับ district/subdistrict lookup
- response อยู่ภายใต้ `SuccessResponse` ตามมาตรฐาน API ปัจจุบัน
- endpoints ต้องผ่าน JWT authentication ตาม security convention ปัจจุบัน เว้นแต่ product ระบุให้เป็น public master data
- มี controller/service/repository tests สำหรับ valid parent, invalid parent, missing parameter และ empty result

Blocked decisions:

- ยืนยันว่าต้องการให้ master address endpoints เป็น public หรือ authenticated

Implementation decisions:

- ใช้ schema ต้นทาง `provinces`, `districts`, `subdistricts` ตาม `RUN4_external_data`
- endpoints เป็น authenticated ตาม security convention ปัจจุบัน
- `Accept-Language: EN` คืน `name` ภาษาอังกฤษ; ค่าอื่นหรือไม่ส่ง header คืนภาษาไทย
- parent code ที่ไม่มีข้อมูลคืน empty list; code ที่ไม่เป็นบวกคืน validation error
- subdistrict response คืน `postalCode` เมื่อมี `zip_code`
- runtime database ต้อง import tables/data จาก `RUN4_external_data` ก่อนใช้งานจริง
- focused test: `./mvnw test -Dtest=MasterDataServiceAddressTest` ผ่าน 5/5 tests
- full test suite: `./mvnw test` ผ่าน 6/6 tests

### MR-MOB-14: Create And Update Delivery Address

Status: [ ] ยังไม่ได้ทำ

APIs:

| Method | API | Purpose |
|---|---|---|
| POST | `/v1/delivery-addresses` | สร้างที่อยู่จัดส่ง |
| PUT | `/v1/delivery-addresses/{addressId}` | แก้ไขที่อยู่จัดส่ง |

- สร้างที่อยู่สำหรับจัดส่งเอกสารของผู้ใช้ที่ login อยู่
- อ่าน `mobile_user_uuid` จาก JWT/request context เท่านั้น ห้ามรับจาก request body
- บันทึกข้อมูลลง `m_delivery_address` โดยใช้ fields:
  - `firstName`
  - `lastName`
  - `addressLine`
  - `province`
  - `district`
  - `subDistrict`
  - `postalCode`
  - `isDefault`
- validate required fields, ความยาวข้อมูล และรูปแบบ `postalCode`
- validate จังหวัด อำเภอ/เขต ตำบล/แขวง และรหัสไปรษณีย์กับ Thailand address master data
- ถ้าเป็นที่อยู่แรกของผู้ใช้ ให้ตั้ง `is_default = 1` อัตโนมัติ
- ถ้า request ระบุ `isDefault = true` ให้ยกเลิก default เดิมก่อนตั้งรายการใหม่เป็น default ภายใน transaction เดียวกัน
- กำหนด `is_active = 'YES'` จากฝั่ง server เท่านั้น
- response คืน `id` และข้อมูลที่อยู่ที่สร้างสำเร็จภายใต้ `SuccessResponse`
- การแก้ไขต้องค้นหาที่อยู่ด้วย `addressId` และ `mobile_user_uuid` จาก JWT เพื่อยืนยัน ownership
- อนุญาตให้แก้ไขเฉพาะที่อยู่ที่มี `is_active = 'YES'`
- การแก้ไขรองรับ fields ชุดเดียวกับ create แต่ห้ามเปลี่ยน `id`, `mobile_user_uuid` และ `is_active` จาก request body
- หากแก้ไข `isDefault = true` ให้ยกเลิก default เดิมและตั้งรายการนี้เป็น default ภายใน transaction เดียวกัน
- หากแก้ไข default address ปัจจุบันเป็น `isDefault = false` ต้องกำหนด behavior ให้ชัดเจน: ปฏิเสธคำขอ หรือเลือก active address อื่นเป็น default โดยอัตโนมัติ
- update `updated_at` เมื่อแก้ไขสำเร็จ และ response คืนข้อมูลล่าสุดภายใต้ `SuccessResponse`

Acceptance criteria:

- user ไม่สามารถสร้างที่อยู่ให้ `mobile_user_uuid` ของ user อื่นได้
- user ไม่สามารถอ่านหรือแก้ไขที่อยู่ของ user อื่นผ่าน `addressId` ได้
- ข้อมูลจังหวัด อำเภอ/เขต และตำบล/แขวงต้องสัมพันธ์กันตาม master data
- ผู้ใช้มี active default address ได้ไม่เกินหนึ่งรายการ
- การเปลี่ยน default ต้อง atomic และ rollback ทั้งหมดเมื่อบันทึกไม่สำเร็จ
- duplicate request/retry ต้องไม่ทำให้เกิด default address มากกว่าหนึ่งรายการ
- update address ที่ไม่มีอยู่, เป็นของ user อื่น หรือ inactive ต้องไม่แก้ไขข้อมูล
- มี controller/service/repository tests สำหรับ create/update happy path, invalid address hierarchy, invalid postal code, first address, replace default, not found, forbidden ownership, inactive address และ unauthenticated request
- OpenAPI ระบุ request/response example และ validation error

## Recommended Implementation Order

1. ปิด open decisions: request number, unpaid draft, address, upload format, payment channel
2. MR-MOB-01 shared foundation
3. MR-MOB-02 status และ MR-MOB-03 price
4. MR-MOB-04 create request
5. MR-MOB-08 upload และ MR-MOB-09 resubmit
6. MR-MOB-05 list, MR-MOB-06 detail และ MR-MOB-07 timeline
7. MR-MOB-10 payment attempt, payment webhook และ MR-MOB-11 payment status
8. MR-MOB-13 Thailand address master APIs ก่อนเริ่ม delivery-address UI/API
9. MR-MOB-14 create/update delivery address APIs
10. MR-MOB-12 contract/integration/concurrency tests

## Evidence Checked

- `src/main/java/com/seaman/constant/Routes.java`
- `src/main/java/com/seaman/controller/DocumentController.java`
- `src/main/java/com/seaman/service/DocumentService.java`
- `src/main/java/com/seaman/repository/DocumentRepository.java`
- `src/main/java/com/seaman/entity/DocumentRequestItemEntity.java`
- `src/main/java/com/seaman/model/response/DocumentRequestItemResponse.java`
- `documents/mvp1/script/RUN1_2026_create_table.sql`
- `documents/mvp1/script/RUN2_2026_insert_master.sql`
- `documents/mvp1/script/RUN3_create_index.sql`
- `documents/mvp1/script/RUN4_external_data`
