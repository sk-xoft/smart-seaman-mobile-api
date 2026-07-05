# Mobile Document Renewal API Tasks

อ้างอิง: `documents/mvp1/document_renewal_figma_spec.md` ส่วน `Mobile APIs`

ตรวจสถานะจาก source code ณ วันที่ 4 กรกฎาคม 2026 โดยถือว่า task เป็น **ทำแล้ว** เมื่อมี route, controller, service, repository/SQL mapping, request/response model, validation และ test ที่เกี่ยวข้องครบตาม acceptance criteria

## Status Legend

- [x] ทำแล้ว
- [~] ทำบางส่วน
- [ ] ยังไม่ได้ทำ
- [?] มี script/design แล้ว แต่ยังยืนยัน runtime/deployment ไม่ได้

## Documentation Rule

- เมื่อ API task ใด implement เสร็จและเปลี่ยน Status เป็น `[x]` ต้องเพิ่มตัวอย่าง cURL ที่เรียกใช้งานได้ไว้ใน section ของ task นั้นทุกครั้ง
- cURL ต้องใช้ `${base_url}` โดยไม่รวม `/v1`, ใช้ `${access_token}` สำหรับ JWT และแสดง required headers, query parameters หรือ request body ให้ครบ
- task ที่เป็น internal foundation และไม่มี public endpoint ต้องระบุว่าไม่มี cURL โดยตรง พร้อมอ้างอิง API ที่ใช้ foundation นั้น

## Current Status

| ลำดับ | Status | Method | API | หมายเหตุ |
|---:|:---:|---|---|---|
| 1 | [x] | GET | `/v1/document-renewals/statuses` | implement controller/service/repository/model และ mobile progress mapping แล้ว; focused tests ผ่าน |
| 2 | [x] | GET | `/v1/document-renewals/prices?documentCode={code}` | implement API, validation, BigDecimal, effective-date filtering และ overlapping-config guard แล้ว |
| 3 | [ ] | POST | `/v1/document-renewals` | ยังไม่มี controller/service/repository/model สำหรับสร้าง renewal request |
| 4 | [ ] | GET | `/v1/document-renewals/my?offSet={n}` | ยังไม่มี API รายการคำขอของ user |
| 5 | [ ] | GET | `/v1/document-renewals/{requestId}` | ยังไม่มี API รายละเอียดคำขอ |
| 6 | [ ] | GET | `/v1/document-renewals/{requestId}/timeline` | ยังไม่มี API timeline |
| 7 | [ ] | POST | `/v1/document-renewals/{requestId}/items/{itemId}/file` | ยังไม่มี API upload/replace file สำหรับ renewal item |
| 8 | [ ] | POST | `/v1/document-renewals/{requestId}/resubmit` | ยังไม่มี API resubmit และ state transition |
| 9 | [ ] | POST | `/v1/document-renewals/{requestId}/payments` | มี schema payment แล้ว แต่ยังไม่มี payment service/provider integration/API |
| 10 | [ ] | GET | `/v1/document-renewals/{requestId}/payments/{transactionId}` | ยังไม่มี API ตรวจ payment status |

สรุป Mobile APIs: **ทำแล้ว 2/10, ทำบางส่วน 0/10, ยังไม่ได้ทำ 8/10**

## Prerequisites And Supporting Work

| Status | งาน | หลักฐาน/หมายเหตุ |
|:---:|---|---|
| [x] | API ตรวจ required document ที่ขาด/ต้องแก้ไข | `GET /v1/documents/request-items/validate`; มี controller, service, repository query และ response model แล้ว |
| [?] | Schema สำหรับ renewal flow | มี DDL ใน `RUN1_2026_create_table.sql`; ไม่สามารถสรุปว่า deploy แล้วจาก source code |
| [?] | Status master seed | มี upsert 7 statuses ใน `RUN2_2026_insert_master.sql`; ไม่สามารถสรุปว่า run แล้วจาก source code |
| [~] | Required document master/setting | มี schema, seed ตัวอย่าง `DOC001` และ validation API แล้ว แต่ยังไม่ยืนยัน master จริงครบทุก `document_code` |
| [ ] | Renewal price master data | มี table แต่ยังไม่พบ seed/config ราคาที่ใช้งานจริง |
| [ ] | Delivery address decision | schema มี `m_delivery_address` แล้ว แต่ spec ยังต้องยืนยัน UX/API สำหรับเลือกหรือ snapshot ที่อยู่ |
| [~] | Create/update delivery address APIs | มี route/controller/service/repository/request/response model และ service tests แล้ว แต่ controller/repository tests และ OpenAPI examples ยังไม่ครบ acceptance criteria |
| [x] | Thailand address master APIs | implement route/controller/service/repository/response model แล้ว; focused tests ผ่าน 5/5 และ pin Lombok ให้รองรับ JDK 21 แล้ว |
| [ ] | Payment provider decision | ต้องยืนยัน channel, charge flow และ webhook ก่อนปิด payment tasks |
| [~] | Automated tests สำหรับ renewal flow | มี focused tests สำหรับ status/price/foundation 11 cases; flow อื่นยังไม่มี test |

