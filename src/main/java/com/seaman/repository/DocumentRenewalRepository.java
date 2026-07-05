package com.seaman.repository;

import com.seaman.entity.DocumentRenewalPriceEntity;
import com.seaman.entity.DocumentRenewalStatusEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DocumentRenewalRepository extends CommonRepository {

    public List<DocumentRenewalStatusEntity> findActiveStatuses() {
        String sql = "SELECT id, name_th, name_en, css_color FROM m_document_status "
                + "WHERE is_active = 'YES' AND is_mobile_visible = 'YES' ORDER BY CASE name_th "
                + "WHEN 'รอตรวจเอกสาร' THEN 1 WHEN 'รอผู้ยื่นแก้ไข' THEN 2 "
                + "WHEN 'รอผลกรมเจ้าท่า' THEN 3 WHEN 'รอรับเอกสารจากกรม' THEN 4 "
                + "WHEN 'กำลังจัดส่ง' THEN 5 WHEN 'จัดส่งสำเร็จ' THEN 6 "
                + "WHEN 'ยกเลิก' THEN 7 ELSE 99 END, name_th";
        return template.query(sql, new MapSqlParameterSource(),
                new BeanPropertyRowMapper<>(DocumentRenewalStatusEntity.class));
    }

    public List<DocumentRenewalPriceEntity> findActivePrices(String documentCode) {
        String sql = "SELECT p.id AS price_setting_id, p.document_code, p.government_fee, p.document_processing_fee, "
                + "p.shipping_fee, p.shipping_discount, p.service_fee_discount "
                + ", p.effective_from, p.effective_to "
                + "FROM m_document_prices_setting p JOIN m_documents d "
                + "ON d.DOCUMENT_CODE = p.document_code "
                + "WHERE p.document_code = :documentCode AND p.is_active = 'YES' "
                + "AND p.effective_from <= CURRENT_DATE "
                + "AND (p.effective_to IS NULL OR p.effective_to >= CURRENT_DATE) "
                + "AND d.DOCUMENT_STATUS = 'A' ORDER BY p.effective_from DESC";
        return template.query(sql, new MapSqlParameterSource("documentCode", documentCode),
                new BeanPropertyRowMapper<>(DocumentRenewalPriceEntity.class));
    }
}
