package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRequestItemEntity;
import com.seaman.exception.BusinessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DocumentRenewalCreateRepository extends CommonRepository {

    public int countActiveRequiredItems(String documentCode) {
        Integer count = template.queryForObject(
                "SELECT COUNT(*) FROM m_document_setting_requires "
                        + "WHERE document_code = :documentCode AND is_required = 1 AND is_active = 'YES'",
                new MapSqlParameterSource("documentCode", documentCode), Integer.class);
        return count == null ? 0 : count;
    }

    public String nextRequestNo(String period) {
        template.update("INSERT INTO m_document_request_sequence (period, last_number) "
                        + "VALUES (:period, 1) ON DUPLICATE KEY UPDATE last_number = last_number + 1",
                new MapSqlParameterSource("period", period));
        Integer number = template.queryForObject(
                "SELECT last_number FROM m_document_request_sequence "
                        + "WHERE period = :period FOR UPDATE",
                new MapSqlParameterSource("period", period), Integer.class);
        if (number == null || number > 99999) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "requestNumberSequence");
        }
        return period + String.format("%05d", number);
    }

    public DocumentRenewalRequestEntity findLatestActiveRequestNotDelivered(
            String mobileUserUuid, String documentCode) {
        List<DocumentRenewalRequestEntity> rows = template.query(
                "SELECT r.*, s.document_status_code AS status_code, "
                        + "s.name_en AS status_name_en, s.name_th AS status_name_th, "
                        + "s.css_color AS status_css_color "
                        + "FROM m_document_request r "
                        + "INNER JOIN m_document_status s ON s.id = r.document_status_id "
                        + "WHERE r.mobile_user_uuid = :mobileUserUuid "
                        + "AND r.document_code = :documentCode "
                        + "AND r.is_active = 'YES' "
                        + "AND s.document_status_code <> 'DELIVERED' "
                        + "ORDER BY r.submitted_at DESC, r.id DESC LIMIT 1",
                new MapSqlParameterSource().addValue("mobileUserUuid", mobileUserUuid)
                        .addValue("documentCode", documentCode),
                new BeanPropertyRowMapper<>(DocumentRenewalRequestEntity.class));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<DocumentRequestItemEntity> findRequestItemsForValidate(
            String requestId, String mobileUserUuid) {
        String sql = "SELECT i.id, r.mobile_user_uuid, r.document_code, "
                + "i.document_master_request_item_code, m.storage_scope, "
                + "m.document_master_items_name AS document_name, "
                + "CASE "
                + "WHEN i.approve_status = 'FIX' THEN 'NEED_FIX' "
                + "WHEN i.approve_status = 'PASS' THEN 'COMPLETE' "
                + "WHEN m.storage_scope = 'REQUEST' AND i.document_master_request_item_code = 'MRI001' AND ("
                + "((SELECT COUNT(DISTINCT f.slot_code) FROM m_document_request_item_files f "
                + "WHERE f.request_item_id = i.id AND f.document_type = 'ID_CARD' "
                + "AND f.slot_code IN ('FRONT','BACK') AND f.file_uploaded = 1 "
                + "AND (f.check_result IS NULL OR f.check_result <> 'fix')) = 2) "
                + "OR EXISTS (SELECT 1 FROM m_document_request_item_files f "
                + "WHERE f.request_item_id = i.id AND f.document_type = 'ID_CARD' "
                + "AND f.slot_code = 'MAIN' AND f.file_uploaded = 1 "
                + "AND (f.check_result IS NULL OR f.check_result <> 'fix')) "
                + "OR EXISTS (SELECT 1 FROM m_document_request_item_files f "
                + "WHERE f.request_item_id = i.id AND f.document_type = 'PASSPORT' "
                + "AND f.slot_code = 'MAIN' AND f.file_uploaded = 1 "
                + "AND (f.check_result IS NULL OR f.check_result <> 'fix'))) THEN 'COMPLETE' "
                + "WHEN m.storage_scope = 'REQUEST' AND i.document_master_request_item_code <> 'MRI001' "
                + "AND EXISTS (SELECT 1 FROM m_document_request_item_files f "
                + "WHERE f.request_item_id = i.id AND f.document_type = 'GENERAL' "
                + "AND f.slot_code = 'MAIN' AND f.file_uploaded = 1 "
                + "AND (f.check_result IS NULL OR f.check_result <> 'fix')) THEN 'COMPLETE' "
                + "WHEN m.storage_scope = 'PROFILE' THEN 'COMPLETE' "
                + "ELSE 'MISSING' END AS document_status, "
                + "m.sort_order, "
                + "CASE WHEN EXISTS (SELECT 1 FROM m_document_request_item_files f "
                + "WHERE f.request_item_id = i.id AND f.file_uploaded = 1) THEN 1 ELSE 0 END AS file_uploaded, "
                + "i.note AS check_note "
                + "FROM m_document_request_items i "
                + "INNER JOIN m_document_request r ON r.id = i.request_id "
                + "INNER JOIN m_document_master_request_item m "
                + "ON m.document_master_items_code = i.document_master_request_item_code "
                + "WHERE i.request_id = :requestId "
                + "AND r.mobile_user_uuid = :mobileUserUuid "
                + "AND r.is_active = 'YES' "
                + "ORDER BY m.sort_order, i.id";
        return template.query(sql,
                new MapSqlParameterSource().addValue("requestId", requestId)
                        .addValue("mobileUserUuid", mobileUserUuid),
                new BeanPropertyRowMapper<>(DocumentRequestItemEntity.class));
    }

    public void insertRequest(String id, String requestNo, String mobileUserUuid,
                              String documentCode, String statusId, String priceSettingId,
                              String deliveryAddressId, java.math.BigDecimal amount) {
        int rows = template.update("INSERT INTO m_document_request "
                        + "(id, request_no, mobile_user_uuid, document_code, document_status_id, "
                        + "price_setting_id, delivery_address_id, is_active, amount) "
                        + "VALUES (:id, :requestNo, :mobileUserUuid, :documentCode, :statusId, "
                        + ":priceSettingId, :deliveryAddressId, 'YES', :amount)",
                new MapSqlParameterSource().addValue("id", id).addValue("requestNo", requestNo)
                        .addValue("mobileUserUuid", mobileUserUuid).addValue("documentCode", documentCode)
                        .addValue("statusId", statusId).addValue("priceSettingId", priceSettingId)
                        .addValue("deliveryAddressId", deliveryAddressId).addValue("amount", amount));
        requireOne(rows, "documentRenewalRequest");
    }

    public int insertRequestItems(String requestId, String documentCode) {
        return template.update("INSERT INTO m_document_request_items "
                        + "(id, request_id, request_no, document_master_request_item_code, "
                        + "approve_status) "
                        + "SELECT UUID(), r.id, r.request_no, s.document_master_request_item_code, "
                        + "'PENDING' FROM m_document_request r "
                        + "INNER JOIN m_document_setting_requires s "
                        + "ON s.document_code = :documentCode "
                        + "AND s.is_required = 1 AND s.is_active = 'YES' "
                        + "WHERE r.id = :requestId",
                new MapSqlParameterSource().addValue("requestId", requestId)
                        .addValue("documentCode", documentCode));
    }

    private void requireOne(int rows, String resource) {
        if (rows != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, resource);
        }
    }
}