## Task Breakdown

### MR-MOB-01: Shared Renewal Foundation

Status: [x] ทำแล้ว

- [x] เพิ่ม route constants กลุ่ม `/document-renewals` สำหรับ status และ price
- [x] เพิ่ม entity/DTO สำหรับ renewal request, status, price, request item, transaction, payment, department submission, delivery และ delivery address
- [x] เพิ่ม service/repository กลางสำหรับตรวจ ownership ด้วย `mobile_user_uuid` จาก authenticated request context
- [x] กำหนด enum ของ status/action โดยใช้ stable internal code และ English master name ไม่ผูก business logic กับชื่อภาษาไทย
- [x] lock owned request และกำหนด `@Transactional` boundary ครอบ status update กับ append timeline
- [x] ป้องกัน stale/concurrent transition ด้วย expected current status ใน update condition
- [x] เพิ่ม focused tests สำหรับ ownership, invalid request ID, invalid state และ status/timeline write

API coverage:

- MR-MOB-01 เป็น internal/shared foundation จึงไม่มี public API เฉพาะของตัวเอง
- foundation ถูกใช้งานโดย `GET /v1/document-renewals/statuses` (MR-MOB-02) และ
  `GET /v1/document-renewals/prices` (MR-MOB-03)
- ownership guard และ atomic status transition จะถูกเรียกจาก request detail, timeline,
  resubmit และ payment APIs เมื่อ MR-MOB-04 ถึง MR-MOB-11 implement
- ยังไม่มี endpoint สำหรับเรียก status transition โดยตรง และไม่ควร expose generic transition API ให้ mobile

ตัวอย่าง cURL สำหรับตรวจ foundation ผ่าน APIs ที่เปิดใช้งานแล้ว:

```bash
# Active renewal status master
curl --request GET \
  --url "${base_url}/v1/document-renewals/statuses" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"

# Active/effective renewal price
curl --request GET \
  --get \
  --url "${base_url}/v1/document-renewals/prices" \
  --data-urlencode "documentCode=DOC001" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

หมายเหตุ:

- `${base_url}` ไม่รวม `/v1` เช่น `https://api.example.com`
- `${access_token}` เป็น JWT ของ mobile user ที่ login แล้ว
- price API จะคืน `MA00016` เมื่อไม่มี active/effective price ของ `documentCode`
- ownership/transition foundation ยังไม่มี cURL จนกว่า endpoint ที่ใช้ request ownership จะ implement

Acceptance criteria:

- user อ่านหรือแก้ไขได้เฉพาะ request ของตัวเอง
- ทุก query ใช้ชื่อ table/column ตรงกับ RUN1
- การเปลี่ยนสถานะทุกครั้งเพิ่ม `m_document_transaction` ใน transaction เดียวกัน

### MR-MOB-02: Get Renewal Status Master

Status: [x] ทำแล้ว

API: `GET /v1/document-renewals/statuses`

- ดึงเฉพาะ `is_active = 'YES'`
- map `id`, `nameTh`, `nameEn`, `cssColor` และ mobile progress step
- กำหนดลำดับ 5 main steps และแยก correction/cancel ให้ตรง spec

Acceptance criteria:

- response ครบ 7 statuses จาก spec เมื่อ master data ครบ
- `รอผู้ยื่นแก้ไข` map เป็น step 1 และ `ยกเลิก` ไม่เป็น normal progress step

Implementation: map step ใน application จาก stable English status name; correction เป็น step 1 และ cancelled มี `progressStep = null`

ตัวอย่าง cURL:

```bash
curl --request GET \
  --url "${base_url}/v1/document-renewals/statuses" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

ตัวแปร:

- `${base_url}` ไม่รวม `/v1` เช่น `https://api.example.com`
- `${access_token}` เป็น JWT ของ mobile user ที่ login แล้ว

### MR-MOB-03: Get Renewal Price

Status: [x] ทำแล้ว

API: `GET /v1/document-renewals/prices?documentCode={code}`

- validate `documentCode`
- ดึง active/effective price ของเอกสาร
- ส่ง fee breakdown และยอดรวมด้วย `DECIMAL`/`BigDecimal`
- กำหนด behavior เมื่อไม่พบราคา หรือมีช่วง effective date ซ้อนกัน

