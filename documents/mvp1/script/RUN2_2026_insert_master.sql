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
