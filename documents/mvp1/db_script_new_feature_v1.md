# Smart Seaman — Database Schema (MySQL)

> ระบบจัดการคำขอต่อเอกสารสำหรับลูกเรือ (Document Renewal Request Management)
> **Database Engine:** MySQL 8.0+ · Character Set: `utf8mb4` · Collation: `utf8mb4_unicode_ci`

---


## สารบัญ
- [ภาพรวม Flow ของระบบ](#ภาพรวม-flow-ของระบบ)
- [M_DOCUMENTS Migration](#m_documents-migration)
- [M_DOCUMENT_STATUS](#m_document_status)
- [M_DOCUMENT_REQUEST](#m_document_request)
- [M_DOCUMENT_PRICES_SETTING](#m_document_prices_setting)
- [M_PAYMENT_TRANSACTION](#m_payment_transaction)
- [M_DOCUMENT_MASTER_REQUEST_ITEM](#m_document_master_request_item)
- [M_DOCUMENT_REQUEST_ITEMS](#m_document_request_items)
- [M_DOCUMENT_PROFILE_REQUEST_ITEM](#m_document_profile_request_item)
- [M_DOCUMENT_REQUEST_ITEM_FILES](#m_document_request_item_files)
- [M_DOCUMENT_SETTING_REQUIRES](#m_document_setting_requires)
- [M_DOCUMENT_TRANSACTION](#m_document_transaction)
- [M_DEPT_SUBMISSION](#m_dept_submission--รอรับเอกสาร-จากกรมเจ้าท่า)
- [M_DELIVERY_ADDRESS](#m_delivery_address)
- [M_DOCUMENT_REQUEST_DELIVERY_ADDRESS](#m_document_request_delivery_address)
- [M_DELIVERY](#m_delivery--กำลังจัดส่ง)
- [Status Reference](#status-reference)
- [Action Reference](#action-reference)
---

## ภาพรวม Flow ของระบบ

```
ลูกเรือยื่นคำขอ
       │
       ▼
[รอตรวจเอกสาร] ──ส่งกลับ──► [รอผู้ยื่นแก้ไข] ──resubmit──► [รอตรวจเอกสาร]
       │
       │ ผ่านการตรวจ
       ▼
[รอผลกรมเจ้าท่า]
       │
       ▼
[รอรับเอกสารจากกรม]
       │
       ▼
[กำลังจัดส่ง]
       │
       ▼
[จัดส่งสำเร็จ]

(ยกเลิกได้จากทุก status ก่อนจัดส่ง)
```

ทุก state transition จะถูกบันทึกเป็น row ใน `m_document_transaction` เสมอ ทำให้สามารถ audit trail ย้อนหลังได้ทุก action

---

## M_DOCUMENTS Migration

**บทบาท:** เพิ่ม flag ใน master table `m_documents` เพื่อระบุว่า document ประเภทนั้นสามารถขอต่ออายุผ่าน mobile app ได้หรือไม่

```sql
ALTER TABLE m_documents
    ADD COLUMN DOCUMENT_RENEWAL_FLAG VARCHAR(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NO'
        COMMENT 'Flag for document renewal on mobile app. YES = can renew, NO = cannot renew'
        AFTER DOCUMENT_MOBILE_FLAG,
    ADD CONSTRAINT chk_documents_renewal_flag
        CHECK (DOCUMENT_RENEWAL_FLAG IN ('YES', 'NO'));
```

**หมายเหตุ:**
- `DOCUMENT_RENEWAL_FLAG = 'YES'` หมายถึงสามารถเลือกต่ออายุเอกสารนี้ได้
- `DOCUMENT_RENEWAL_FLAG = 'NO'` หมายถึงยังไม่เปิดให้ต่ออายุผ่าน flow นี้
- ตั้ง default เป็น `NO` เพื่อป้องกันไม่ให้เอกสารเดิมทั้งหมดถูกเปิดต่ออายุโดยอัตโนมัติ
- ใช้ `VARCHAR(3)` และ collation `utf8mb4_general_ci` ให้รองรับค่า `YES`/`NO`

---

## M_DOCUMENT_STATUS
**บทบาท:** ตารางเก็บข้อมูล document status

```sql
CREATE TABLE m_document_status (
    id              CHAR(36)        NOT NULL DEFAULT (UUID()),
    name_th         VARCHAR(255)    NOT NULL,
    name_en         VARCHAR(255)    NOT NULL,
    css_color       VARCHAR(100)    NOT NULL,
    is_active       VARCHAR(3)      NOT NULL DEFAULT 'YES',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_document_status_name_th (name_th),
    UNIQUE KEY uq_document_status_name_en (name_en),
    CONSTRAINT chk_document_status_active
        CHECK (is_active IN ('YES', 'NO'))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```


## M_DOCUMENT_REQUEST

**บทบาท:** ตาราง core ของระบบ แต่ละ row คือหนึ่งคำขอต่อเอกสาร เก็บ `status` ปัจจุบัน, ข้อมูลการชำระเงิน และเวลาที่ยื่น

```sql
CREATE TABLE m_document_request (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    request_no          VARCHAR(20)     NOT NULL,
    mobile_user_uuid    VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    mobile_number       VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
    email               VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
    document_code       VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    idempotency_key     VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
    document_status_id  CHAR(36)        NOT NULL,
    price_setting_id    CHAR(36)        NULL,
    delivery_address_id CHAR(36)        NULL,
    is_resubmit         TINYINT(1)      NOT NULL DEFAULT 0,     -- 1 = ผู้ยื่น resubmit หลังแก้ไข
    is_active           VARCHAR(3)      NOT NULL DEFAULT 'YES', -- YES = active, NO = soft deleted
    amount              DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,  -- ยอดชำระ (บาท)
    submitted_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_by        CHAR(36)        NULL,                   -- NULL = ลูกเรือยื่นเอง
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_docreq_request_no (request_no),
    UNIQUE KEY uq_docreq_user_idempotency_key (mobile_user_uuid, idempotency_key),
    KEY idx_docreq_mobile_user (mobile_user_uuid),
    KEY idx_docreq_document_code (document_code),
    KEY idx_docreq_price_setting (price_setting_id),
    KEY idx_docreq_delivery_address (delivery_address_id),
    KEY idx_docreq_active_user (is_active, mobile_user_uuid),
    KEY idx_docreq_status_submitted (document_status_id, submitted_at),
    KEY idx_docreq_validate_user_doc_active_submitted_id
        (mobile_user_uuid, document_code, is_active, submitted_at, id),
    CONSTRAINT chk_docreq_amount
        CHECK (amount >= 0),
    CONSTRAINT chk_docreq_resubmit
        CHECK (is_resubmit IN (0, 1)),
    CONSTRAINT fk_docreq_status
        FOREIGN KEY (document_status_id) REFERENCES m_document_status (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

**หมายเหตุ:**
- `mobile_user_uuid` คือ logical reference ไปยัง `m_mobile_users.MOBILE_UUID` ของระบบเดิม
- `mobile_number` และ `email` คือ snapshot จาก `m_mobile_users` ณ เวลาสร้าง request
- `document_code` คือ logical reference ไปยัง `m_documents.DOCUMENT_CODE` ของระบบเดิม
- `mobile_user_uuid` และ `document_code` ใช้ `utf8mb4_general_ci` เพื่อให้ join กับ legacy/mobile และ MVP1 master tables ได้โดยไม่เกิด collation mismatch
- ยังไม่ใส่ FK ตรงไปยัง `m_mobile_users` และ `m_documents` ใน script นี้ เพราะต้องยืนยัน DDL/type ของ table เดิมก่อน หาก type/index ตรงกันให้เพิ่ม FK ใน migration เฉพาะ environment ได้
- `request_no` สร้างจาก sequence ที่ฝั่ง application (format: `YYMM` + running 5 digits เช่น `250500001`) เพื่อรองรับมากกว่า 999 requests ต่อเดือน
- `is_resubmit = 1` จะเซ็ตเมื่อลูกเรือแก้ไขเอกสารแล้วส่งกลับมาใหม่ UI จะแสดง badge "ผู้ยื่น resubmit"
- `is_active = 'NO'` ใช้กับ soft delete และต้องอยู่ใน owner-scoped lookup ทุกครั้ง
- `price_setting_id` อ้าง `m_document_prices_setting.id`; `amount` เป็นยอดรวมที่ตรึงไว้ตอนสร้าง request
- `delivery_address_id` อ้าง `m_delivery_address.id` เป็น source reference; ข้อมูลที่อยู่ ณ เวลาสร้าง request จะถูก copy ไปที่ `m_document_request_delivery_address`
- `idx_docreq_validate_user_doc_active_submitted_id` รองรับ lookup active non-delivered renewal request ของ validate-and-create

---
## M_DOCUMENT_PRICES_SETTING
**บทบาท:** ตาราง setting ราคาเอกสารแต่ละ document

```sql
CREATE TABLE m_document_prices_setting (
    id                      CHAR(36)        NOT NULL DEFAULT (UUID()),
    document_code           VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    government_fee          DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    document_processing_fee DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    shipping_fee            DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    shipping_discount       DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    service_fee_discount    DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    effective_from          DATE             NOT NULL,
    effective_to            DATE             NULL,
    is_active               VARCHAR(3)      NOT NULL DEFAULT 'YES',
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_docprice_document_effective (document_code, effective_from),
    KEY idx_docprice_active_effective (document_code, is_active, effective_from, effective_to),
    CONSTRAINT chk_docprice_fee
        CHECK (
            government_fee >= 0
            AND document_processing_fee >= 0
            AND shipping_fee >= 0
            AND shipping_discount >= 0
            AND service_fee_discount >= 0
        ),
    CONSTRAINT chk_docprice_active
        CHECK (is_active IN ('YES', 'NO')),
    CONSTRAINT chk_docprice_effective_period
        CHECK (effective_to IS NULL OR effective_to >= effective_from),
    CONSTRAINT fk_docprice_document
        FOREIGN KEY (document_code) REFERENCES m_documents (DOCUMENT_CODE)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

**หมายเหตุ:**
- `document_code` อ้างอิง `m_documents.DOCUMENT_CODE`; ชื่อเอกสารให้ join จาก `m_documents.DOCUMENT_NAME_TH` และ `m_documents.DOCUMENT_NAME_EN`
- ไม่เก็บ `document_name_th` และ `document_name_en` ใน table นี้ เพื่อลดข้อมูลซ้ำและกันชื่อเอกสารไม่ตรงกับ master
- ราคาสุทธิที่เรียกเก็บควรคำนวณที่ application layer จาก `government_fee + document_processing_fee + shipping_fee - shipping_discount - service_fee_discount` และต้องไม่ติดลบ


## M_PAYMENT_TRANSACTION

**บทบาท:** เก็บ payment attempt และ refund ที่เกิดกับคำขอเอกสาร โดยออกแบบสำหรับ Omise
หนึ่ง `m_document_request` สามารถมีหลาย transaction ได้ เช่น QR หมดอายุแล้วสร้างใหม่,
บัตรชำระไม่ผ่านแล้วลองใหม่ หรือคืนเงินหลายครั้ง (partial refund)

```sql
CREATE TABLE m_payment_transaction (
    id                      CHAR(36)        NOT NULL DEFAULT (UUID()),
    request_id              CHAR(36)        NOT NULL,
    parent_transaction_id   CHAR(36)        NULL,

    transaction_no          VARCHAR(40)     NOT NULL,
    transaction_type        VARCHAR(10)     NOT NULL DEFAULT 'CHARGE',
                                            -- CHARGE | REFUND
    channel                 VARCHAR(30)     NOT NULL,
                                            -- PROMPTPAY | CREDIT_CARD | INTERNET_BANKING
    payment_method          VARCHAR(50)     NOT NULL,
                                            -- promptpay | card | internet_banking_scb | ...

    amount                  DECIMAL(12, 2)  NOT NULL,
    currency                CHAR(3)         NOT NULL DEFAULT 'THB',
    refunded_amount         DECIMAL(12, 2)  NOT NULL DEFAULT 0.00,
                                            -- ยอดคืนสะสม ใช้กับ CHARGE เท่านั้น
    status                  VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
                                            -- PENDING | PROCESSING | SUCCESS | FAILED |
                                            -- EXPIRED | PARTIALLY_REFUNDED | REFUNDED | CANCELLED

    provider                VARCHAR(20)     NOT NULL DEFAULT 'OMISE',
    provider_charge_id      VARCHAR(100)    NULL, -- Omise chrg_... / chrg_test_...
    provider_source_id      VARCHAR(100)    NULL, -- Omise src_... / src_test_...
    provider_refund_id      VARCHAR(100)    NULL, -- Omise rfnd_... / rfnd_test_...
    provider_transaction_id VARCHAR(100)    NULL, -- Omise trxn_... (ถ้ามี)
    provider_status         VARCHAR(30)     NULL, -- status ดิบล่าสุดจาก Omise
    provider_response       JSON            NULL, -- response ล่าสุด (ห้ามมี secret/token)

    idempotency_key         VARCHAR(100)    NOT NULL,
    description             VARCHAR(255)    NULL,
    return_uri              VARCHAR(500)    NULL,
    authorize_uri           VARCHAR(500)    NULL, -- URL สำหรับ 3DS / bank redirect

    bank_code               VARCHAR(30)     NULL,
    card_brand              VARCHAR(30)     NULL,
    card_last_digits        CHAR(4)         NULL,
    failure_code            VARCHAR(100)    NULL,
    failure_message         VARCHAR(500)    NULL,

    is_livemode             TINYINT(1)      NOT NULL DEFAULT 0,
    expires_at              DATETIME        NULL,
    paid_at                 DATETIME        NULL,
    failed_at               DATETIME        NULL,
    refunded_at             DATETIME        NULL,
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                            ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_payment_transaction_no (transaction_no),
    UNIQUE KEY uq_payment_idempotency_key (idempotency_key),
    UNIQUE KEY uq_payment_provider_charge (provider, provider_charge_id),
    UNIQUE KEY uq_payment_provider_refund (provider, provider_refund_id),
    KEY idx_payment_request_created (request_id, created_at),
    KEY idx_payment_status_updated (status, updated_at),
    KEY idx_payment_parent (parent_transaction_id),

    CONSTRAINT chk_payment_amount
        CHECK (amount > 0),
    CONSTRAINT chk_payment_refunded_amount
        CHECK (refunded_amount >= 0 AND refunded_amount <= amount),
    CONSTRAINT chk_payment_type
        CHECK (transaction_type IN ('CHARGE', 'REFUND')),
    CONSTRAINT chk_payment_channel
        CHECK (channel IN ('PROMPTPAY', 'CREDIT_CARD', 'INTERNET_BANKING')),
    CONSTRAINT chk_payment_status
        CHECK (status IN (
            'PENDING', 'PROCESSING', 'SUCCESS', 'FAILED', 'EXPIRED',
            'PARTIALLY_REFUNDED', 'REFUNDED', 'CANCELLED'
        )),
    CONSTRAINT chk_payment_currency
        CHECK (currency = 'THB'),
    CONSTRAINT chk_payment_livemode
        CHECK (is_livemode IN (0, 1)),
    CONSTRAINT fk_payment_request
        FOREIGN KEY (request_id) REFERENCES m_document_request (id),
    CONSTRAINT fk_payment_parent
        FOREIGN KEY (parent_transaction_id) REFERENCES m_payment_transaction (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

**แนวทางการใช้งานกับ Omise:**

- `amount` เก็บเป็นหน่วยบาทในฐานข้อมูล แต่ตอนเรียก Omise ต้องแปลงเป็นหน่วยย่อย (THB 1,500.00 = `150000`) ด้วย `BigDecimal` ห้ามใช้ `double`
- แต่ละ payment attempt สร้าง row `transaction_type = 'CHARGE'` ใหม่เสมอ ห้าม reuse Omise token/source เพราะ token และ source ใช้ได้ครั้งเดียว
- `provider_charge_id` คือ ID หลักสำหรับ query สถานะจาก `GET /charges/{id}`; `provider_source_id` ใช้กับ PromptPay และช่องทาง redirect
- Refund แต่ละครั้งสร้าง row `transaction_type = 'REFUND'`, ระบุ `parent_transaction_id` ไปยัง CHARGE และเก็บ `provider_refund_id`; จากนั้นอัปเดต `refunded_amount` และ status ของ CHARGE เป็น `PARTIALLY_REFUNDED` หรือ `REFUNDED` ภายใน database transaction เดียวกัน
- `idempotency_key` เป็น key ภายในระบบสำหรับกัน mobile retry/API retry สร้าง charge หรือ refund ซ้ำ ควรสร้างจาก operation เดียวกันและใช้ค่าเดิมทุกครั้งที่ retry
- `provider_response` ใช้เก็บ payload ล่าสุดเพื่อช่วย audit/debug เท่านั้น ห้ามเก็บ secret key, Omise token หรือข้อมูลบัตรเต็ม; หน้า UI ให้ประกอบ masked card จาก `card_last_digits`
- หลังรับ Omise webhook อย่าเชื่อ payload เพียงอย่างเดียว ให้ใช้ `provider_charge_id` เรียก Charge API เพื่อตรวจสอบสถานะก่อน update และทำ handler ให้ idempotent
- เวลาใน Omise response เป็น UTC; แนะนำเก็บ `paid_at`, `expires_at` และ timestamp อื่นเป็น UTC แล้วแปลงเป็น `Asia/Bangkok` ตอนแสดงผล
- ไม่ควรลบ transaction เมื่อ request ถูกลบ จึงไม่กำหนด `ON DELETE CASCADE`; ประวัติการเงินควรถูกเก็บไว้เพื่อ audit

**Status mapping (Omise → ระบบ):**

| Omise | `m_payment_transaction.status` |
|---|---|
| charge `pending` | `PENDING` หรือ `PROCESSING` เมื่อรอ 3DS/redirect/webhook |
| charge `successful` | `SUCCESS` |
| charge `failed` | `FAILED` |
| charge `expired` | `EXPIRED` |
| charge `reversed` | `CANCELLED` |
| refund สำเร็จและยอดคืน < ยอด charge | parent CHARGE = `PARTIALLY_REFUNDED` |
| refund สำเร็จและยอดคืน = ยอด charge | parent CHARGE = `REFUNDED` |

> เอกสารอ้างอิง Omise: [Charge API](https://docs.omise.co/charges-api),
> [Source API](https://docs.omise.co/sources-api),
> [Refund API](https://docs.omise.co/refunds-api),
> [Webhooks](https://docs.omise.co/api-webhooks)



## M_DOCUMENT_MASTER_REQUEST_ITEM

**บทบาท:** Master รายการเอกสารประกอบที่ระบบรองรับ เช่น สำเนาบัตรประชาชน รูปถ่าย ใบรับรองแพทย์ และ Sea Service Record

```sql
CREATE TABLE m_document_master_request_item (
    id                          CHAR(36)        NOT NULL DEFAULT (UUID()),
    document_master_items_code  VARCHAR(10)     CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    document_master_items_name  CHAR(36)        NOT NULL,
    sort_order                  TINYINT         NOT NULL DEFAULT 1,
    storage_scope               VARCHAR(10)     NOT NULL DEFAULT 'PROFILE',
    is_active                   VARCHAR(3)      NOT NULL DEFAULT 'YES',
    created_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_doc_master_reqitem_code (document_master_items_code),
    KEY idx_doc_master_reqitem_active_sort (is_active, sort_order),

    CONSTRAINT chk_doc_master_reqitem_active
        CHECK (is_active IN ('YES', 'NO')),
    CONSTRAINT chk_doc_master_reqitem_storage_scope
        CHECK (storage_scope IN ('PROFILE', 'REQUEST'))
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

**หมายเหตุ:**
- `document_master_items_code` เป็น business key ที่ใช้เชื่อมกับ profile, request item และ document setting
- `sort_order` เป็นลำดับเริ่มต้นของรายการ และ `is_active` ใช้ปิดรายการโดยไม่ลบ master
- `storage_scope = 'PROFILE'` หมายถึงใช้ไฟล์จาก profile กลาง; `REQUEST` หมายถึงต้อง upload ไฟล์เฉพาะใน request นั้น

---

## M_DOCUMENT_REQUEST_ITEMS

**บทบาท:** เก็บผลตรวจเอกสารประกอบแต่ละรายการภายในคำขอ หนึ่งคำขอมีผลตรวจได้หนึ่งรายการต่อ document master item

```sql
CREATE TABLE m_document_request_items (
    id                                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    request_id                          CHAR(36)        NOT NULL,
    request_no                          VARCHAR(20)     NOT NULL,
    document_master_request_item_code   VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    approve_status                      VARCHAR(10)     NOT NULL DEFAULT 'PENDING',
    note                                TEXT            NULL,
    created_at                          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_doc_reqitems_request_master (request_id, document_master_request_item_code),
    KEY idx_doc_reqitems_request_status (request_id, approve_status),
    KEY idx_doc_reqitems_request_no (request_no),
    KEY idx_doc_reqitems_master_code (document_master_request_item_code),

    CONSTRAINT chk_doc_reqitems_approve_status
        CHECK (approve_status IN ('PENDING', 'PASS', 'FIX')),
    CONSTRAINT chk_doc_reqitems_fix_note
        CHECK (approve_status <> 'FIX' OR note IS NOT NULL),
    CONSTRAINT fk_doc_reqitems_request
        FOREIGN KEY (request_id) REFERENCES m_document_request (id),
    CONSTRAINT fk_doc_reqitems_master_code
        FOREIGN KEY (document_master_request_item_code) REFERENCES m_document_master_request_item (document_master_items_code)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

**หมายเหตุ:**
- `approve_status` มีค่า `PENDING` (รอตรวจ), `PASS` (ผ่าน) และ `FIX` (ต้องแก้ไข)
- `request_no` snapshot จาก `m_document_request.request_no` เพื่อรองรับ query/display โดย request number
- `note` บังคับเป็น non-NULL เมื่อ `approve_status = 'FIX'`
- Unique key ป้องกัน document master item เดียวกันซ้ำภายใน request เดียวกัน

---

## M_DOCUMENT_PROFILE_REQUEST_ITEM

**บทบาท:** เก็บไฟล์เอกสารประกอบและผลตรวจล่าสุดในระดับ profile ของผู้ใช้ เพื่อให้เอกสารเดิมนำไปตรวจความครบถ้วนกับ document หลายประเภทได้

```sql
CREATE TABLE m_document_profile_request_item (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    mobile_user_uuid    VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    document_master_request_item_code VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    document_type       VARCHAR(20)     NULL,
    slot_code           VARCHAR(20)     NULL,
    sort_order          TINYINT         NOT NULL DEFAULT 1, -- ลำดับที่ในตาราง (1, 2, 3, 4)
    file_uploaded       TINYINT(1)      NOT NULL DEFAULT 0,
    file_path           VARCHAR(500)    NULL,             -- path หรือ URL ของไฟล์ที่อัปโหลด
    original_file_name  VARCHAR(255)    NULL,
    mime_type           VARCHAR(100)    NULL,
    file_size           BIGINT          NULL,
    file_uploaded_at    DATETIME        NULL,
    check_result        VARCHAR(10)     NULL,             -- 'pass' | 'fix' | NULL (ยังไม่ตรวจ)
    check_note          TEXT            NULL,             -- หมายเหตุเมื่อผล = 'fix' (required)
    is_updated          TINYINT(1)      NOT NULL DEFAULT 0, -- 1 = ผู้ยื่น resubmit ไฟล์ใหม่
    checked_at          DATETIME        NULL,
    checked_by          CHAR(36)        NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_profile_reqitem_mobile_user_sort (mobile_user_uuid, sort_order),
    KEY idx_profile_reqitem_master_code (document_master_request_item_code),
    KEY idx_profile_reqitem_check_result (check_result),
    KEY idx_profile_reqitem_validate_state
        (mobile_user_uuid, document_master_request_item_code,
         document_type, slot_code, file_uploaded, check_result),
    UNIQUE KEY uq_profile_reqitem_mobile_master_slot
        (mobile_user_uuid, document_master_request_item_code, document_type, slot_code),
    CONSTRAINT chk_profile_reqitem_file_uploaded
        CHECK (file_uploaded IN (0, 1)),
    CONSTRAINT chk_profile_reqitem_is_updated
        CHECK (is_updated IN (0, 1)),
    CONSTRAINT chk_profile_reqitem_check_result
        CHECK (check_result IS NULL OR check_result IN ('pass', 'fix')),
    CONSTRAINT chk_profile_reqitem_fix_note
        CHECK (check_result <> 'fix' OR check_note IS NOT NULL),
    CONSTRAINT fk_profile_reqitem_mobile_user
        FOREIGN KEY (mobile_user_uuid) REFERENCES m_mobile_users (MOBILE_UUID),
    CONSTRAINT fk_profile_reqitem_master_code
        FOREIGN KEY (document_master_request_item_code) REFERENCES m_document_master_request_item (document_master_items_code)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

**หมายเหตุ:**
- หนึ่งผู้ใช้มีได้หนึ่ง profile item ต่อ `document_master_request_item_code`, `document_type` และ `slot_code`
- `document_type`/`slot_code` ใช้รองรับ `ID_CARD/FRONT+BACK`, `PASSPORT/MAIN` และ `GENERAL/MAIN`
- `check_result` มีค่า `'pass'`, `'fix'` หรือ `NULL`; เมื่อเป็น `'fix'` ต้องมี `check_note`
- `is_updated = 1` ใช้แสดงว่าไฟล์ถูกส่งมาแก้ไขใหม่
- `idx_profile_reqitem_validate_state` รองรับ hot query ของ validate-and-create ที่ aggregate profile file state ต่อ user/item

---

## M_DOCUMENT_REQUEST_ITEM_FILES

**บทบาท:** เก็บไฟล์เอกสารประกอบในระดับ request สำหรับรายการที่ `storage_scope = 'REQUEST'`

```sql
CREATE TABLE m_document_request_item_files (
    id                                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    request_item_id                     CHAR(36)        NOT NULL,
    document_master_request_item_code   VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    document_type                       VARCHAR(20)     NULL,
    slot_code                           VARCHAR(20)     NULL,
    sort_order                          TINYINT         NOT NULL DEFAULT 1,
    file_uploaded                       TINYINT(1)      NOT NULL DEFAULT 0,
    file_path                           VARCHAR(500)    NULL,
    original_file_name                  VARCHAR(255)    NULL,
    mime_type                           VARCHAR(100)    NULL,
    file_size                           BIGINT          NULL,
    file_uploaded_at                    DATETIME        NULL,
    check_result                        VARCHAR(10)     NULL,
    check_note                          TEXT            NULL,
    is_updated                          TINYINT(1)      NOT NULL DEFAULT 0,
    checked_at                          DATETIME        NULL,
    checked_by                          CHAR(36)        NULL,
    created_at                          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_doc_reqitem_files_request_item (request_item_id, sort_order),
    KEY idx_doc_reqitem_files_master_code (document_master_request_item_code),
    KEY idx_doc_reqitem_files_check_result (check_result),
    KEY idx_doc_reqitem_files_validate_state
        (request_item_id, document_type, slot_code, file_uploaded, check_result),
    UNIQUE KEY uq_doc_reqitem_files_slot
        (request_item_id, document_type, slot_code),
    CONSTRAINT chk_doc_reqitem_files_file_uploaded
        CHECK (file_uploaded IN (0, 1)),
    CONSTRAINT chk_doc_reqitem_files_is_updated
        CHECK (is_updated IN (0, 1)),
    CONSTRAINT chk_doc_reqitem_files_check_result
        CHECK (check_result IS NULL OR check_result IN ('pass', 'fix')),
    CONSTRAINT chk_doc_reqitem_files_fix_note
        CHECK (check_result <> 'fix' OR check_note IS NOT NULL),
    CONSTRAINT fk_doc_reqitem_files_request_item
        FOREIGN KEY (request_item_id) REFERENCES m_document_request_items (id),
    CONSTRAINT fk_doc_reqitem_files_master_code
        FOREIGN KEY (document_master_request_item_code) REFERENCES m_document_master_request_item (document_master_items_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**หมายเหตุ:**
- ใช้สำหรับ upload files เฉพาะ request เช่น `MRI004` ที่ seed เป็น `storage_scope = 'REQUEST'`
- Unique key ป้องกัน slot ซ้ำใน request item เดียวกัน
- `idx_doc_reqitem_files_validate_state` รองรับ hot query ของ validate-and-create ที่ aggregate request-scoped file state ต่อ request item

---

## M_DOCUMENT_SETTING_REQUIRES

**บทบาท:** ตาราง setting สำหรับกำหนดว่า document แต่ละประเภทต้องใช้ document item ใดบ้าง พร้อมลำดับแสดงผลและสถานะเปิด/ปิด

```sql
CREATE TABLE m_document_setting_requires (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    document_code       VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    document_master_request_item_code VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    sort_order          TINYINT         NOT NULL DEFAULT 1,
    is_required         TINYINT(1)      NOT NULL DEFAULT 1,
    is_active           VARCHAR(3)      NOT NULL DEFAULT 'YES',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_doc_setting_requires (document_code, document_master_request_item_code),
    KEY idx_doc_setting_requires_doc_sort (document_code, sort_order),
    KEY idx_doc_setting_requires_item (document_master_request_item_code),

    CONSTRAINT chk_doc_setting_requires_required
        CHECK (is_required IN (0, 1)),
    CONSTRAINT chk_doc_setting_requires_active
        CHECK (is_active IN ('YES', 'NO')),

    CONSTRAINT fk_doc_setting_requires_document
        FOREIGN KEY (document_code) REFERENCES m_documents (DOCUMENT_CODE),
    CONSTRAINT fk_doc_setting_requires_item
        FOREIGN KEY (document_master_request_item_code) REFERENCES m_document_master_request_item (document_master_items_code)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

**หมายเหตุ:**
- `UNIQUE (document_code, document_master_request_item_code)` กัน mapping ซ้ำของ document เดียวกัน
- `sort_order` ใช้เรียงรายการเอกสารประกอบบน mobile/admin UI แยกตาม `document_code`
- `is_required` รองรับกรณีบาง item เป็น optional ในอนาคต
- `is_active` ใช้ปิด mapping โดยไม่ต้องลบ row จริง
- เวลา drop table ต้อง drop `m_document_request_item_files`, `m_document_request_items` และ `m_document_setting_requires` ก่อน `m_document_master_request_item`

---

## M_DOCUMENT_TRANSACTION

**บทบาท:** Transaction log ทุก action ที่เกิดขึ้นกับคำขอ บันทึกแบบ append-only (ไม่แก้ไข ไม่ลบ) ใช้สร้าง audit trail และ tracking timeline ที่เห็นใน UI

```sql
CREATE TABLE m_document_transaction (
    id              CHAR(36)        NOT NULL DEFAULT (UUID()),
    request_id      CHAR(36)        NOT NULL,
    action          VARCHAR(50)     NOT NULL,   -- รหัส action (ดู Action Reference)
    from_status     VARCHAR(50)     NULL,       -- status ก่อนเปลี่ยน (NULL = สร้างใหม่)
    to_status       VARCHAR(50)     NOT NULL,   -- status หลังเปลี่ยน
    note            TEXT            NULL,       -- หมายเหตุเพิ่มเติม (optional)
    actioned_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actioned_by     CHAR(36)        NULL,       -- NULL = ระบบ / ลูกเรือ

    PRIMARY KEY (id),
    KEY idx_doctx_request_actioned (request_id, actioned_at),
    KEY idx_doctx_action (action),
    CONSTRAINT chk_doctx_action
        CHECK (action IN (
            'CREATE', 'PAYMENT_SUCCESS', 'SEND_BACK', 'RESUBMIT', 'CHECK_DOCS',
            'SUBMIT_TO_DEPT', 'RECORD_DEPT_RESULT', 'RECEIVE_FROM_DEPT',
            'RECORD_DELIVERY', 'DELIVERY_COMPLETE', 'CANCEL'
        )),
    CONSTRAINT fk_doctx_request
        FOREIGN KEY (request_id) REFERENCES m_document_request (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

**หมายเหตุ:**
- เป็น **append-only table** ห้าม UPDATE/DELETE ในทุกกรณี — แนะนำ revoke สิทธิ์ UPDATE/DELETE บน table นี้ใน production
- `from_status = NULL` หมายถึง action แรก (CREATE)
- ใช้ query ตารางนี้เรียงตาม `actioned_at ASC` เพื่อแสดง timeline tracking ใน UI
- `actioned_by = NULL` ใช้กรณีที่ action เกิดจากลูกเรือ (เช่น RESUBMIT) หรือ scheduled job
- ไม่มี `ON UPDATE CURRENT_TIMESTAMP` บน `actioned_at` เพราะตารางนี้ไม่ควรถูก update เลย

**ตัวอย่าง rows สำหรับ 1 คำขอ:**

| action | from_status | to_status | actioned_by |
|---|---|---|---|
| `CREATE` | NULL | รอตรวจเอกสาร | NULL |
| `SEND_BACK` | รอตรวจเอกสาร | รอผู้ยื่นแก้ไข | superadmin02 |
| `RESUBMIT` | รอผู้ยื่นแก้ไข | รอตรวจเอกสาร | NULL |
| `CHECK_DOCS` | รอตรวจเอกสาร | รอผลกรมเจ้าท่า | superadmin02 |
| `RECORD_DEPT_RESULT` | รอผลกรมเจ้าท่า | รอรับเอกสารจากกรม | superadmin02 |
| `RECEIVE_FROM_DEPT` | รอรับเอกสารจากกรม | รอรับเอกสารจากกรม | superadmin02 |
| `RECORD_DELIVERY` | รอรับเอกสารจากกรม | กำลังจัดส่ง | superadmin02 |
| `DELIVERY_COMPLETE` | กำลังจัดส่ง | จัดส่งสำเร็จ | NULL |

---

## M_DEPT_SUBMISSION > รอรับเอกสาร จากกรมเจ้าท่า

**บทบาท:** เก็บข้อมูลการยื่นเรื่องกับกรมเจ้าท่า (Marine Department) ต่อ 1 คำขอ มีได้ 1 row (one-to-one) เก็บทั้งวันที่ยื่น, วันที่รับเอกสารได้, และวันที่รับเอกสารจริง

```sql
CREATE TABLE m_dept_submission (
    id                      CHAR(36)    NOT NULL DEFAULT (UUID()),
    request_id              CHAR(36)    NOT NULL,
    submitted_to_dept_date  DATE        NOT NULL,   -- วันที่ยื่น Google Form กรมเจ้าท่า
    submitted_by            CHAR(36)    NOT NULL,
    available_from_date     DATE        NULL,       -- วันที่รับเอกสารได้ตั้งแต่ (กรมแจ้งมา)
    received_from_dept_date DATE        NULL,       -- วันที่รับเอกสารจริง
    recorded_at             DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_deptsubmit_request (request_id),  -- บังคับ one-to-one
    KEY idx_deptsubmit_submitted_date (submitted_to_dept_date),
    CONSTRAINT fk_deptsubmit_request
        FOREIGN KEY (request_id) REFERENCES m_document_request (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

**หมายเหตุ:**
- `UNIQUE KEY` บน `request_id` บังคับ one-to-one กับ `m_document_request`
- `available_from_date` เซ็ตตอน action `RECORD_DEPT_RESULT` (กรมแจ้งวันนัด)
- `received_from_dept_date` เซ็ตตอน action `RECEIVE_FROM_DEPT` (admin ไปรับเอกสารจริง)
- แก้ไข `available_from_date` ได้ภายหลัง (UI มีปุ่ม "บันทึกการเปลี่ยนแปลง")

---

## M_DELIVERY_ADDRESS

**บทบาท:** เก็บที่อยู่จัดส่งของผู้ใช้ รองรับหลายที่อยู่ การตั้งค่า default และการปิดใช้งานโดยไม่ลบข้อมูล

```sql
CREATE TABLE m_delivery_address (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    mobile_user_uuid    VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    first_name          VARCHAR(100)    NOT NULL,
    last_name           VARCHAR(100)    NOT NULL,
    address_line        VARCHAR(500)    NOT NULL,   -- บ้านเลขที่, ซอย, หมู่, ถนน
    province            VARCHAR(100)    NOT NULL,
    district            VARCHAR(100)    NOT NULL,   -- เขต/อำเภอ
    sub_district        VARCHAR(100)    NOT NULL,   -- แขวง/ตำบล
    postal_code         VARCHAR(10)     NOT NULL,
    is_default          TINYINT(1)      NOT NULL DEFAULT 0,
    is_active           VARCHAR(3)      NOT NULL DEFAULT 'YES',
    default_owner_uuid  VARCHAR(36) GENERATED ALWAYS AS (
        CASE WHEN is_default = 1 AND is_active = 'YES' THEN mobile_user_uuid ELSE NULL END
    ) STORED,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    KEY idx_delivery_address_mobile_user (mobile_user_uuid, is_active),
    KEY idx_delivery_address_postal_code (postal_code),
    UNIQUE KEY uq_delivery_address_active_default (default_owner_uuid),

    CONSTRAINT chk_delivery_address_default
        CHECK (is_default IN (0, 1)),
    CONSTRAINT chk_delivery_address_active
        CHECK (is_active IN ('YES', 'NO')),
    CONSTRAINT fk_delivery_address_mobile_user
        FOREIGN KEY (mobile_user_uuid) REFERENCES m_mobile_users (MOBILE_UUID)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

**หมายเหตุ:**
- `mobile_user_uuid` ผูกที่อยู่กับผู้ใช้ใน `m_mobile_users`
- `is_default` ระบุที่อยู่หลัก และ `uq_delivery_address_active_default` บังคับให้มี active default ได้ไม่เกินหนึ่งรายการต่อผู้ใช้
- `is_active = 'NO'` ใช้ซ่อนที่อยู่เก่าโดยยังรักษาประวัติการอ้างอิงจาก delivery

---

## M_DOCUMENT_REQUEST_DELIVERY_ADDRESS

**บทบาท:** เก็บ snapshot ที่อยู่จัดส่งต่อ renewal request เพื่อให้คำขอเก่าคงข้อมูลที่อยู่ ณ เวลาสร้าง request แม้ผู้ใช้จะแก้ `m_delivery_address` ภายหลัง

```sql
CREATE TABLE m_document_request_delivery_address (
    id                          CHAR(36)        NOT NULL DEFAULT (UUID()),
    request_id                  CHAR(36)        NOT NULL,
    source_delivery_address_id  CHAR(36)        NULL,
    mobile_user_uuid            VARCHAR(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
    first_name                  VARCHAR(100)    NOT NULL,
    last_name                   VARCHAR(100)    NOT NULL,
    address_line                VARCHAR(500)    NOT NULL,
    province                    VARCHAR(100)    NOT NULL,
    district                    VARCHAR(100)    NOT NULL,
    sub_district                VARCHAR(100)    NOT NULL,
    postal_code                 VARCHAR(10)     NOT NULL,
    mobile_number               VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
    created_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_docreq_delivery_address_request (request_id),
    KEY idx_docreq_delivery_address_source (source_delivery_address_id),
    KEY idx_docreq_delivery_address_user (mobile_user_uuid),

    CONSTRAINT fk_docreq_delivery_address_request
        FOREIGN KEY (request_id) REFERENCES m_document_request (id),
    CONSTRAINT fk_docreq_delivery_address_source
        FOREIGN KEY (source_delivery_address_id) REFERENCES m_delivery_address (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

**หมายเหตุ:**
- `request_id` เป็น one-to-one กับ renewal request
- `source_delivery_address_id` ใช้ trace กลับไปยัง address ต้นทาง แต่ข้อมูลที่ใช้กับ request ต้องอ่านจาก snapshot row นี้
- `mobile_number` snapshot จาก `m_mobile_users.MOBILE_NUMBER` ตอนสร้าง request
- `POST /v1/documents-renewals/requests/validate-and-create` ใช้ default active delivery address ของ user ในการสร้าง snapshot เฉพาะกรณีมี default address; ถ้าไม่มี default address จะสร้าง request ต่อโดยไม่มี snapshot

---

## M_DELIVERY > กำลังจัดส่ง

**บทบาท:** เก็บข้อมูลการจัดส่งเอกสารกลับให้ลูกเรือ เก็บ Tracking Number, วันจัดส่ง, สถานะ tracking จากไปรษณีย์ไทย

```sql
CREATE TABLE m_delivery (
    id                  CHAR(36)        NOT NULL DEFAULT (UUID()),
    request_id          CHAR(36)        NOT NULL,
    delivery_address_id CHAR(36)        NULL,
    tracking_no         VARCHAR(50)     NOT NULL,               -- เช่น 'EF123456789TH'
    carrier             VARCHAR(100)    NOT NULL DEFAULT 'Thailand Post',
    shipped_date        DATE            NOT NULL,               -- วันที่นำส่งไปรษณีย์
    delivery_status     VARCHAR(20)     NOT NULL DEFAULT 'in_transit',
    shipped_recorded_at DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    shipped_by          CHAR(36)        NOT NULL,
    delivered_at        DATETIME        NULL,                   -- NULL = ยังส่งไม่ถึง
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    UNIQUE KEY uq_delivery_request (request_id),    -- บังคับ one-to-one
    KEY idx_delivery_address (delivery_address_id),
    KEY idx_delivery_tracking_no (tracking_no),
    KEY idx_delivery_status_updated (delivery_status, updated_at),
    CONSTRAINT chk_delivery_status
        CHECK (delivery_status IN ('pending', 'in_transit', 'delivered', 'failed', 'returned')),
    CONSTRAINT fk_delivery_request
        FOREIGN KEY (request_id) REFERENCES m_document_request (id),
    CONSTRAINT fk_delivery_address
        FOREIGN KEY (delivery_address_id) REFERENCES m_delivery_address (id)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
```

**หมายเหตุ:**
- `UNIQUE KEY` บน `request_id` บังคับ one-to-one กับ `m_document_request`
- `delivery_address_id` อ้างอิงที่อยู่จัดส่งที่เลือก และอนุญาตให้เป็น `NULL`
- `delivery_status` อัปเดตได้จาก webhook ของ API ไปรษณีย์ไทย หรือ manual โดย admin
- เมื่อ `delivery_status = 'delivered'` ควร trigger update `m_document_request.document_status_id` ให้ชี้ไป status `จัดส่งสำเร็จ` และบันทึก `m_document_transaction` action `DELIVERY_COMPLETE`

---

---

## Status Reference

| Status | ความหมาย | Step |
|---|---|---|
| `รอตรวจเอกสาร` | ลูกเรือยื่นคำขอแล้ว รอ admin ตรวจ | 1 |
| `รอผู้ยื่นแก้ไข` | Admin ส่งกลับให้ลูกเรือแก้ไขเอกสาร | 1 (hold) |
| `รอผลกรมเจ้าท่า` | Admin ยื่นกรมเจ้าท่าแล้ว รอผล | 2 |
| `รอรับเอกสารจากกรม` | กรมอนุมัติแล้ว รอ admin ไปรับ | 3 |
| `กำลังจัดส่ง` | Admin รับเอกสารแล้ว กำลังส่งไปรษณีย์ | 4 |
| `จัดส่งสำเร็จ` | ลูกเรือได้รับเอกสารแล้ว | 5 |
| `ยกเลิก` | คำขอถูกยกเลิก | — |

---

## Action Reference

| Action | จาก Status | ไป Status | ผู้ทำ |
|---|---|---|---|
| `CREATE` | — | `รอตรวจเอกสาร` | System / ลูกเรือ |
| `SEND_BACK` | `รอตรวจเอกสาร` | `รอผู้ยื่นแก้ไข` | Admin |
| `RESUBMIT` | `รอผู้ยื่นแก้ไข` | `รอตรวจเอกสาร` | ลูกเรือ |
| `CHECK_DOCS` | `รอตรวจเอกสาร` | `รอผลกรมเจ้าท่า` | Admin |
| `SUBMIT_TO_DEPT` | `รอผลกรมเจ้าท่า` | `รอผลกรมเจ้าท่า` | Admin |
| `RECORD_DEPT_RESULT` | `รอผลกรมเจ้าท่า` | `รอรับเอกสารจากกรม` | Admin |
| `RECEIVE_FROM_DEPT` | `รอรับเอกสารจากกรม` | `รอรับเอกสารจากกรม` | Admin |
| `RECORD_DELIVERY` | `รอรับเอกสารจากกรม` | `กำลังจัดส่ง` | Admin |
| `DELIVERY_COMPLETE` | `กำลังจัดส่ง` | `จัดส่งสำเร็จ` | System / Admin |
| `CANCEL` | ทุก status | `ยกเลิก` | Admin |

*Smart Seaman Schema — MySQL 8.0+ Edition · v2.0*
