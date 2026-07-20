# DocumentService Flow

เอกสารนี้อธิบายขั้นตอนการทำงานของ `src/main/java/com/seaman/service/DocumentService.java`

## ภาพรวม

`DocumentService` เป็น service หลักสำหรับจัดการ certificate/document ของลูกเรือ และทำงานร่วมกับ document renewal flow บางส่วน โดยใช้ข้อมูลผู้ใช้จาก `HttpServletRequest` attribute `userObject`

Dependency หลัก:

- `DocumentRepository`: อ่านรายการเอกสาร, เอกสารใกล้หมดอายุ, และ required request items
- `CertificateRepository`: create/update/delete/read certificate ของผู้ใช้
- `DocumentRequestItemFileRepository` และ `DocumentRequestItemFileService`: อ่านและ map ไฟล์ประกอบ request item
- `DocumentRenewalCreateRepository`: generate request number และ insert `m_document_request`, `m_document_request_items`
- `DocumentRenewalFoundationRepository`: หา status และบันทึก transaction ของ renewal request
- `DocumentRenewalService`: หา price setting ของ document renewal
- `AmazonS3`: upload/read certificate file
- `TransactionLogsService`: insert/update transaction log ทุก flow หลัก

## Common Pattern

method ส่วนใหญ่ใช้ pattern เดียวกัน:

1. สร้าง response object
2. อ่าน `trace_id`, request body, และ `userObject` จาก `HttpServletRequest`
3. insert transaction log ด้วย `transactionLogsService.insert(...)`
4. ทำ business logic ผ่าน repository หรือ S3
5. catch `CommonException` แล้วส่งต่อ code เดิม
6. catch exception อื่นแล้ว wrap เป็น `BusinessException(AppStatus.EXCEPTION_GLOBAL, ...)`
7. update transaction log ใน `finally`

## pageDocument

ใช้สำหรับแสดงรายการ certificate/document แบบแบ่งหน้า

Input:

- `offSet`
- `documentType`: เช่น `COT` หรือ `Document`

ขั้นตอน:

1. กำหนด service name เป็น `CERTIFICATE OF TRAINING` ถ้าเป็น COT ไม่เช่นนั้นเป็น `DOCUMENTS`
2. อ่าน current user จาก `userObject`
3. บันทึก transaction log
4. เรียก `documentRepository.findByPage(userUuid, offSet, documentType)`
5. เรียก `documentRepository.countByPageByUserUid(...)` เพื่อหา total
6. คำนวณ flag `last`
7. loop รายการเอกสารเพื่อจัด format:
   - ถ้าไม่มี start/end date ให้แสดงค่าว่าง
   - ถ้ามี end date ให้คำนวณปี/เดือน/วันคงเหลือ
   - format start/end date เป็น `yyyy-MM-dd`
8. set `items` และ `itemTotal` ใน `PageDocumentResponse`

## closeToExpiration

ใช้สำหรับแสดงรายการเอกสารที่ใกล้หมดอายุ

Input:

- `offSet`

ขั้นตอน:

1. อ่าน current user
2. บันทึก transaction log ด้วย service name `DOCUMENTS EXPIRATION CHECKED`
3. เรียก `documentRepository.findCloseToExpiration(userUuid, offSet)`
4. เรียก `documentRepository.countByPageByUserUidCloseToExpiration(userUuid)`
5. คำนวณ flag `last`
6. loop รายการเอกสารเพื่อคำนวณระยะเวลาคงเหลือและ format date
7. คืน `PageDocumentResponse`

## documentCreate

ใช้สร้าง certificate ใหม่พร้อม upload file ไป S3

Input:

- `DocumentCreateRequest`

ขั้นตอน:

1. อ่าน current user
2. บันทึก transaction log ด้วย service name `CERT_CREATE`
3. generate file id ใหม่ด้วย `frameworkUtils.generateUUID()`
4. ตรวจสอบว่า user ยังไม่มี certificate ของ `documentCode` นี้ด้วย `certificateRepository.findByUsersAndCertCodeList(...)`
5. ถ้ามีอยู่แล้ว throw `DATA_IS_EXISTING`
6. validate base64 file ด้วย `base64FileValidator.validateDocument(...)`
7. สร้าง `CertificateEntity`
8. insert certificate ผ่าน `certificateRepository.insert(entity)`
9. ถ้า insert สำเร็จ:
   - สร้าง S3 key จาก `object.store.path.template`
   - upload file ไป S3 bucket
   - set response field เช่น `documentCode`, date, end date type, file name
