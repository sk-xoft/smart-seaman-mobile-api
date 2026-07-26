# Mobile Document Renewal API Tasks

อ้างอิง:
- Requirement: `documents/mvp1/document_renewal_figma_spec.md`
- Design/API Spec: `documents/mvp1/document_service_flow.md`
- DB Script: `documents/mvp1/script/01_create_mvp1_tables.sql`, `documents/mvp1/script/02_seed_mvp1_master_data.sql`, `documents/mvp1/script/90_03_migrate_document_renewal_price_effective_period.sql`, `documents/mvp1/script/90_04_migrate_renewal_request_draft.sql`, `documents/mvp1/script/90_06_migrate_identity_document_multi_file.sql`, `documents/mvp1/script/90_09_migrate_omise_payment_channels.sql`, `documents/mvp1/script/90_11_migrate_document_request_collation.sql`, `documents/mvp1/script/90_12_migrate_document_request_user_contact_snapshot.sql`, `documents/mvp1/script/90_13_migrate_document_request_delivery_address_snapshot.sql`, `documents/mvp1/script/90_14_migrate_validate_create_performance_indexes.sql`, `documents/mvp1/script/90_15_migrate_document_request_idempotency_key.sql`

อัปเดตล่าสุด: 2026-07-25
ผู้รับผิดชอบ: Backend
สถานะรวม: In Progress
Progress: 22/22 tasks

หมายเหตุ: implementation task ทั้งหมดในเอกสารนี้อยู่สถานะ `[x]` จากหลักฐาน source/test ที่บันทึกไว้ แต่ยังมี operational remaining work เรื่อง production master data, deploy status ของ SQL scripts และ optional MySQL integration harness

## Status Legend

- [x] Done: ทำครบ + test ผ่าน + มีหลักฐาน
- [~] In Progress: ทำบางส่วน แต่ยังไม่ครบ acceptance criteria
- [ ] Todo: ยังไม่ได้เริ่ม
- [!] Blocked: ติด dependency / decision / environment
- [?] Unknown: มีข้อมูลบางส่วน แต่ยังยืนยันไม่ได้

## Documentation Rule

- เมื่อ API task ใดเปลี่ยนเป็น `[x]` ต้องมี cURL หรือ endpoint/test evidence ที่ตรวจซ้ำได้
- cURL ต้องใช้ `${base_url}` โดยไม่รวม `/v1`, ใช้ `${access_token}` สำหรับ JWT และแสดง required headers, query parameters หรือ request body ให้ครบ
- task ที่เป็น internal foundation และไม่มี public endpoint ต้องระบุว่าไม่มี cURL โดยตรง พร้อมอ้าง API หรือ test ที่พิสูจน์ behavior

## Current Status

| # | Status | Task | Owner | Evidence | Remaining |
|---:|:---:|---|---|---|---|
| 0 | [x] | Validate and create renewal request draft | Backend | `POST /v1/documents-renewals/requests/validate-and-create`, `DocumentServiceValidateRequestTest` | - |
| 1 | [x] | Shared renewal foundation | Backend | `DocumentRenewalFoundationRepositoryTest`, `m_document_transaction` transition contract | - |
| 2 | [x] | Get renewal status master | Backend | `GET /v1/document-renewals/statuses`, `DocumentRenewalControllerTest` | - |
| 3 | [x] | Get renewal price | Backend | `GET /v1/document-renewals/prices`, `DocumentRenewalServiceTest` | - |
| 4 | [x] | Create renewal request | Backend | `POST /v1/document-renewals`, `DocumentRenewalCreateServiceTest` | - |
| 5 | [x] | List my renewal requests | Backend | `GET /v1/document-renewals/my`, `DocumentRenewalListServiceTest` | - |
| 6 | [x] | Get renewal request detail | Backend | `GET /v1/document-renewals/{requestNo}`, `DocumentRenewalDetailServiceTest` | - |
| 7 | [x] | Get renewal timeline | Backend | `GET /v1/document-renewals/{requestNo}/timeline`, `DocumentRenewalTimelineServiceTest` | - |
| 8 | [x] | Upload or replace renewal supporting file | Backend | `POST /v1/document-renewals/{requestNo}/items/{documentRequestItemCode}/file`, `DocumentRenewalItemFileServiceTest` | - |
| 9 | [x] | Resubmit corrected renewal documents | Backend | `POST /v1/document-renewals/{requestNo}/resubmit`, `DocumentRenewalResubmitServiceTest` | - |
| 10 | [x] | Create payment attempt | Backend | `POST /v1/document-renewals/{requestId}/payments`, `DocumentRenewalPaymentServiceTest` | - |
| 11 | [x] | Get payment status and webhook transition | Backend | `GET /v1/document-renewals/{requestId}/payments/{transactionId}`, `OmiseWebhookServiceTest` | - |
| 12 | [x] | Mobile renewal API tests and contract | Backend/QA | `./mvnw test`, latest recorded result `Tests run: 99, Failures: 0, Errors: 0, Skipped: 0` | - |
| 13 | [x] | Thailand address master APIs | Backend | `GET /v1/master/provinces`, `MasterDataServiceAddressTest` | - |
| 14 | [x] | Create and update delivery address | Backend | `POST /v1/delivery-addresses`, `PUT /v1/delivery-addresses/{addressId}`, delivery address tests | - |
| 15 | [x] | Identity document multi-file upload | Backend | `POST /v1/documents/request-items/{itemCode}/files`, `90_06_migrate_identity_document_multi_file.sql` | - |
| 16 | [x] | Get default delivery address | Backend | `GET /v1/delivery-addresses`, `DeliveryAddressControllerTest` | - |
| 17 | [x] | Update mobile number with history | Backend | `POST /v1/profile-update`, `ProfileMobileNumberUpdateTest` | - |
| 18 | [x] | Snapshot delivery address per renewal request | Backend/DBA | `m_document_request_delivery_address`, `90_13_migrate_document_request_delivery_address_snapshot.sql`, `DocumentServiceValidateRequestTest`, `DocumentRenewalCreateServiceTest` | - |
| 19 | [x] | Update renewal request mobile number snapshot | Backend | `PUT /v1/documents-renewals/requests/{requestNo}/mobile`, `DocumentRenewalMobileServiceTest` | - |
| 20 | [x] | Idempotency for validate-and-create draft creation | Backend/DBA | `idempotency_key`, `90_15_migrate_document_request_idempotency_key.sql`, `DocumentServiceValidateRequestTest` | - |
| 21 | [x] | Close-to-expiration certification list renewal indicators | Backend | `GET /v1/documents/certification/to-expiration?offSet=0`, `DocumentControllerTest` | - |

## Supporting Status

| Status | Work | Owner | Evidence | Remaining |
|:---:|---|---|---|---|
| [?] | Schema สำหรับ renewal flow | DBA/Backend | `01_create_mvp1_tables.sql`, migration scripts under `documents/mvp1/script/` | ยืนยันว่า SQL scripts ถูก deploy ใน target environment แล้ว |
| [?] | Status master seed | DBA/Backend | `02_seed_mvp1_master_data.sql` มี renewal statuses | ยืนยันว่า seed ถูก run ใน target environment แล้ว |
| [~] | Required document master/setting | Product/Admin/DBA | schema และ seed ตัวอย่าง `DOC001`, validation API พร้อมใช้งาน | เพิ่ม/ยืนยัน master จริงให้ครบทุก `document_code` ที่เปิด renewal |
| [!] | Renewal price master data | Product/Admin/DBA | มี table และ price API แล้ว | Product/Admin ต้องยืนยันราคาจริงก่อนเพิ่ม production seed/config |
| [?] | Thailand address runtime data | DBA | `documents/mvp1/script/04_import_thailand_address_master.sql` | import tables/data ใน target database และยืนยัน row count |
| [~] | MySQL 8 disposable integration harness | Backend/QA | มี unit/contract/repository-SQL tests แล้ว | เพิ่ม Testcontainers task แยกถ้าต้องการ migration smoke test แบบ strict MySQL |