Acceptance criteria:

- ไม่ใช้ floating-point กับจำนวนเงิน
- คืนราคาเฉพาะ document ที่เปิด renewal และราคาอยู่ในช่วงใช้งาน
- มี test กรณีพบราคา, ไม่พบราคา และ config ซ้ำ

Implementation status:

- [x] validate และ normalize `documentCode`
- [x] query เฉพาะ active price ของ active document
- [x] ใช้ `BigDecimal` และคืน fee breakdown/total
- [x] มี test กรณีพบราคา, ไม่พบราคา และ config ซ้ำ
- [x] เพิ่ม `effective_from`/`effective_to`, effective-period constraint และ lookup index ใน RUN1
- [x] เพิ่ม migration `RUN5_document_renewal_foundation.sql` สำหรับฐานข้อมูลเดิม
- [x] filter ช่วงวันที่แบบ inclusive และรองรับ `effective_to = NULL`
- [x] fail เมื่อพบ active/effective config ซ้อนกันมากกว่าหนึ่งรายการ

Operational prerequisite: ต้องกำหนด production price master data จริงก่อนเปิด renewal; ไม่ hard-code ราคาใน application หรือ seed ตัวอย่างเป็นราคาจริง

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

Status: [x] ทำแล้ว

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

Status: [~] ทำบางส่วน

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

### MR-MOB-15: Identity Document Multi-File Upload

Status: [ ] ยังไม่ได้ทำ

API:

| Method | API | Content-Type | Purpose |
|---|---|---|---|
| POST | `/v1/documents/request-items/{itemCode}/files` | `multipart/form-data` | upload หรือ replace ไฟล์ในแต่ละ slot ของ profile supporting document |

รองรับ `m_document_master_request_item` รหัส `MRI001` ซึ่งเป็น master item เดียวสำหรับ
`สำเนาบัตรประชาชน / Passport` แต่มีจำนวนไฟล์ที่ต้อง upload ต่างกัน:

| `documentType` | Required `slotCode` | จำนวนไฟล์ |
|---|---|---:|
| `ID_CARD` | `FRONT`, `BACK` | 2 |
| `PASSPORT` | `MAIN` | 1 |

Request multipart fields:

- `documentType`: required สำหรับ `MRI001`; รับเฉพาะ `ID_CARD` หรือ `PASSPORT`
- `slotCode`: `FRONT`/`BACK` สำหรับ `ID_CARD` และ `MAIN` สำหรับ `PASSPORT`
- `file`: binary file; รองรับ JPEG, PNG และ PDF ขนาดไม่เกิน 10 MB ต่อไฟล์

Schema changes:

- คง `MRI001` เป็นหนึ่ง row ใน `m_document_master_request_item`; ห้ามแตกเป็นสาม master items เพราะเป็น requirement ทางธุรกิจรายการเดียว
- เพิ่ม `m_document_master_request_item_slot` สำหรับกำหนด slot ที่รองรับ โดยมีอย่างน้อย:
  - `id`
  - `document_master_request_item_code`
  - `document_type`
  - `slot_code`
  - `slot_name_th`
  - `slot_name_en`
  - `sort_order`
  - `is_required`
  - `is_active`
- กำหนด unique key `(document_master_request_item_code, document_type, slot_code)`
- seed `MRI001/ID_CARD/FRONT`, `MRI001/ID_CARD/BACK` และ `MRI001/PASSPORT/MAIN`
- แยกไฟล์เป็น child table `m_document_profile_request_item_file` เพื่อให้หนึ่ง
  `m_document_profile_request_item` มีหลายไฟล์ โดยมีอย่างน้อย:
  - `id`
  - `profile_request_item_id`
  - `document_type`
  - `slot_code`
  - `storage_key`
  - `original_file_name`
  - `mime_type`
  - `file_size`
  - `file_uploaded_at`
  - `check_result`
  - `check_note`
  - `is_updated`
  - audit timestamps/users
- กำหนด unique key `(profile_request_item_id, document_type, slot_code)` และ foreign key ไป profile item/master slot ที่เกี่ยวข้อง
- document items ทั่วไปใช้ `document_type = 'GENERAL'` และ `slot_code = 'MAIN'` เพื่อให้ใช้ upload model เดียวกัน

Upload behavior:

- อ่าน `mobile_user_uuid` จาก JWT/request context เท่านั้น และหา/create profile item ด้วย user + `itemCode`
- validate ว่า `itemCode`, `documentType` และ `slotCode` เป็น active combination ใน master slot
- ตรวจ MIME จาก file signature; ห้ามเชื่อเฉพาะ `Content-Type` หรือ extension จาก client
- generate storage key ด้วย UUID ฝั่ง server; ห้ามนำ original filename มาใช้เป็น key โดยตรง
- upload object ใหม่ให้สำเร็จก่อน update metadata ใน DB
- replace slot เดิมด้วย transaction; หลัง commit จึงลบ object เก่า
- ถ้า object upload ล้มเหลว ห้ามแก้ DB; ถ้า DB update ล้มเหลวหลัง upload ต้องลบ orphan object ใหม่
- เมื่อ upload ชนิดใหม่ต่างจากชนิด active เดิม (`ID_CARD` ↔ `PASSPORT`) ให้ลบ metadata/files ของชนิดเดิมและเริ่ม validation ใหม่
- ห้ามคืน permanent public object-storage path; file view/download ต้องผ่าน authenticated API หรือ short-lived signed URL

Validation API changes:

- ปรับ `GET /v1/documents/request-items/validate?documentCode={code}` ให้คำนวณความครบจาก required slots
- `ID_CARD` ถือว่าครบเมื่อมีทั้ง `FRONT` และ `BACK`; `PASSPORT` ถือว่าครบเมื่อมี `MAIN`
- slot ที่ไม่มีไฟล์หรือมี `checkResult = fix` ถือว่ายังไม่ครบ
- response ของ item เพิ่ม `documentType` และ `files[]`; แต่ละ file คืน `fileId`, `slotCode`,
  `fileUploaded`, `fileUploadedAt`, `checkResult`, `checkNote`, `isUpdated`
- `fileUploaded` ระดับ item เป็น aggregate flag และเป็น `true` เมื่อ required slots ครบเท่านั้น

Migration and compatibility:

- migrate profile items อื่นที่มีไฟล์เดิมเป็น `GENERAL/MAIN` โดยคง storage key และ upload timestamp เดิม
- ไฟล์เดิมของ `MRI001` ไม่มีข้อมูลระบุว่าเป็นด้านใด จึงเก็บ legacy metadata ไว้เพื่อ audit แต่ไม่นับว่าครบ
- หลัง deploy ผู้ใช้ที่มี `MRI001` เดิมต้องเลือก `ID_CARD` หรือ `PASSPORT` และ upload ตาม required slots ใหม่
- deployment ต้องทำ schema/seed ก่อน application rollout; application version ใหม่ต้องไม่อ่าน legacy single-file columns เพื่อสรุปความครบของ `MRI001`

Acceptance criteria:

- upload `ID_CARD/FRONT` เพียงไฟล์เดียวยังคงคืน `BACK` เป็น missing
- upload `ID_CARD/FRONT` และ `ID_CARD/BACK` ครบแล้ว item ผ่าน file-completeness validation
- upload `PASSPORT/MAIN` แล้ว item ผ่าน file-completeness validation
- reject combination เช่น `ID_CARD/MAIN`, `PASSPORT/FRONT` และ document type ที่ไม่รองรับ
- reject empty file, MIME/signature ไม่ตรง allowlist และไฟล์เกิน 10 MB
- user ไม่สามารถอ่าน, replace หรือลบไฟล์ของ user อื่น
- replace ไฟล์ไม่ทำให้ DB ชี้ object ที่ upload ไม่สำเร็จ และไม่ทิ้ง orphan object เมื่อ DB ล้มเหลว
- การเปลี่ยน `ID_CARD`/`PASSPORT` ไม่นำไฟล์ชนิดเดิมมาคำนวณความครบ
- มี controller/service/repository tests สำหรับ happy paths, missing slot, invalid combination,
  ownership, replace, type switch, MIME/size validation, storage failure และ DB rollback
- อัปเดต OpenAPI multipart request/response examples และ validation errors

## Recommended Implementation Order

1. ปิด open decisions: request number, unpaid draft, address, upload format, payment channel
2. MR-MOB-01 shared foundation
3. MR-MOB-02 status และ MR-MOB-03 price
4. MR-MOB-15 identity document multi-file upload และปรับ profile document validation
5. MR-MOB-04 create request
6. MR-MOB-08 renewal item upload และ MR-MOB-09 resubmit โดย reuse file validation/storage component จาก MR-MOB-15
7. MR-MOB-05 list, MR-MOB-06 detail และ MR-MOB-07 timeline
8. MR-MOB-10 payment attempt, payment webhook และ MR-MOB-11 payment status
9. MR-MOB-13 Thailand address master APIs ก่อนเริ่ม delivery-address UI/API
10. MR-MOB-14 create/update delivery address APIs
11. MR-MOB-12 contract/integration/concurrency tests

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
- `documents/mvp1/script/RUN5_document_renewal_foundation.sql`
