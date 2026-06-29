# Validate Document Items By Profiles And Documents

## Action

request
- `document_code`
- `mobile_user_uuid`

logic
1. ตรวจสอบว่า document นั้นต้องการเอกสารย่อยอะไรบ้างจาก `m_document_setting_requires`
2. ตรวจสอบว่า user มีเอกสารครบตาม requirement หรือไม่จาก `m_document_profile_request_item`
3. ถ้าไม่พบ profile item, ยังไม่ได้ upload, หรือผลตรวจเป็น `fix` ให้ถือว่ายังไม่ครบ/ต้องแก้ไข

## Query

Query ตรวจสอบสถานะเอกสารตาม requirement:

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

Query ดึงเฉพาะเอกสารที่ยังไม่ครบหรือต้องแก้ไข:

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
    END AS document_status,
    dpri.id AS profile_request_item_id,
    COALESCE(dpri.file_uploaded, 0) AS file_uploaded,
    dpri.check_result,
    dpri.check_note,
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
  AND (
      dpri.id IS NULL
      OR dpri.file_uploaded = 0
      OR dpri.check_result = 'fix'
  )
ORDER BY dsr.sort_order ASC;
```

## Index

- `m_document_setting_requires`: เพิ่ม composite index สำหรับ filter และ sort ของ validation query
- `m_document_profile_request_item`: ใช้ `UNIQUE (mobile_user_uuid, document_master_request_item_code)` ที่มีอยู่แล้วใน create table สำหรับ `LEFT JOIN`