## Task Breakdown

### MR-MOB-21: Close-To-Expiration Certification List Renewal Indicators

Status: [x] Done
Owner: Backend
Estimate: 0.5 MD
Priority: High

Goal:
- เพิ่มข้อมูลสำหรับหน้าเอกสารใกล้หมดอายุ เพื่อให้ mobile แสดงได้ว่า certificate หมดอายุแล้วหรือยัง และ document code นั้นกำลังมีคำขอต่อเอกสารที่ยังดำเนินการอยู่หรือไม่

Scope:
- API `GET /v1/documents/certification/to-expiration?offSet={n}`
- ใช้ authenticated user จาก request context เท่านั้น
- คืนรายการ certificate ของ user ที่ `CERT_END_DATE <= NOW() + INTERVAL 18 MONTH`
- เพิ่ม field `documentRenewalFlag` จาก `m_documents.DOCUMENT_RENEWAL_FLAG`
- เพิ่ม field `certExpiredFlag` จากการคำนวณ `certEndDate` เทียบวันปัจจุบันตาม timezone `Asia/Bangkok`
- เพิ่ม field `documentRenewalProcessingFlag` จากการตรวจ `m_document_request`
- คง field `documentRenewalRequestFlag` ไว้เป็น alias ของ `documentRenewalProcessingFlag` เพื่อ backward compatibility

Out of scope:
- การสร้าง renewal request
- การ validate required renewal documents
- การเปลี่ยนสถานะ renewal request
- การกรองรายการด้วย `DOCUMENT_RENEWAL_FLAG`

Implementation checklist:
- [x] Controller / endpoint เดิม
- [x] Repository / SQL เพิ่ม renewal processing indicator
- [x] Response field เพิ่มใน `DocumentEntity`
- [x] Expired calculation ใน service
- [x] cURL example
- [x] Compile/test verification

Business logic:
- `offSet` เป็นตำแหน่งเริ่มต้นของ pagination แบบ 0-based
- API คืน `itemTotal`, `last` และ `items[]`
- `certExpiredFlag = "Y"` เมื่อ `certEndDate` น้อยกว่าวันปัจจุบันตาม timezone `Asia/Bangkok`
- ถ้า `certEndDate = "2027-12-31 00:00:00"` ระบบ parse เฉพาะ date part `2027-12-31` ก่อนคำนวณ
- `certExpiredFlag = "N"` เมื่อ certificate ยังไม่หมดอายุ หรือไม่มี `certEndDate`
- `documentRenewalProcessingFlag = "Y"` เมื่อพบ row ใน `m_document_request` ที่:
  - `mobile_user_uuid` ตรงกับ authenticated user
  - `document_code` ตรงกับ `m_documents.DOCUMENT_CODE`
  - `is_active = 'YES'`
  - status จาก `m_document_status.document_status_code` ไม่ใช่ `DELIVERED` และไม่ใช่ `CANCELLED`
- `documentRenewalProcessingFlag = "N"` เมื่อไม่พบคำขอต่อเอกสารที่ยังดำเนินการอยู่
- `documentRenewalRequestFlag` set ค่าเท่ากับ `documentRenewalProcessingFlag`
- API ไม่ filter ด้วย `DOCUMENT_RENEWAL_FLAG`; field นี้ใช้เพื่อให้ client ตัดสินใจแสดง action/CTA เอง

Response fields ที่เกี่ยวข้อง:
- `certStartDate`: วันที่เริ่มต้น certificate format `yyyy-MM-dd`
- `certEndDate`: วันที่หมดอายุ certificate format `yyyy-MM-dd`
- `disYear`, `disMonth`, `disDay`: จำนวนเวลาคงเหลือที่คำนวณจาก `certEndDate`
- `certExpiredFlag`: `"Y"`/`"N"`
- `documentRenewalFlag`: master flag จาก `m_documents`
- `documentRenewalProcessingFlag`: `"Y"`/`"N"` บอกว่ามี renewal request ที่ยังดำเนินการอยู่
- `documentRenewalRequestFlag`: alias ของ `documentRenewalProcessingFlag`

API example: `GET /v1/documents/certification/to-expiration?offSet=0`

