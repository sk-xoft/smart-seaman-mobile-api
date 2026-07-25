INSERT INTO m_document_status (
    document_status_code,
    name_th,
    name_en,
    css_color,
    is_active,
    is_mobile_visible
) VALUES
    ('PAYMENT_PENDING', 'รอชำระเงิน', 'Payment Pending', '#999999', 'YES', 'NO'),
    ('PENDING_DOCUMENT_REVIEW', 'รอตรวจเอกสาร', 'Pending Document Review', '#ff0000', 'YES', 'YES'),
    ('PENDING_APPLICANT_CORRECTION', 'รอผู้ยื่นแก้ไข', 'Pending Applicant Correction', '#ff914d', 'YES', 'YES'),
    ('PENDING_MARINE_DEPARTMENT_RESULT', 'รอผลกรมเจ้าท่า', 'Pending Marine Department Result', '#af87ff', 'YES', 'YES'),
    ('PENDING_DEPARTMENT_DOCUMENT_PICKUP', 'รอรับเอกสารจากกรม', 'Pending Department Document Pickup', '#ffde59', 'YES', 'YES'),
    ('DELIVERING', 'กำลังจัดส่ง', 'Delivering', '#21e5f8', 'YES', 'YES'),
    ('DELIVERED', 'จัดส่งสำเร็จ', 'Delivered', '#00bf63', 'YES', 'YES'),
    ('CANCELLED', 'ยกเลิก', 'Cancelled', '#ff5eb3', 'YES', 'YES')
ON DUPLICATE KEY UPDATE
    document_status_code = VALUES(document_status_code),
    name_en = VALUES(name_en),
    css_color = VALUES(css_color),
    is_active = VALUES(is_active),
    is_mobile_visible = VALUES(is_mobile_visible);

INSERT INTO m_document_master_request_item (
    document_master_items_code,
    document_master_items_name,
    sort_order,
    storage_scope,
    is_active
) VALUES
    ('MRI001', 'สำเนาบัตรประชาชน / Passport', 1, 'PROFILE', 'YES'),
    ('MRI002', 'รูปถ่าย (ขนาด 2 นิ้ว)', 2, 'PROFILE', 'YES'),
    ('MRI003', 'ใบรับรองแพทย์', 3, 'PROFILE', 'YES'),
    ('MRI004', 'Sea Service Record', 4, 'REQUEST', 'YES')
ON DUPLICATE KEY UPDATE
    document_master_items_name = VALUES(document_master_items_name),
    sort_order = VALUES(sort_order),
    storage_scope = VALUES(storage_scope),
    is_active = VALUES(is_active);

INSERT INTO m_document_setting_requires (
    id,
    document_code,
    document_master_request_item_code,
    sort_order,
    is_required,
    is_active,
    created_at,
    updated_at
) VALUES
    ('b9151ac8-73d9-11f1-bfe7-ee0327299703', 'DOC001', 'MRI001', 1, 1, 'YES', '2026-06-29 16:43:57', '2026-06-29 16:43:57'),
    ('b9243e97-73d9-11f1-bfe7-ee0327299703', 'DOC001', 'MRI002', 2, 1, 'YES', '2026-06-29 16:43:57', '2026-06-29 16:43:57'),
    ('b932d7d2-73d9-11f1-bfe7-ee0327299703', 'DOC001', 'MRI003', 3, 1, 'YES', '2026-06-29 16:43:57', '2026-06-29 16:43:57'),
    ('b93e8758-73d9-11f1-bfe7-ee0327299703', 'DOC001', 'MRI004', 4, 1, 'YES', '2026-06-29 16:43:57', '2026-06-29 16:43:57')
ON DUPLICATE KEY UPDATE
    sort_order = VALUES(sort_order),
    is_required = VALUES(is_required),
    is_active = VALUES(is_active),
    updated_at = VALUES(updated_at);
