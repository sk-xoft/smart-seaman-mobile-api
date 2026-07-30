INSERT INTO m_document_setting_requires (
    id,
    document_code,
    document_master_request_item_code,
    sort_order,
    is_required,
    is_active,
    created_at,
    updated_at
)
SELECT
    UUID(),
    d.document_code,
    i.document_master_request_item_code,
    i.sort_order,
    1,
    'YES',
    NOW(),
    NOW()
FROM (
    SELECT 'DOC006' AS document_code UNION ALL
    SELECT 'DOC007' UNION ALL
    SELECT 'DOC008' UNION ALL
    SELECT 'DOC009' UNION ALL
    SELECT 'DOC010' UNION ALL
    SELECT 'DOC011' UNION ALL
    SELECT 'DOC012' UNION ALL
    SELECT 'DOC013' UNION ALL
    SELECT 'DOC014' UNION ALL
    SELECT 'DOC015' UNION ALL
    SELECT 'DOC016' UNION ALL
    SELECT 'DOC017' UNION ALL
    SELECT 'DOC018' UNION ALL
    SELECT 'DOC019' UNION ALL
    SELECT 'DOC020' UNION ALL
    SELECT 'DOC021' UNION ALL
    SELECT 'DOC022' UNION ALL
    SELECT 'DOC023' UNION ALL
    SELECT 'DOC024'
) d
CROSS JOIN (
    SELECT 'MRI001' AS document_master_request_item_code, 1 AS sort_order UNION ALL
    SELECT 'MRI002', 2 UNION ALL
    SELECT 'MRI003', 3 UNION ALL
    SELECT 'MRI004', 4
) i
WHERE NOT EXISTS (
    SELECT 1
    FROM m_document_setting_requires x
    WHERE x.document_code = d.document_code
      AND x.document_master_request_item_code = i.document_master_request_item_code
);