```bash
curl --request GET \
  --url "${base_url}/v1/documents/certification/to-expiration?offSet=0" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

Example response:

```json
{
  "code": "0000",
  "description": "success",
  "data": {
    "itemTotal": 1,
    "last": true,
    "items": [
      {
        "documentCode": "DOC001",
        "documentNameTh": "เอกสารตัวอย่าง",
        "documentNameEn": "Sample Document",
        "certStartDate": "2024-01-01",
        "certEndDate": "2027-12-31",
        "disYear": "1",
        "disMonth": "5",
        "disDay": "5",
        "certExpiredFlag": "N",
        "documentRenewalFlag": "Y",
        "documentRenewalProcessingFlag": "Y",
        "documentRenewalRequestFlag": "Y"
      }
    ]
  }
}
```

### MR-MOB-00: Validate And Create Renewal Request Draft

Status: [x] Done
Owner: Backend
Estimate: 2 MD
Priority: High

Goal:
- ตรวจ required document items ของ mobile user และสร้าง renewal request draft สถานะ `PAYMENT_PENDING` ถ้ายังไม่มี active request ของ document นั้น

Scope:
- API `POST /v1/documents-renewals/requests/validate-and-create`
- รับ `documentCode` จาก request body และ normalize เป็น uppercase
- รับ optional `idempotencyKey` จาก request body เพื่อกัน client retry แล้วสร้าง request draft ซ้ำ
- ใช้ `mobile_user_uuid` จาก authenticated user เท่านั้น
- คืน `documentName` จาก `m_documents` ตามภาษา request (`Accept-Language`) โดย fallback เป็น `documentCode`
- ถ้ามี active renewal request ของ user/document ที่ยังไม่ delivered ให้คืน request เดิมและ request items เดิม โดยไม่สร้างซ้ำ
- ถ้ายังไม่มี active request ให้ validate required/profile document items จาก `m_document_setting_requires` และ `m_document_profile_request_item`
- ดึง price config ด้วย renewal price service และ snapshot `price_setting_id`/`amount`
- ถ้ามี default active delivery address ของ user ให้ snapshot ที่อยู่จัดส่งลง `m_document_request_delivery_address`; ถ้าไม่มีให้สร้าง request ต่อโดย `delivery_address_id = NULL`
- generate `request_no`, insert `m_document_request`, insert request items และ append timeline action `CREATE`

Out of scope:
- admin review flow
- payment charge creation
- admin approval workflow
- การสร้าง master document/price setting ใหม่

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Request/response DTO
- [x] Validation
- [x] Error handling
- [x] Transaction / concurrency handling
- [x] Swagger / API doc
- [x] Focused test
- [x] cURL example

Business logic:
- client ส่งแค่ `documentCode`; ห้ามส่ง `mobileUserUuid`, ราคา, status หรือ request number จาก client
- client สามารถส่ง optional `idempotencyKey`; ถ้า retry action เดิมต้องส่ง key เดิม
- server อ่าน authenticated user จาก `userObject` ใน request context
- server snapshot `mobile_number` และ `email` จาก authenticated user ลง `m_document_request`
- server หา default active delivery address ของ user; ถ้ามีให้ snapshot ลง `m_document_request_delivery_address`, ถ้าไม่มีให้ไม่สร้าง snapshot และไม่ error
- `documentCode` ถูก `trim()` และแปลงเป็น uppercase ก่อนใช้ query
- `documentName` lookup จาก `m_documents.DOCUMENT_NAME_TH` หรือ `DOCUMENT_NAME_EN` ตามภาษา request
- ระบบตรวจ active request เดิมด้วย `findLatestActiveRequestNotDelivered(mobileUserUuid, documentCode)`
- ถ้ามี `idempotencyKey` ระบบ lookup request เดิมของ user/key ก่อนสร้าง request ใหม่
- ถ้าเจอ `idempotencyKey` เดิมของ document เดิม ให้คืน request เดิมโดยไม่สร้างซ้ำ
- ถ้าเจอ `idempotencyKey` เดิมแต่คนละ `documentCode` ให้คืน validation error `idempotencyKey`
- ถ้าพบ active request เดิม:
  - ดึง request items ของ request เดิมด้วย owner scope
  - คืน `requestId`, `requestNo`, `documentCode`, `items`
  - ไม่เรียก price service, ไม่ insert request ใหม่ และไม่ append timeline ซ้ำ
- ถ้าไม่พบ active request เดิม:
  - query required/profile items ของ user/document
  - ถ้าไม่พบ document setting หรือ request items ให้คืน business error `DOCUMENT_SETTING_NOT_FOUND`
  - ดึงราคา renewal จาก active/effective price config
  - ดึง default active delivery address ของ user ถ้ามี
  - สร้าง request ใหม่เป็น unpaid draft status `PAYMENT_PENDING`
  - snapshot delivery address ลง `m_document_request_delivery_address` ใน transaction เดียวกับ request เฉพาะกรณีมี default address
  - สร้าง request items ตาม required setting
  - ถ้าจำนวน request items ที่ insert ไม่เท่ากับจำนวน items ที่ validate ได้ ให้ rollback ด้วย database error
  - append `m_document_transaction` action `CREATE` พร้อม note `Unpaid draft created`
  - fetch request items ที่เพิ่ง insert แล้วคืน response โดยใช้ rule แยกตาม `storageScope`
  - first create response ของ `storageScope = PROFILE` ต้องแสดง `documentStatus` จาก profile validation จริง ไม่ใช่ force เป็น `COMPLETE`
  - item ที่เป็น `storageScope = PROFILE` ต้องดึง `fileUploaded` และ file metadata จาก `m_document_profile_request_item`
  - item ที่เป็น `storageScope = PROFILE` ต้องไม่ใช้ `m_document_request_items.approve_status = PASS` มา override เป็น `COMPLETE` ถ้า profile ไม่มีไฟล์จริง
  - first create response ของ `storageScope = REQUEST` ต้องคง `documentStatus = MISSING` จนกว่า user จะ upload file สำหรับ renewal request นั้น เพราะเป็นเอกสารที่ต้องส่งใหม่ทุก request
  - existing active request response ของ `storageScope = REQUEST` ที่ upload แล้วต้องคืน `files[]` จาก `m_document_request_item_files`
- method เป็น `@Transactional` ดังนั้น request header, request items และ transaction timeline ต้อง commit/rollback พร้อมกัน

Acceptance criteria:
- request body รับเฉพาะ `documentCode` และ validate required/format/length
- user ไม่สามารถสร้าง request ให้ user อื่นได้
- active request เดิมต้องถูก reuse และไม่สร้าง duplicate draft
- retry ด้วย `idempotencyKey` เดิมต้องคืน request เดิมและไม่สร้าง duplicate draft
- `m_document_request` ต้องมี unique key `(mobile_user_uuid, idempotency_key)` เพื่อกัน concurrent duplicate
- new request ต้องอยู่สถานะ `PAYMENT_PENDING`
- amount และ `price_setting_id` ต้องมาจาก server-side price config
- `mobile_number` และ `email` ใน `m_document_request` ต้องมาจาก `m_mobile_users` ผ่าน authenticated user
- `validate-and-create` ต้องสร้าง renewal request draft ได้แม้ user ยังไม่มี default active delivery address
- ถ้ามี default address ข้อมูลที่อยู่จัดส่งของ renewal request ต้องถูก snapshot ลง `m_document_request_delivery_address` และไม่เปลี่ยนตาม `m_delivery_address` ภายหลัง
- `request_no` ต้องมาจาก server-side running number
- insert request/items/timeline ต้อง rollback พร้อมกันเมื่อขั้นตอนใดล้มเหลว
- first create response ต้องคืน `requestId`, `requestNo`, `documentCode`, `documentName` และ `items`
- response ต้องคืน `idempotencyKey` เมื่อ request ถูกสร้างหรือ reused จาก key นั้น
- response ต้องคืน `mobileNumber` และ `email` จาก snapshot/authenticated user
- response ต้องคืน `address` เป็น array ของ delivery address snapshot; ถ้าไม่มี address ให้คืน `[]`
- item ที่เป็น `storageScope = PROFILE` ต้องคืน `documentStatus` ตามไฟล์ profile จริง เช่น `COMPLETE`, `MISSING`, `NOT_UPLOADED`, `NEED_FIX` หรือ `INCOMPLETE`
- item ที่เป็น `storageScope = PROFILE` ต้องคืน `fileUploaded` จาก profile document ของ user
- item ที่เป็น `storageScope = PROFILE` และไม่มีข้อมูลใน `m_document_profile_request_item` ต้องไม่คืน `documentStatus = COMPLETE`
- item ที่เป็น `storageScope = REQUEST` เช่น `MRI004` ต้องคืน `documentStatus = MISSING` หลังสร้าง request ครั้งแรก
- item ที่เป็น `storageScope = REQUEST` และ `fileUploaded = 1` ต้องมี `files[]` พร้อม `filePath`, `originalFileName`, `mimeType`, `fileSize`, `fileUploadedAt`
- existing active request response ต้องคืน status จริงจาก request items เดิม

Evidence when done:
- API example: `POST /v1/documents-renewals/requests/validate-and-create`
- Test class: `DocumentServiceValidateRequestTest`
- Files changed: `Routes`, `DocumentController`, `DocumentService`, `DocumentRepository`, `DocumentRenewalCreateRepository`, `DocumentRequestValidateRequest`, `DocumentRequestValidateResponse`, `DocumentRequestItemResponse`
- SQL/index evidence: `03_create_core_indexes.sql`, `90_04_migrate_renewal_request_draft.sql`, `90_13_migrate_document_request_delivery_address_snapshot.sql`, `90_15_migrate_document_request_idempotency_key.sql`, unique key `(mobile_user_uuid, idempotency_key)`, unique key `(mobile_user_uuid, document_master_request_item_code, document_type, slot_code)`

Request body:

```json
{
  "documentCode": "DOC001",
  "idempotencyKey": "renewal-doc001-attempt-1"
}
```

Response fields:

```json
{
  "code": "MA00000",
  "description": "Success",
  "data": {
    "requestId": "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee",
    "requestNo": "260700001",
    "documentCode": "DOC001",
    "documentName": "ประกาศนียบัตรลูกเรือ",
    "mobileNumber": "0812345678",
    "email": "crew@example.com",
    "address": [
      {
        "id": "11111111-2222-3333-4444-555555555555",
        "firstName": "ศรัญญู",
        "lastName": "แก้วโสภา",
        "addressLine": "16 ม. 8",
        "province": "39",
        "district": "3902",
        "subDistrict": "390202",
        "postalCode": "39170",
        "mobileNumber": "0812345678"
      }
    ],
    "items": [
      {
        "documentMasterRequestItemCode": "MRI002",
        "storageScope": "PROFILE",
        "documentStatus": "COMPLETE",
        "fileUploaded": 1
      },
      {
        "documentMasterRequestItemCode": "MRI004",
        "storageScope": "REQUEST",
        "documentStatus": "MISSING",
        "fileUploaded": 0,
        "files": []
      }
    ]
  }
}
```

Reference SQL สำหรับ validation ภายใน:

```sql
SELECT
    dsr.document_code,
    dsr.document_master_request_item_code,
    dmri.document_master_items_name,
    dsr.sort_order,
    CASE
        WHEN dpri.id IS NULL THEN 'MISSING'
        WHEN dpri.file_uploaded = 0 THEN 'NOT_UPLOADED'
        WHEN dpri.check_result = 'fix' THEN 'NEED_FIX'
        ELSE 'OK'
    END AS document_status,
    dpri.id AS profile_request_item_id,
    COALESCE(dpri.file_uploaded, 0) AS file_uploaded,
    dpri.check_result,
    dpri.file_path