10. update transaction log และคืน `DocumentCreateResponse`

## documentUpdate

ใช้แก้ไข certificate เดิม โดยรองรับทั้งเปลี่ยนไฟล์และไม่เปลี่ยนไฟล์

Input:

- `DocumentUpdateRequest`

ขั้นตอน:

1. อ่าน current user
2. บันทึก transaction log ด้วย service name `CERT_UPDATE`
3. generate file id ใหม่ไว้ล่วงหน้า
4. ตรวจสอบว่า certificate ของ user/documentCode มีอยู่
5. ถ้าไม่พบ throw `DATA_IS_EXISTING`
6. สร้าง `CertificateEntity` พร้อมข้อมูล update
7. ถ้า `isChangeFile = N`:
   - update เฉพาะข้อมูล certificate ด้วย `certificateRepository.updateNoChangeFile(entity)`
8. ถ้าเปลี่ยนไฟล์:
   - validate file
   - set file id และ original file name
   - update DB ด้วย `certificateRepository.update(entity)`
   - ถ้า update สำเร็จ upload file ใหม่ไป S3
9. set response แล้ว update transaction log

## documentDelete

ใช้ลบ certificate ตาม `certCode`

Input:

- `certCode`

ขั้นตอน:

1. อ่าน current user
2. บันทึก transaction log ด้วย service name `CERT_DELETE`
3. ตรวจสอบว่า certificate ของ user/certCode มีอยู่
4. ถ้าไม่พบ throw `DATA_IS_EXISTING`
5. เรียก `certificateRepository.documentDelete(userUuid, certCode)`
6. ถ้าสำเร็จ log `Delete Cert is success.`
7. update transaction log และคืน `"success"`

หมายเหตุ: comment ของ controller ระบุว่าเป็น soft delete แต่ repository method ปัจจุบันต้องดู implementation เพิ่มเติมว่าเป็น delete จริงหรือ update status

## documentEdit

ใช้ดึงข้อมูล certificate เพื่อแสดงในหน้า edit

Input:

- `certCode`

ขั้นตอน:

1. อ่าน current user
2. บันทึก transaction log ด้วย service name `CERT_EDIT`
3. หา certificate ด้วย `certificateRepository.findByUsersAndCertCodeList(userUuid, certCode)`
4. ถ้าไม่พบ throw `DATA_IS_EXISTING`
5. map ข้อมูล certificate เป็น `DocumentCreateResponse`
6. ถ้า `certEndDate` เป็น null:
   - set `certEndDateType = N`
   - set `certEndDate = 9999-99-99`
7. ถ้ามี `certEndDate`:
   - set `certEndDateType = A`
   - format end date เป็น `yyyy-MM-dd`
8. set file name และคืน response

## validateAndCreateDocumentRenewalsItems

ใช้ตรวจ required document request items ของ document code และสร้าง renewal request draft เมื่อเอกสาร profile ครบ

Input:

- `DocumentRequestValidateRequest.documentCode`

Transaction:

- method นี้มี `@Transactional`
- ถ้า insert `m_document_request` หรือ `m_document_request_items` fail จะ rollback ทั้ง flow

ขั้นตอน:

1. สร้าง `DocumentRequestValidateResponse`
2. อ่าน current user จาก `userObject`
3. ถ้าไม่มี user ใน request ให้ throw `EXCEPTION_GLOBAL`
4. บันทึก transaction log ด้วย service name `VALIDATE_AND_CREATE_DOCUMENT_RENEWALS_ITEMS`
5. normalize `documentCode` เป็น uppercase
6. เรียก `documentRepository.findMissingItemsByUserAndDocumentCode(userUuid, documentCode)`
7. ถ้าไม่พบ required item config ให้ throw `DOCUMENT_SETTING_NOT_FOUND`
8. map รายการ `DocumentRequestItemEntity` เป็น `DocumentRequestItemResponse`
   - status เช่น `MISSING`, `NOT_UPLOADED`, `NEED_FIX`, `COMPLETE` มาจาก repository query
   - ถ้ามี `profileRequestItemId` จะดึงไฟล์ประกอบและ map เข้า `files`
9. set `documentCode` และ `items` ลง response
10. ตรวจว่ามี profile item ที่ยังไม่ complete หรือไม่ด้วย `hasIncompleteProfileItem(items)`
11. ถ้ายังมี PROFILE item ที่ไม่ใช่ `COMPLETE`:
    - ไม่สร้าง request
    - คืน response ที่มี `items`
    - `requestId` และ `requestNo` จะเป็น null
