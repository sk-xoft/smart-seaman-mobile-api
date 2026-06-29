INSERT INTO m_document_status (
    name_th,
    name_en,
    css_color,
    is_active
) VALUES
    ('รอตรวจเอกสาร', 'Pending Document Review', '#ff0000', 'YES'),
    ('รอผู้ยื่นแก้ไข', 'Pending Applicant Correction', '#ff914d', 'YES'),
    ('รอผลกรมเจ้าท่า', 'Pending Marine Department Result', '#af87ff', 'YES'),
    ('รอรับเอกสารจากกรม', 'Pending Department Document Pickup', '#ffde59', 'YES'),
    ('กำลังจัดส่ง', 'Delivering', '#21e5f8', 'YES'),
    ('จัดส่งสำเร็จ', 'Delivered', '#00bf63', 'YES'),
    ('ยกเลิก', 'Cancelled', '#ff5eb3', 'YES')
ON DUPLICATE KEY UPDATE
    name_en = VALUES(name_en),
    css_color = VALUES(css_color),
    is_active = VALUES(is_active);

INSERT INTO m_document_master_request_item (
    document_master_items_code,
    document_master_items_name,
    sort_order,
    is_active
) VALUES
    ('MRI001', 'สำเนาบัตรประชาชน / Passport', 1, 'YES'),
    ('MRI002', 'รูปถ่าย (ขนาด 2 นิ้ว)', 2, 'YES'),
    ('MRI003', 'ใบรับรองแพทย์', 3, 'YES'),
    ('MRI004', 'Sea Service Record', 4, 'YES')
ON DUPLICATE KEY UPDATE
    document_master_items_name = VALUES(document_master_items_name),
    sort_order = VALUES(sort_order),
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