FROM m_document_setting_requires dsr
INNER JOIN m_document_master_request_item dmri
    ON dmri.document_master_items_code = dsr.document_master_request_item_code
LEFT JOIN m_document_profile_request_item dpri
    ON dpri.document_master_request_item_code = dsr.document_master_request_item_code
   AND dpri.mobile_user_uuid = :mobileUserUuid
WHERE dsr.document_code = :documentCode
  AND dsr.is_required = 1
  AND dsr.is_active = 'YES'
  AND dmri.is_active = 'YES'
ORDER BY dsr.sort_order ASC;
```

ตัวอย่าง cURL:

```bash
curl --request POST \
  --url "${base_url}/v1/documents-renewals/requests/validate-and-create" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Content-Type: application/json" \
  --header "Accept-Language: TH" \
  --data '{
    "documentCode": "DOC001"
  }'
```

### MR-MOB-01: Shared Renewal Foundation

Status: [x] Done
Owner: Backend
Estimate: 2 MD
Priority: High

Goal:
- สร้าง foundation กลางสำหรับ owner-scoped renewal request, status transition และ timeline

Scope:
- route constants กลุ่ม `/document-renewals`
- entity/DTO สำหรับ request, status, price, item, transaction, payment, department submission, delivery และ delivery address
- ownership guard ด้วย `mobile_user_uuid` จาก authenticated request context
- `@Transactional` boundary สำหรับ status update และ append timeline

Out of scope:
- generic transition API ให้ mobile เรียกโดยตรง

Implementation checklist:
- [x] Route constants
- [x] Entity/DTO
- [x] Service/repository ownership guard
- [x] Transaction / concurrency handling
- [x] Focused tests

Acceptance criteria:
- user อ่านหรือแก้ไขได้เฉพาะ request ของตัวเอง
- ทุก query ใช้ชื่อ table/column ตรงกับ RUN scripts
- การเปลี่ยนสถานะทุกครั้งเพิ่ม `m_document_transaction` ใน transaction เดียวกัน

Evidence when done:
- Test class: `DocumentRenewalFoundationRepositoryTest`
- API coverage: `GET /v1/document-renewals/statuses`, `GET /v1/document-renewals/prices`
- Files changed: `Routes`, renewal service/repository/model classes

### MR-MOB-02: Get Renewal Status Master

Status: [x] Done
Owner: Backend
Estimate: 0.5 MD
Priority: High

Goal:
- ให้ mobile ดึง renewal status master และ progress mapping ได้

Scope:
- `GET /v1/document-renewals/statuses`
- ดึงเฉพาะ `is_active = 'YES'`
- map `id`, `nameTh`, `nameEn`, `cssColor` และ mobile progress step

Out of scope:
- admin status management API

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Response DTO
- [x] Focused test
- [x] cURL example

Acceptance criteria:
- response ครบ 7 visible statuses เมื่อ master data ครบ
- `รอผู้ยื่นแก้ไข` map เป็น step 1
- `ยกเลิก` ไม่เป็น normal progress step

Evidence when done:
- Test class: `DocumentRenewalControllerTest`
- API example: `GET /v1/document-renewals/statuses`

```bash
curl --request GET \
  --url "${base_url}/v1/document-renewals/statuses" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

### MR-MOB-03: Get Renewal Price

Status: [x] Done
Owner: Backend
Estimate: 1 MD
Priority: High

Goal:
- ให้ mobile ตรวจราคา renewal จาก active/effective server-side config

Scope:
- `GET /v1/document-renewals/prices?documentCode={code}`
- validate และ normalize `documentCode`
- ใช้ `BigDecimal` สำหรับ fee breakdown และ total
- guard overlapping active/effective price config

Out of scope:
- production price approval

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Request/response DTO
- [x] Validation
- [x] Error handling
- [x] Focused test
- [x] cURL example

Acceptance criteria:
- ไม่ใช้ floating-point กับจำนวนเงิน
- คืนราคาเฉพาะ document ที่เปิด renewal และราคาอยู่ในช่วงใช้งาน
- ไม่พบราคาให้คืน `MA00016`
- active/effective config ซ้อนกันต้อง fail

Evidence when done:
- Test class: `DocumentRenewalServiceTest`
- DB script: `90_03_migrate_document_renewal_price_effective_period.sql`
- API example: `GET /v1/document-renewals/prices`

```bash
curl --request GET \
  --get \
  --url "${base_url}/v1/document-renewals/prices" \
  --data-urlencode "documentCode=DOC001" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

### MR-MOB-04: Create Renewal Request

Status: [x] Done
Owner: Backend
Estimate: 3 MD
Priority: High

Goal:
- mobile user สร้าง unpaid renewal draft ได้ โดย server เป็นผู้กำหนด owner, price, request number และ initial status

Scope:
- `POST /v1/document-renewals`
- validate document เปิด renewal, required profile documents และ delivery address ownership
- snapshot `price_setting_id` และ `amount`
- snapshot delivery address ที่เลือกลง `m_document_request_delivery_address`
- generate `request_no` แบบ concurrency-safe
- สร้าง request header, request items และ `CREATE` timeline ใน transaction เดียวกัน

Out of scope:
- payment webhook success transition
- admin approval workflow

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Request/response DTO
- [x] Validation
- [x] Error handling
- [x] Transaction / concurrency handling
- [x] Swagger / API doc
- [x] Focused test
- [x] cURL example

Acceptance criteria:
- ไม่เชื่อถือ `mobileUserUuid`, ราคา หรือ status จาก request body
- delivery address ต้องเป็น active address ของ authenticated user และถูก snapshot ต่อ request
- `request_no` unique แม้สร้างพร้อมกัน
- ไม่เข้า `รอตรวจเอกสาร` ก่อน payment success
- rollback ทั้ง request/items/transaction เมื่อขั้นตอนใดล้มเหลว

Evidence when done:
- Test class: `DocumentRenewalCreateServiceTest`
- DB script: `90_04_migrate_renewal_request_draft.sql`
- DB script: `90_13_migrate_document_request_delivery_address_snapshot.sql`
- API example: `POST /v1/document-renewals`

```bash
curl --request POST \
  --url "${base_url}/v1/document-renewals" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Content-Type: application/json" \
  --header "Accept-Language: TH" \
  --data '{
    "documentCode": "DOC001",
    "deliveryAddressId": "11111111-2222-3333-4444-555555555555"
  }'
```

### MR-MOB-18: Snapshot Delivery Address Per Renewal Request

Status: [x] Done
Owner: Backend/DBA
Estimate: 1 MD
Priority: High

Goal:
- renewal request เก็บที่อยู่จัดส่ง ณ เวลาสร้าง request แยกจาก `m_delivery_address` ปัจจุบันของ user

Scope:
- เพิ่ม `m_document_request_delivery_address` แบบ one-to-one กับ `m_document_request`
- `POST /v1/document-renewals` snapshot address จาก `deliveryAddressId` ที่ validate ownership แล้ว
- `POST /v1/documents-renewals/requests/validate-and-create` snapshot default active delivery address ของ user เฉพาะกรณีมี default address
- migration backfill request เดิมที่มี `delivery_address_id`

Out of scope:
- เปลี่ยน response delivery detail
- ลบ `m_document_request.delivery_address_id`

Implementation checklist:
- [x] Repository / SQL
- [x] Service logic
- [x] Transaction handling
- [x] Focused test
- [x] DB script

Acceptance criteria:
- request ใหม่ที่มี default/selected address ต้องมี snapshot row หนึ่ง row ใน `m_document_request_delivery_address`
- ถ้า user แก้ `m_delivery_address` ภายหลัง snapshot ของ request เดิมต้องไม่เปลี่ยน
- `validate-and-create` ต้องไม่ error เมื่อ user ไม่มี default active address และต้องไม่ insert snapshot row
- existing active request ต้องไม่สร้าง request หรือ snapshot ซ้ำ

Evidence when done:
- DB script: `90_13_migrate_document_request_delivery_address_snapshot.sql`
- Test classes: `DocumentServiceValidateRequestTest`, `DocumentRenewalCreateServiceTest`, `DocumentRenewalCreateRepositoryTest`
- Files changed: `DocumentService`, `DocumentRenewalCreateService`, `DocumentRenewalCreateRepository`, `01_create_mvp1_tables.sql`

### MR-MOB-05: List My Renewal Requests

Status: [x] Done
Owner: Backend
Estimate: 1 MD
Priority: High

Goal:
- mobile user เห็นรายการ renewal requests ของตัวเองพร้อม pagination และ status summary

Scope:
- `GET /v1/document-renewals/my?offSet={n}`
- owner-scoped query จาก JWT
- page size 10, sort ล่าสุดก่อน, map `itemTotal`, `isLast`, amount, status และ `isResubmit`

Out of scope:
- admin search/filter

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Response DTO
- [x] Validation
- [x] Focused test
- [x] cURL example

Acceptance criteria:
- response มี `itemTotal`, `isLast` และ items
- วันที่แสดงเป็น `DD/MM/YYYY HH:mm` ใน timezone `Asia/Bangkok`
- ไม่คืน request ของ user อื่น

Evidence when done:
- Test class: `DocumentRenewalListServiceTest`
- API example: `GET /v1/document-renewals/my`

```bash
curl --request GET \
  --get \
  --url "${base_url}/v1/document-renewals/my" \
  --data-urlencode "offSet=0" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