12. ถ้า PROFILE items ครบ:
    - หา price ด้วย `documentRenewalService.price(documentCode)`
    - generate `requestId`
    - generate period จากวันปัจจุบันใน timezone `Asia/Bangkok` รูปแบบ `yyMM`
    - generate `requestNo` ด้วย `documentRenewalCreateRepository.nextRequestNo(period)`
    - หา status id ของ `PAYMENT_PENDING`
    - insert `m_document_request`
      - `delivery_address_id` เป็น null
      - `is_active` เป็น `YES`
      - amount มาจาก active price setting
    - insert required items ลง `m_document_request_items`
    - ตรวจจำนวน row ที่ insert ต้องเท่ากับจำนวน required items ที่ validate ได้
    - append transaction action `CREATE`
    - set `requestId` และ `requestNo` ลง response
13. update transaction log และคืน response

ผลลัพธ์สำคัญ:

- เอกสารยังไม่ครบ: API ทำหน้าที่ validate และคืนรายการ missing/fix โดยไม่สร้าง `m_document_request`
- เอกสารครบ: API สร้าง draft request และคืน `requestId/requestNo`
- REQUEST scoped item ที่ยัง `MISSING` ไม่บล็อกการสร้าง request เพราะเป็นเอกสารที่จะ upload หลังมี request แล้ว

## hasIncompleteProfileItem

เป็น private helper สำหรับตัดสินว่าควรสร้าง renewal request หรือยัง

เงื่อนไข:

- ถ้า item ไม่ใช่ `storageScope = REQUEST`
- และ `documentStatus` ไม่ใช่ `COMPLETE`
- ถือว่ายังมี profile document ไม่ครบ

ผลกระทบ:

- PROFILE item ที่ `MISSING`, `NOT_UPLOADED`, `NEED_FIX`, `INCOMPLETE` จะบล็อกการสร้าง request
- REQUEST item ไม่บล็อกการสร้าง request

## mapDocumentRequestItems

เป็น private helper สำหรับ map entity เป็น response

ขั้นตอน:

1. loop `DocumentRequestItemEntity`
2. copy field หลัก เช่น id, document code, item code, storage scope, type, name, status, sort order, file info, check result
3. ถ้ามี `profileRequestItemId`:
   - ดึงไฟล์จาก `documentRequestItemFileRepository.findFiles(userUuid, itemCode)`
   - map เป็น response ด้วย `documentRequestItemFileService.mapFiles(...)`
4. ถ้ามี `fileUploadedAt`:
   - format เป็น `DateUtil.DATE_TIME`
5. คืน list ของ `DocumentRequestItemResponse`

## viewCert

ใช้โหลด certificate file จาก S3

Input:

- `certCode`

ขั้นตอน:

1. อ่าน current user
2. หา certificate ด้วย `certificateRepository.findBy(userUuid, certCode)`
3. ถ้าไม่พบ throw `DATA_NOT_FOUND`
4. สร้าง S3 key จาก `object.store.path.template`
5. อ่าน object เป็น string ด้วย `getS3.getObjectAsString(bucketName, keyName)`
6. คืน file content string

## Endpoint ที่เกี่ยวข้อง

DocumentService ถูกเรียกจาก `DocumentController` โดย endpoint หลัก ได้แก่:

- `GET /v1/documents/certification/COT`
- `GET /v1/documents/certification/DOC`
- `GET /v1/documents/certification/to-expiration`
- `POST /v1/documents/certification/create`
- `POST /v1/documents/certification/update`
- `DELETE /v1/documents/certification/delete`
- `GET /v1/documents/certification/edit`
- `POST /v1/documents/requests/validate-and-create`
- `POST /v1/documents/request-items/validate` เป็น alias เก่า
- `GET /v1/documents/certification/view`

## Tables ที่เกี่ยวข้อง

- `m_certificates`: certificate ของ user
- `m_documents`: master document
- `m_document_setting_requires`: mapping document code กับ required request item
- `m_document_master_request_item`: master request item และ storage scope
- `m_document_profile_request_item`: profile-level uploaded request item files
- `m_document_request`: renewal request draft/main record
- `m_document_request_items`: required items snapshot ของ request
- `m_document_transaction`: renewal transaction timeline
- `m_document_prices_setting`: active price setting
- `m_document_status`: renewal status master

