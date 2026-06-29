ALTER TABLE m_document_setting_requires
    ADD INDEX idx_doc_setting_requires_validate (
        document_code,
        is_required,
        is_active,
        sort_order,
        document_master_request_item_code
    );