### MR-MOB-06: Get Renewal Request Detail

Status: [x] Done
Owner: Backend
Estimate: 2 MD
Priority: High

Goal:
- mobile user ดูรายละเอียด request ของตัวเองตาม current status ได้โดยไม่เห็นข้อมูลภายในหรือ future status detail

Scope:
- `GET /v1/document-renewals/{requestNo}`
- owner-scoped detail, supporting items, correction state, department submission, delivery และ signed file URL

Out of scope:
- admin review detail

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Response DTO
- [x] Validation
- [x] Error handling
- [x] Focused test
- [x] cURL example

Acceptance criteria:
- mobile เห็นเฉพาะรายละเอียดของ current status
- rejected item แสดง `checkNote` ชัดเจน
- delivery data แสดงเฉพาะ status ที่เกี่ยวกับการจัดส่ง
- ไม่คืน object-storage key ถาวร

Evidence when done:
- Test class: `DocumentRenewalDetailServiceTest`
- API example: `GET /v1/document-renewals/{requestNo}`

```bash
curl --request GET \
  --url "${base_url}/v1/document-renewals/${request_no}" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

### MR-MOB-07: Get Renewal Timeline

Status: [x] Done
Owner: Backend
Estimate: 1 MD
Priority: Medium

Goal:
- mobile user เห็น timeline ของ request ตัวเองจาก append-only transaction log

Scope:
- `GET /v1/document-renewals/{requestNo}/timeline`
- read `m_document_transaction`
- map action/from/to status, timestamp และ controlled display detail

Out of scope:
- internal/admin note exposure

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Response DTO
- [x] Focused test
- [x] cURL example

Acceptance criteria:
- เรียง `actioned_at ASC, id ASC` อย่าง deterministic
- วันที่แสดงเป็น `DD/MM/YYYY HH:mm`
- ไม่ expose `note` หรือ `actionedBy` ที่เป็น internal/admin detail

Evidence when done:
- Test class: `DocumentRenewalTimelineServiceTest`
- API example: `GET /v1/document-renewals/{requestNo}/timeline`

```bash
curl --request GET \
  --url "${base_url}/v1/document-renewals/${request_no}/timeline" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

### MR-MOB-08: Upload Or Replace Renewal Supporting File

Status: [x] Done
Owner: Backend
Estimate: 2 MD
Priority: High

Goal:
- mobile user upload หรือแก้ไฟล์ supporting document ของ renewal request ได้อย่างปลอดภัย

Scope:
- `POST /v1/document-renewals/{requestNo}/items/{documentRequestItemCode}/file`
- multipart fields `documentType`, `slotCode`, `file`
- อนุญาต request status `Payment Pending` สำหรับ unpaid draft upload
- อนุญาต request status `Pending Applicant Correction` เฉพาะ item status `FIX`
- lookup `storage_scope` จาก `m_document_master_request_item`
- ถ้า `storage_scope = REQUEST` ให้ insert/update `m_document_request_item_files`
- ถ้า `storage_scope = PROFILE` ให้ insert/update `m_document_profile_request_item`
- reuse MIME/size validation, UUID storage key และ transaction-aware object cleanup

Out of scope:
- storage scope อื่นนอกเหนือจาก `REQUEST` และ `PROFILE`

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Validation
- [x] Error handling
- [x] Transaction / concurrency handling
- [x] Focused test
- [x] cURL example

Acceptance criteria:
- user upload file ได้เฉพาะ state ที่อนุญาต
- response ต้องคืน `requestId` และ `requestNo` ของ renewal request ที่ upload
- `PAYMENT_PENDING` ต้อง upload ได้ทั้ง `REQUEST` และ `PROFILE` ตาม `storage_scope`
- `PENDING_APPLICANT_CORRECTION` ต้อง upload ได้เฉพาะ item ที่ `approve_status = FIX`
- item `storage_scope = REQUEST` เช่น `MRI004` ต้องบันทึกไฟล์ลง `m_document_request_item_files`
- item `storage_scope = PROFILE` เช่น `MRI001` ต้องบันทึกไฟล์ลง `m_document_profile_request_item`
- DB ไม่ชี้ไฟล์ใหม่หาก upload ล้มเหลว
- DB rollback หลัง upload ต้อง cleanup orphan object
- ไม่ใช้ชื่อไฟล์จาก client เป็น storage key โดยตรง

Evidence when done:
- Test class: `DocumentRenewalItemFileServiceTest`
- API example: `POST /v1/document-renewals/{requestNo}/items/{documentRequestItemCode}/file`

```bash
curl --request POST \
  --url "${base_url}/v1/document-renewals/${request_no}/items/${document_request_item_code}/file" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH" \
  --form "documentType=GENERAL" \
  --form "slotCode=MAIN" \
  --form "file=@${file_path};type=application/pdf"
```

### MR-MOB-09: Resubmit Corrected Documents

Status: [x] Done
Owner: Backend
Estimate: 2 MD
Priority: High

Goal:
- mobile user ส่งเอกสารที่แก้ครบแล้วกลับเข้าสู่สถานะรอตรวจเอกสาร

Scope:
- `POST /v1/document-renewals/{requestNo}/resubmit`
- lock request row, validate corrected slots, reset item state, update status และ append `RESUBMIT` timeline
- publish in-app/FCM notification หลัง commit

Out of scope:
- admin approve/reject logic

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Validation
- [x] Error handling
- [x] Transaction / concurrency handling
- [x] Focused test
- [x] cURL example

Acceptance criteria:
- อนุญาตเฉพาะ `Pending Applicant Correction`
- concurrent/double submit ไม่สร้าง transition ซ้ำ
- status update และ timeline เป็น atomic transaction
- item ที่แก้แล้วแสดง `is_updated = 1`

Evidence when done:
- Test class: `DocumentRenewalResubmitServiceTest`
- API example: `POST /v1/document-renewals/{requestNo}/resubmit`

```bash
curl --request POST \
  --url "${base_url}/v1/document-renewals/${request_no}/resubmit" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

### MR-MOB-10: Create Payment Attempt

Status: [x] Done
Owner: Backend
Estimate: 3 MD
Priority: High

Goal:
- mobile user สร้าง Omise payment attempt สำหรับ renewal request ที่ยังรอชำระได้

Scope:
- `POST /v1/document-renewals/{requestId}/payments`
- ownership guard และ status guard `PAYMENT_PENDING`
- สร้าง `m_payment_transaction`
- Omise PromptPay QR และ Mobile Banking app redirect
- idempotency ด้วย client `idempotencyKey`

Out of scope:
- เปลี่ยน request status จาก client callback โดยตรง

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Request/response DTO
- [x] Validation
- [x] Error handling
- [x] External integration config
- [x] Focused test
- [x] cURL example

Acceptance criteria:
- amount มาจาก server-side snapshot `m_document_request.amount`
- retry ด้วย `idempotencyKey` เดิมไม่สร้าง charge ซ้ำ
- ไม่ log credential หรือข้อมูล payment sensitive
- success จาก client callback อย่างเดียวไม่เปลี่ยน request status

Evidence when done:
- Test class: `DocumentRenewalPaymentServiceTest`
- DB script: `90_09_migrate_omise_payment_channels.sql`
- External docs: `https://docs.omise.co/promptpay`, `https://docs.omise.co/mobile-banking-kbank`, `https://docs.omise.co/api-webhooks`

```bash
curl --request POST \
  --url "${base_url}/v1/document-renewals/${request_id}/payments" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Content-Type: application/json" \
  --header "Accept-Language: TH" \
  --data '{
    "paymentMethod": "promptpay",
    "idempotencyKey": "renewal-260700001-attempt-1"
  }'
```

```bash
curl --request POST \
  --url "${base_url}/v1/document-renewals/${request_id}/payments" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Content-Type: application/json" \
  --header "Accept-Language: TH" \
  --data '{
    "paymentMethod": "mobile_banking_kbank",
    "platformType": "IOS",
    "returnUri": "smartseaman://document-renewals/payment-return",
    "idempotencyKey": "renewal-260700001-kbank-1"
  }'
```

### MR-MOB-11: Get Payment Status And Webhook Transition

Status: [x] Done
Owner: Backend
Estimate: 2 MD
Priority: High

Goal:
- mobile ตรวจ payment status ได้ และระบบใช้ Omise webhook เป็น source of truth สำหรับ success transition

Scope:
- `GET /v1/document-renewals/{requestId}/payments/{transactionId}`
- public webhook `POST /v1/payments/omise/webhook`
- verify signature เมื่อมี `OMISE_WEBHOOK_SECRET`
- retrieve charge ก่อน update DB
- success transition จาก `PAYMENT_PENDING` เป็น `PENDING_DOCUMENT_REVIEW`

Out of scope:
- client-side payment success status mutation

Implementation checklist:
- [x] Controller / endpoint
- [x] Webhook endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Validation
- [x] Error handling
- [x] Transaction / concurrency handling
- [x] Focused test
- [x] cURL example

Acceptance criteria:
- ไม่ expose raw provider payload หรือข้อมูล sensitive
- payment success update request ได้เพียงครั้งเดียวด้วย row lock + current status guard
- append `PAYMENT_SUCCESS` timeline และส่ง notification หลัง commit
- failed/expired charge update เฉพาะ payment transaction

Evidence when done:
- Test class: `OmiseWebhookServiceTest`
- API example: `GET /v1/document-renewals/{requestId}/payments/{transactionId}`
- Config keys: `omise.api-url`, `omise.secret-key`, `omise.webhook-secret`

```bash
curl --request GET \
  --url "${base_url}/v1/document-renewals/${request_id}/payments/${transaction_id}" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

### MR-MOB-12: Mobile Renewal API Tests And Contract

Status: [x] Done
Owner: Backend/QA
Estimate: 2 MD
Priority: High

Goal:
- มี focused tests ครอบคลุม renewal flow, owner scope, validation, transition, payment idempotency และ API contract สำคัญ

Scope:
- controller contract tests สำหรับ 10 mobile renewal endpoints
- service tests สำหรับ ownership, validation และ state transition
- repository SQL contract tests สำหรับ owner scope, idempotency และ row lock queries
- concurrency/idempotency guard tests สำหรับ request number, resubmit และ payment success webhook duplicate
- request validation tests สำหรับ payment provider contract

Out of scope:
- Testcontainers/MySQL disposable schema harness

Implementation checklist:
- [x] Controller tests
- [x] Service tests
- [x] Repository SQL contract tests
- [x] Validation tests
- [x] Date/money contract tests
- [x] cURL examples in task doc

Acceptance criteria:
- ครอบคลุม happy path, invalid state, forbidden ownership, not found และ duplicate callback/retry
- ทุก status change มี timeline row ผ่าน `appendTransaction` verification
- date format และ money precision ถูก pin ด้วย test

Evidence when done:
- Verification command: `./mvnw test`
- Latest recorded result: `Tests run: 99, Failures: 0, Errors: 0, Skipped: 0`
- Test classes: `DocumentRenewalControllerTest`, `DocumentRenewalServiceTest`, `DocumentRenewalCreateServiceTest`, `DocumentRenewalListServiceTest`, `DocumentRenewalDetailServiceTest`, `DocumentRenewalTimelineServiceTest`, `DocumentRenewalItemFileServiceTest`, `DocumentRenewalResubmitServiceTest`, `DocumentRenewalPaymentServiceTest`, `OmiseWebhookServiceTest`, `DocumentRenewalPaymentRequestValidationTest`

### MR-MOB-13: Thailand Address Master APIs

Status: [x] Done
Owner: Backend
Estimate: 2 MD
Priority: High

Goal:
- ให้ mobile ดึง province/district/subdistrict สำหรับ delivery address validation ได้

Scope:
- `GET /v1/master/provinces`
- `GET /v1/master/districts?provinceCode={provinceCode}`
- `GET /v1/master/subdistricts?districtCode={districtCode}`
- source data จาก `documents/mvp1/script/04_import_thailand_address_master.sql`
- localized `name`, `nameTh`, `nameEn` และ `postalCode` สำหรับ subdistrict

Out of scope:
- admin CRUD สำหรับ master address

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Entity/response DTO
- [x] Validation
- [x] Error handling
- [x] Focused test
- [x] cURL/endpoint evidence

Acceptance criteria:
- province API คืนจังหวัดที่ active/ใช้งานได้ทั้งหมดโดยไม่ซ้ำ
- district/subdistrict filter ด้วย parent code
- query ใช้ index บน parent code
- response อยู่ภายใต้ `SuccessResponse`
- endpoints เป็น authenticated ตาม security convention ปัจจุบัน

Evidence when done:
- Test command: `./mvnw test -Dtest=MasterDataServiceAddressTest`
- Latest recorded focused result: 5/5 tests passed
- API examples: `GET /v1/master/provinces`, `GET /v1/master/districts`, `GET /v1/master/subdistricts`

### MR-MOB-14: Create And Update Delivery Address

Status: [x] Done
Owner: Backend
Estimate: 3 MD
Priority: High

Goal:
- mobile user สร้างและแก้ไขที่อยู่จัดส่งของตัวเองได้ โดยรักษา default address ให้มีได้ไม่เกินหนึ่งรายการ

Scope:
- `POST /v1/delivery-addresses`
- `PUT /v1/delivery-addresses/{addressId}`
- validate required fields, length, postal code และ address hierarchy
- lock user row and active address rows สำหรับ default mutation
- generated column/unique key `uq_delivery_address_active_default`

Out of scope:
- delete delivery address

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Request/response DTO
- [x] Validation
- [x] Error handling
- [x] Transaction / concurrency handling
- [x] Swagger / API doc
- [x] Focused test
- [x] cURL example

Acceptance criteria:
- user ไม่สามารถสร้างหรือแก้ไข address ให้ user อื่น
- address hierarchy ต้องสัมพันธ์กันตาม master data
- active default address มีได้ไม่เกินหนึ่งรายการต่อ user
- default replacement ต้อง atomic
- update address ที่ไม่มีอยู่, เป็นของ user อื่น หรือ inactive ต้องไม่แก้ไขข้อมูล

Evidence when done:
- DB script: `90_05_migrate_delivery_address_default_guard.sql`
- Test evidence: delivery address controller/service/repository validation tests
- API examples: `POST /v1/delivery-addresses`, `PUT /v1/delivery-addresses/{addressId}`

```bash
curl --request POST \
  --url "${base_url}/v1/delivery-addresses" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Content-Type: application/json" \
  --header "Accept-Language: TH" \
  --data '{
    "firstName": "ศรัญญู",
    "lastName": "แก้วโสภา",
    "addressLine": "16 ม. 8",
    "province": "39",
    "district": "3902",
    "subDistrict": "390202",
    "postalCode": "39170",
    "isDefault": true
  }'
```

```bash
curl --request PUT \
  --url "${base_url}/v1/delivery-addresses/${address_id}" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Content-Type: application/json" \
  --header "Accept-Language: TH" \
  --data '{
    "firstName": "ศรัญญู",
    "lastName": "แก้วโสภา",
    "addressLine": "99 ม. 1",
    "province": "39",
    "district": "3902",
    "subDistrict": "390202",
    "postalCode": "39170",
    "isDefault": true
  }'
```

### MR-MOB-15: Identity Document Multi-File Upload

Status: [x] Done
Owner: Backend
Estimate: 4 MD
Priority: High

Goal:
- รองรับ `MRI001` เป็น business requirement รายการเดียว แต่ upload ได้ตาม slot rules ของ ID card/passport

Scope:
- `POST /v1/documents/request-items/{itemCode}/files`
- `ID_CARD`: required `FRONT`, `BACK`
- `PASSPORT`: required `MAIN`
- document ทั่วไป: `GENERAL`, `MAIN`
- validate MIME signature, size <= 10 MB, ownership และ valid slot combination
- update validation API ให้คำนวณ required slots และคืน `documentType`/`files[]`

Out of scope:
- แยก `MRI001` เป็นหลาย master items

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Request/response DTO
- [x] Validation
- [x] Error handling
- [x] Transaction / storage cleanup
- [x] Swagger / API doc
- [x] Focused test
- [x] cURL example

Acceptance criteria:
- `ID_CARD/FRONT` อย่างเดียวต้องยัง missing `BACK`
- `ID_CARD/FRONT+BACK` หรือ `PASSPORT/MAIN` ครบแล้วผ่าน validation
- reject invalid combination เช่น `ID_CARD/MAIN`, `PASSPORT/FRONT`
- reject empty file, MIME/signature ไม่ตรง allowlist และไฟล์เกิน 10 MB
- replace/type switch ไม่ทำให้ DB ชี้ object ที่ upload ไม่สำเร็จหรือทิ้ง orphan object

Evidence when done:
- DB script: `90_06_migrate_identity_document_multi_file.sql`
- API examples: `POST /v1/documents/request-items/MRI001/files`, `GET /v1/documents/request-items/validate`
- Latest recorded focused result: 6/6 cases passed

```bash
curl --request POST \
  --url "${base_url}/v1/documents/request-items/MRI001/files" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH" \
  --form "documentType=ID_CARD" \
  --form "slotCode=FRONT" \
  --form "file=@${file_path};type=image/jpeg"
```

```bash
curl --request POST \
  --url "${base_url}/v1/documents/request-items/MRI001/files" \
  --header "Authorization: Bearer ${access_token}" \
  --form "documentType=PASSPORT" \
  --form "slotCode=MAIN" \
  --form "file=@${file_path};type=application/pdf"
```

### MR-MOB-16: Get Default Delivery Address

Status: [x] Done
Owner: Backend
Estimate: 1 MD
Priority: High

Goal:
- mobile user ดึง active default delivery address ของตัวเองได้

Scope:
- `GET /v1/delivery-addresses`
- query ด้วย `mobile_user_uuid`, `is_default = 1`, `is_active = 'YES'`
- not found คืน `MA00016`
- duplicate active default คืน `MA00012`

Out of scope:
- list all delivery addresses

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Response DTO
- [x] Error handling
- [x] Focused test
- [x] cURL example

Acceptance criteria:
- คืนเฉพาะ default address ของ user จาก JWT
- ไม่คืน inactive address
- user ไม่สามารถส่งหรือแก้ `mobile_user_uuid` เพื่ออ่าน address ของ user อื่น
- duplicate default ต้องไม่เลือก row แรกเงียบ ๆ

Evidence when done:
- Test classes: `DeliveryAddressControllerTest`, `DeliveryAddressServiceTest`, `DeliveryAddressRepositoryTest`
- Latest recorded focused result: 10/10 tests passed
- API example: `GET /v1/delivery-addresses`

```bash
curl --request GET \
  --url "${base_url}/v1/delivery-addresses" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Accept-Language: TH"
```

### MR-MOB-17: Update Mobile Number With History

Status: [x] Done
Owner: Backend
Estimate: 2 MD
Priority: Medium

Goal:
- profile update เก็บประวัติการเปลี่ยน `MOBILE_NUMBER` แบบ append-only และ atomic

Scope:
- reuse `POST /v1/profile-update`
- validate หมายเลขโทรศัพท์ไทย 10 หลัก
- lock ค่าเดิมด้วย `SELECT MOBILE_NUMBER ... FOR UPDATE`
- insert `m_mobile_number_history` เฉพาะเมื่อค่าเปลี่ยน
- update profile และ history ใน transaction เดียวกัน

Out of scope:
- unique constraint สำหรับเบอร์โทร

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Validation
- [x] Error handling
- [x] Transaction / concurrency handling
- [x] Swagger / API doc
- [x] Focused test
- [x] cURL example

Acceptance criteria:
- เปลี่ยนเบอร์แล้ว user row มีค่าใหม่และ history มี old/new ถูกต้อง
- เปลี่ยนจาก `NULL` เป็นเบอร์ใหม่ได้
- ส่งเบอร์เดิมซ้ำไม่สร้าง history ซ้ำ
- reject blank, non-digit หรือความยาวไม่ครบ 10 หลัก โดยไม่มี partial write
- concurrent updates ของ user เดียวกันถูก serialize ด้วย `FOR UPDATE`

Evidence when done:
- DB script: `90_08_migrate_mobile_number_history.sql`
- Test classes: `ProfileMobileNumberUpdateTest`, `ProfileMobileNumberValidationTest`
- Latest recorded focused result: 5/5 cases passed
- API example: `POST /v1/profile-update`

```bash
curl --request POST \
  --url "${base_url}/v1/profile-update" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Content-Type: application/json" \
  --header "Accept-Language: TH" \
  --data '{
    "firstName": "ศรัญญู",
    "lastName": "แก้วโสภา",
    "companyCode": "COMP001",
    "dateOfBirth": "1990-01-01",
    "positionCode": "POS001",
    "email": "user@example.com",
    "mobileNumber": "0812345678",
    "isChangeFile": "N"
  }'
```

### MR-MOB-19: Update Renewal Request Mobile Number Snapshot

Status: [x] Done
Owner: Backend
Estimate: 0.5 MD
Priority: High

Goal:
- ให้ mobile app แก้เบอร์มือถือสำหรับ renewal request ที่ถูก snapshot ไว้แล้ว โดยไม่แก้ profile หลักของ user

Scope:
- API `PUT /v1/documents-renewals/requests/{requestNo}/mobile`
- รับ `mobileNumber` จาก request body
- ใช้ authenticated user จาก request context เพื่อ scope ownership
- lock renewal request ด้วย `requestNo` + `mobile_user_uuid` และ `is_active = 'YES'`
- update `m_document_request.mobile_number`
- update `m_document_request_delivery_address.mobile_number` ของ request เดียวกัน

Out of scope:
- เปลี่ยน `m_mobile_users.MOBILE_NUMBER`
- insert `m_mobile_number_history`
- เปลี่ยนข้อมูลที่อยู่ เช่น province/district/subDistrict/postalCode
- admin update flow

Implementation checklist:
- [x] Controller / endpoint
- [x] Service logic
- [x] Repository / SQL
- [x] Request/response DTO
- [x] Validation
- [x] Error handling
- [x] Transaction / concurrency handling
- [x] Swagger / API doc
- [x] Focused test
- [x] cURL example

Business logic:
- client ส่งเฉพาะ `mobileNumber`
- `mobileNumber` ต้องเป็นเบอร์ไทย 10 หลักขึ้นต้นด้วย `0`
- server อ่าน `mobile_user_uuid` จาก authenticated user เท่านั้น
- request ต้องเป็น active renewal request ที่เป็นของ user ที่ login
- update `m_document_request.mobile_number` เป็นค่าใหม่เสมอเมื่อ request ถูกต้อง
- update `m_document_request_delivery_address.mobile_number` ด้วยค่าเดียวกัน
- ถ้า request ไม่มี delivery address snapshot ให้ rollback เพราะ endpoint นี้ต้อง update ทั้ง `m_document_request` และ `m_document_request_delivery_address`
- ทุก update อยู่ใน transaction เดียวกัน

Acceptance criteria:
- user แก้ mobile number ของ renewal request ตัวเองได้
- user แก้ renewal request ของ user อื่นไม่ได้
- invalid mobile number ถูก reject ก่อน update
- `m_document_request.mobile_number` ถูก update ตาม request body
- `m_document_request_delivery_address.mobile_number` ถูก update ตาม request body
- ถ้าไม่มี address snapshot ต้อง rollback โดยไม่มี partial update ใน `m_document_request`

Evidence when done:
- API example: `PUT /v1/documents-renewals/requests/{requestNo}/mobile`
- Test classes: `DocumentRenewalMobileServiceTest`, `DocumentRenewalControllerTest`, `DocumentRenewalFoundationRepositoryTest`
- Files changed: `Routes`, `DocumentRenewalController`, `DocumentRenewalMobileService`, `DocumentRenewalFoundationRepository`, `DocumentRenewalMobileRequest`, `DocumentRenewalMobileResponse`

Request body:

```json
{
  "mobileNumber": "0821549970"
}
```

Response fields:

```json
{
  "code": "MA00000",
  "description": "Success",
  "data": {
    "requestNo": "260700001",
    "mobileNumber": "0821549970"
  }
}
```

```bash
curl --request PUT \
  --url "${base_url}/v1/documents-renewals/requests/260700001/mobile" \
  --header "Authorization: Bearer ${access_token}" \
  --header "Content-Type: application/json" \
  --header "Accept-Language: TH" \
  --data '{
    "mobileNumber": "0821549970"
  }'
```

## Remaining Work

| Priority | Task | Why it remains | Next action | Blocker |
|:---:|---|---|---|---|
| High | Production renewal price config | implementation และ table พร้อมแล้ว แต่ยังไม่มีหลักฐานราคาจริงสำหรับ production | เพิ่ม production seed/config ใน `m_document_prices_setting` หลังได้รับราคา | Product/Admin ต้องยืนยันราคาจริง |
| High | Target DB migration confirmation | source มี RUN scripts แต่เอกสารนี้ตรวจไม่ได้ว่า deploy แล้ว | ตรวจ migration log หรือ query target DB เพื่อยืนยัน scripts ที่เกี่ยวข้องถูก run แล้ว | DBA/DevOps access to target DB |
| High | Status master deployment confirmation | `02_seed_mvp1_master_data.sql` มี seed แต่ยังไม่รู้ว่า target DB มีข้อมูลครบ | query `m_document_status` ใน target DB และยืนยัน visible statuses ครบ | DBA/DevOps access to target DB |
| Medium | Required document master completeness | มี validation API และ seed ตัวอย่าง แต่ยังไม่ยืนยัน master จริงครบทุก `document_code` | ตรวจ/เติม `m_document_setting_requires` สำหรับเอกสารที่เปิด renewal ทุกตัว | Product/Admin ต้องยืนยัน required items |
| Medium | Thailand address runtime import | API พร้อมแล้ว แต่ต้องมี external tables/data ใน runtime DB | import `04_import_thailand_address_master.sql` และตรวจ row count provinces/districts/subdistricts | DBA/DevOps access to target DB |
| Low | MySQL 8 Testcontainers harness | unit/contract tests ผ่านแล้ว แต่ยังไม่มี disposable DB migration smoke test | เปิด task แยกเพื่อเพิ่ม Testcontainers และ bootstrap schema จาก RUN scripts | ต้องอนุมัติเพิ่ม test dependency และ Docker-based CI |

## Definition Of Done

task จะเปลี่ยนเป็น `[x]` ได้เมื่อครบทั้งหมดนี้:

- implementation ครบตาม scope
- acceptance criteria ผ่านครบ
- มี test อย่างน้อย focused test สำหรับ behavior สำคัญ
- ไม่มี known blocker ใน implementation scope
- update API doc หรือ cURL ถ้าเป็น public endpoint
- ระบุ evidence ชัดเจน เช่น test class, endpoint, SQL script หรือไฟล์ที่เกี่ยวข้อง

Operational งานที่อยู่นอก implementation scope เช่น production seed, target DB deployment confirmation หรือ external data import ให้แสดงใน `Remaining Work` แม้ task implementation จะเป็น `[x]`

## Recommended Implementation Order

1. ปิด open decisions: request number, unpaid draft, address และ payment channel
2. MR-MOB-00 validate and create renewal request draft
3. MR-MOB-01 shared foundation
4. MR-MOB-02 status และ MR-MOB-03 price
5. MR-MOB-15 identity document multi-file upload และปรับ profile document validation
6. MR-MOB-13 Thailand address master APIs ก่อนเริ่ม delivery-address UI/API
7. MR-MOB-14 create/update delivery address APIs และ MR-MOB-16 get default delivery address
8. MR-MOB-04 create request
9. MR-MOB-08 renewal item upload และ MR-MOB-09 resubmit โดย reuse file validation/storage component จาก MR-MOB-15
10. MR-MOB-05 list, MR-MOB-06 detail และ MR-MOB-07 timeline
11. MR-MOB-10 payment attempt, payment webhook และ MR-MOB-11 payment status
12. MR-MOB-12 contract/integration/concurrency tests
13. MR-MOB-17 atomic mobile-number update และ change history
14. MR-MOB-19 renewal request mobile snapshot update

## Evidence Checked

- `src/main/java/com/seaman/constant/Routes.java`
- `src/main/java/com/seaman/controller/DocumentController.java`
- `src/main/java/com/seaman/service/DocumentService.java`
- `src/main/java/com/seaman/repository/DocumentRepository.java`
- `src/main/java/com/seaman/entity/DocumentRequestItemEntity.java`
- `src/main/java/com/seaman/model/response/DocumentRequestItemResponse.java`
- `documents/mvp1/script/01_create_mvp1_tables.sql`
- `documents/mvp1/script/02_seed_mvp1_master_data.sql`
- `documents/mvp1/script/03_create_core_indexes.sql`
- `documents/mvp1/script/04_import_thailand_address_master.sql`
- `documents/mvp1/script/90_03_migrate_document_renewal_price_effective_period.sql`
- `documents/mvp1/script/90_04_migrate_renewal_request_draft.sql`
- `documents/mvp1/script/90_05_migrate_delivery_address_default_guard.sql`
- `documents/mvp1/script/90_06_migrate_identity_document_multi_file.sql`
- `documents/mvp1/script/90_08_migrate_mobile_number_history.sql`
- `documents/mvp1/script/90_09_migrate_omise_payment_channels.sql`
- `src/main/java/com/seaman/controller/ProfileController.java`
- `src/main/java/com/seaman/service/ProfileService.java`
- `src/main/java/com/seaman/repository/UserRepository.java`
- `src/main/java/com/seaman/controller/DocumentRenewalController.java`
- `src/main/java/com/seaman/service/DocumentRenewalMobileService.java`
- `src/main/java/com/seaman/repository/DocumentRenewalFoundationRepository.java`
- `documents/non_prod_db_schema.md`

## Assumptions

- ใช้ Markdown เป็นหลัก และเก็บเอกสาร task ใต้ `documents/.../task/`
- ไม่สร้าง backend/database ใหม่สำหรับ task tracking ในตอนนี้
- ใช้ status แบบ checklist เพราะอ่านง่ายสำหรับ backend, mobile, PM และ QA
