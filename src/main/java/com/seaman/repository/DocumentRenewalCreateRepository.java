package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.DeliveryAddressEntity;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRequestItemEntity;
import com.seaman.exception.BusinessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

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

    public DocumentRenewalRequestEntity findByIdempotencyKey(
            String mobileUserUuid, String idempotencyKey) {
        List<DocumentRenewalRequestEntity> rows = template.query(
                "SELECT r.*, s.document_status_code AS status_code, "
                        + "s.name_en AS status_name_en, s.name_th AS status_name_th, "
                        + "s.css_color AS status_css_color "
                        + "FROM m_document_request r "
                        + "INNER JOIN m_document_status s ON s.id = r.document_status_id "
                        + "WHERE r.mobile_user_uuid = :mobileUserUuid "
                        + "AND r.idempotency_key = :idempotencyKey "
                        + "AND r.is_active = 'YES' "
                        + "ORDER BY r.created_at DESC, r.id DESC LIMIT 1",
                new MapSqlParameterSource().addValue("mobileUserUuid", mobileUserUuid)
                        .addValue("idempotencyKey", idempotencyKey),
                new BeanPropertyRowMapper<>(DocumentRenewalRequestEntity.class));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<DocumentRequestItemEntity> findRequestItemsForValidate(
            String requestId, String mobileUserUuid) {
        String sql = "SELECT i.id, r.mobile_user_uuid, r.document_code, "
                + "p.id AS profile_request_item_id, "
                + "i.document_master_request_item_code, m.storage_scope, "
                + "COALESCE(rf.document_type, p.document_type) AS document_type, "
                + "m.document_master_items_name AS document_name, "
                + "CASE "
                // request-scoped file (uploaded via the renewal item-file endpoint) always wins first,
                // regardless of the master item's storage_scope
                + "WHEN i.document_master_request_item_code = 'MRI001' "
                + "AND (COALESCE(rs.valid_id_front_back_slots, 0) = 2 "
                + "OR COALESCE(rs.valid_id_main, 0) > 0 "
                + "OR COALESCE(rs.valid_passport_main, 0) > 0) THEN 'COMPLETE' "
                + "WHEN i.document_master_request_item_code <> 'MRI001' "
                + "AND COALESCE(rs.valid_general_main, 0) > 0 THEN 'COMPLETE' "
                + "WHEN COALESCE(rs.fix_count, 0) > 0 THEN 'NEED_FIX' "
                + "WHEN COALESCE(rs.uploaded_count, 0) > 0 THEN 'INCOMPLETE' "
                // no request-scoped file yet; fall back to profile prefill for PROFILE-scope items
                + "WHEN m.storage_scope = 'PROFILE' AND i.document_master_request_item_code = 'MRI001' "
                + "AND (COALESCE(ps.valid_id_front_back_slots, 0) = 2 "
                + "OR COALESCE(ps.valid_id_main, 0) > 0 "
                + "OR COALESCE(ps.valid_passport_main, 0) > 0) THEN 'COMPLETE' "
                + "WHEN m.storage_scope = 'PROFILE' AND i.document_master_request_item_code <> 'MRI001' "
                + "AND COALESCE(ps.valid_general_main, 0) > 0 THEN 'COMPLETE' "
                + "WHEN m.storage_scope = 'PROFILE' AND COALESCE(ps.fix_count, 0) > 0 THEN 'NEED_FIX' "
                + "WHEN m.storage_scope = 'PROFILE' AND p.id IS NULL THEN 'MISSING' "
                + "WHEN m.storage_scope = 'PROFILE' AND COALESCE(ps.uploaded_count, 0) = 0 THEN 'NOT_UPLOADED' "
                + "WHEN m.storage_scope = 'PROFILE' THEN 'INCOMPLETE' "
                + "WHEN i.approve_status = 'FIX' THEN 'NEED_FIX' "
                + "WHEN i.approve_status = 'PASS' THEN 'COMPLETE' "
                + "ELSE 'MISSING' END AS document_status, "
                + "m.sort_order, "
                + "CASE WHEN COALESCE(rs.uploaded_count, 0) > 0 THEN 1 "
                + "WHEN m.storage_scope = 'PROFILE' AND COALESCE(ps.uploaded_count, 0) > 0 THEN 1 "
                + "ELSE 0 END AS file_uploaded, "
                + "CASE WHEN COALESCE(rs.uploaded_count, 0) > 0 THEN 1 ELSE 0 END "
                + "AS request_item_file_uploaded, "
                + "CASE WHEN COALESCE(rs.uploaded_count, 0) > 0 THEN rf.file_path "
                + "WHEN m.storage_scope = 'PROFILE' THEN p.file_path ELSE NULL END AS file_path, "
                + "CASE WHEN COALESCE(rs.uploaded_count, 0) > 0 THEN rf.file_uploaded_at "
                + "WHEN m.storage_scope = 'PROFILE' THEN p.file_uploaded_at ELSE NULL END AS file_uploaded_at, "
                + "CASE WHEN COALESCE(rs.uploaded_count, 0) > 0 THEN rf.check_result "
                + "WHEN m.storage_scope = 'PROFILE' THEN p.check_result ELSE NULL END AS check_result, "
                + "CASE WHEN COALESCE(rs.uploaded_count, 0) > 0 THEN rf.check_note "
                + "WHEN m.storage_scope = 'PROFILE' THEN p.check_note ELSE i.note END AS check_note "
                + "FROM m_document_request_items i "
                + "INNER JOIN m_document_request r ON r.id = i.request_id "
                + "INNER JOIN m_document_master_request_item m "
                + "ON m.document_master_items_code = i.document_master_request_item_code "
                + "LEFT JOIN (SELECT mobile_user_uuid, document_master_request_item_code, "
                + "MIN(id) AS profile_item_id FROM m_document_profile_request_item "
                + "WHERE mobile_user_uuid = :mobileUserUuid "
                + "GROUP BY mobile_user_uuid, document_master_request_item_code) pmin "
                + "ON pmin.mobile_user_uuid = :mobileUserUuid "
                + "AND pmin.document_master_request_item_code = i.document_master_request_item_code "
                + "LEFT JOIN m_document_profile_request_item p ON p.id = pmin.profile_item_id "
                + "LEFT JOIN (SELECT mobile_user_uuid, document_master_request_item_code, "
                + "COUNT(DISTINCT CASE WHEN document_type = 'ID_CARD' "
                + "AND slot_code IN ('FRONT','BACK') AND file_uploaded = 1 "
                + "AND (check_result IS NULL OR check_result <> 'fix') THEN slot_code END) "
                + "AS valid_id_front_back_slots, "
                + "SUM(CASE WHEN document_type = 'ID_CARD' AND slot_code = 'MAIN' "
                + "AND file_uploaded = 1 AND (check_result IS NULL OR check_result <> 'fix') "
                + "THEN 1 ELSE 0 END) AS valid_id_main, "
                + "SUM(CASE WHEN document_type = 'PASSPORT' AND slot_code = 'MAIN' "
                + "AND file_uploaded = 1 AND (check_result IS NULL OR check_result <> 'fix') "
                + "THEN 1 ELSE 0 END) AS valid_passport_main, "
                + "SUM(CASE WHEN document_type = 'GENERAL' AND slot_code = 'MAIN' "
                + "AND file_uploaded = 1 AND (check_result IS NULL OR check_result <> 'fix') "
                + "THEN 1 ELSE 0 END) AS valid_general_main, "
                + "SUM(CASE WHEN check_result = 'fix' THEN 1 ELSE 0 END) AS fix_count, "
                + "SUM(CASE WHEN file_uploaded = 1 THEN 1 ELSE 0 END) AS uploaded_count "
                + "FROM m_document_profile_request_item WHERE mobile_user_uuid = :mobileUserUuid "
                + "GROUP BY mobile_user_uuid, document_master_request_item_code) ps "
                + "ON ps.mobile_user_uuid = :mobileUserUuid "
                + "AND ps.document_master_request_item_code = i.document_master_request_item_code "
                + "LEFT JOIN (SELECT request_item_id, MIN(id) AS request_file_id "
                + "FROM m_document_request_item_files WHERE request_item_id IN ("
                + "SELECT id FROM m_document_request_items WHERE request_id = :requestId) "
                + "GROUP BY request_item_id) rfmin ON rfmin.request_item_id = i.id "
                + "LEFT JOIN m_document_request_item_files rf ON rf.id = rfmin.request_file_id "
                + "LEFT JOIN (SELECT request_item_id, "
                + "COUNT(DISTINCT CASE WHEN document_type = 'ID_CARD' "
                + "AND slot_code IN ('FRONT','BACK') AND file_uploaded = 1 "
                + "AND (check_result IS NULL OR check_result <> 'fix') THEN slot_code END) "
                + "AS valid_id_front_back_slots, "
                + "SUM(CASE WHEN document_type = 'ID_CARD' AND slot_code = 'MAIN' "
                + "AND file_uploaded = 1 AND (check_result IS NULL OR check_result <> 'fix') "
                + "THEN 1 ELSE 0 END) AS valid_id_main, "
                + "SUM(CASE WHEN document_type = 'PASSPORT' AND slot_code = 'MAIN' "
                + "AND file_uploaded = 1 AND (check_result IS NULL OR check_result <> 'fix') "
                + "THEN 1 ELSE 0 END) AS valid_passport_main, "
                + "SUM(CASE WHEN document_type = 'GENERAL' AND slot_code = 'MAIN' "
                + "AND file_uploaded = 1 AND (check_result IS NULL OR check_result <> 'fix') "
                + "THEN 1 ELSE 0 END) AS valid_general_main, "
                + "SUM(CASE WHEN check_result = 'fix' THEN 1 ELSE 0 END) AS fix_count, "
                + "SUM(CASE WHEN file_uploaded = 1 THEN 1 ELSE 0 END) AS uploaded_count "
                + "FROM m_document_request_item_files WHERE request_item_id IN ("
                + "SELECT id FROM m_document_request_items WHERE request_id = :requestId) "
                + "GROUP BY request_item_id) rs ON rs.request_item_id = i.id "
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
                              String mobileNumber, String email,
                              String documentCode, String statusId, String priceSettingId,
                              String deliveryAddressId, java.math.BigDecimal amount,
                              String idempotencyKey) {
        int rows = template.update("INSERT INTO m_document_request "
                        + "(id, request_no, mobile_user_uuid, mobile_number, email, "
                        + "document_code, document_status_id, "
                        + "price_setting_id, delivery_address_id, is_active, amount, "
                        + "idempotency_key) "
                        + "VALUES (:id, :requestNo, :mobileUserUuid, :mobileNumber, :email, "
                        + ":documentCode, :statusId, "
                        + ":priceSettingId, :deliveryAddressId, 'YES', :amount, "
                        + ":idempotencyKey)",
                new MapSqlParameterSource().addValue("id", id).addValue("requestNo", requestNo)
                        .addValue("mobileUserUuid", mobileUserUuid).addValue("mobileNumber", mobileNumber)
                        .addValue("email", email).addValue("documentCode", documentCode)
                        .addValue("statusId", statusId).addValue("priceSettingId", priceSettingId)
                        .addValue("deliveryAddressId", deliveryAddressId).addValue("amount", amount)
                        .addValue("idempotencyKey", idempotencyKey));
        requireOne(rows, "documentRenewalRequest");
    }

    public String insertDeliveryAddressSnapshot(
            String requestId, DeliveryAddressEntity address, String mobileNumber) {
        String snapshotId = UUID.randomUUID().toString();
        int rows = template.update("INSERT INTO m_document_request_delivery_address "
                        + "(id, request_id, source_delivery_address_id, mobile_user_uuid, "
                        + "first_name, last_name, address_line, province, district, "
                        + "sub_district, postal_code, mobile_number) VALUES "
                        + "(:id, :requestId, :sourceDeliveryAddressId, :mobileUserUuid, "
                        + ":firstName, :lastName, :addressLine, :province, :district, "
                        + ":subDistrict, :postalCode, :mobileNumber)",
                new MapSqlParameterSource().addValue("id", snapshotId)
                        .addValue("requestId", requestId)
                        .addValue("sourceDeliveryAddressId", address.getId())
                        .addValue("mobileUserUuid", address.getMobileUserUuid())
                        .addValue("firstName", address.getFirstName())
                        .addValue("lastName", address.getLastName())
                        .addValue("addressLine", address.getAddressLine())
                        .addValue("province", address.getProvince())
                        .addValue("district", address.getDistrict())
                        .addValue("subDistrict", address.getSubDistrict())
                        .addValue("postalCode", address.getPostalCode())
                        .addValue("mobileNumber", mobileNumber));
        requireOne(rows, "documentRenewalDeliveryAddressSnapshot");
        return snapshotId;
    }

    public int countDeliveryAddressSnapshot(String requestId, String mobileUserUuid) {
        Integer count = template.queryForObject(
                "SELECT COUNT(*) FROM m_document_request_delivery_address "
                        + "WHERE request_id = :requestId AND mobile_user_uuid = :mobileUserUuid",
                new MapSqlParameterSource().addValue("requestId", requestId)
                        .addValue("mobileUserUuid", mobileUserUuid),
                Integer.class);
        return count == null ? 0 : count;
    }

    public List<DeliveryAddressEntity> findDeliveryAddressSnapshot(
            String requestId, String mobileUserUuid) {
        String sql = "SELECT a.id, a.mobile_user_uuid, "
                + "a.first_name, a.last_name, a.address_line, a.province, a.district, "
                + "a.sub_district, a.postal_code, a.mobile_number, "
                + "CONCAT_WS(' ', a.address_line, "
                + "CONCAT('ตำบล', sd.name_in_thai), "
                + "CONCAT('อำเภอ', d.name_in_thai), "
                + "CONCAT('จังหวัด', p.name_in_thai), a.postal_code) AS description "
                + "FROM m_document_request_delivery_address a "
                + "LEFT JOIN provinces p ON p.code = a.province "
                + "LEFT JOIN districts d ON d.code = a.district "
                + "LEFT JOIN subdistricts sd ON sd.code = a.sub_district "
                + "WHERE a.request_id = :requestId AND a.mobile_user_uuid = :mobileUserUuid "
                + "ORDER BY a.created_at DESC, a.id DESC";
        return template.query(sql,
                new MapSqlParameterSource().addValue("requestId", requestId)
                        .addValue("mobileUserUuid", mobileUserUuid),
                new BeanPropertyRowMapper<>(DeliveryAddressEntity.class));
    }

    public DeliveryAddressEntity findDeliveryAddressSnapshotById(String id, String mobileUserUuid) {
        List<DeliveryAddressEntity> rows = template.query(
                "SELECT a.id, a.mobile_user_uuid, a.first_name, a.last_name, a.address_line, "
                        + "a.province, a.district, a.sub_district, a.postal_code, a.mobile_number "
                        + "FROM m_document_request_delivery_address a "
                        + "WHERE a.id = :id AND a.mobile_user_uuid = :mobileUserUuid",
                new MapSqlParameterSource().addValue("id", id)
                        .addValue("mobileUserUuid", mobileUserUuid),
                new BeanPropertyRowMapper<>(DeliveryAddressEntity.class));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void updateDeliveryAddressSnapshot(DeliveryAddressEntity entity) {
        int rows = template.update("UPDATE m_document_request_delivery_address "
                        + "SET first_name = :firstName, last_name = :lastName, "
                        + "address_line = :addressLine, province = :province, district = :district, "
                        + "sub_district = :subDistrict, postal_code = :postalCode "
                        + "WHERE id = :id AND mobile_user_uuid = :mobileUserUuid",
                new MapSqlParameterSource().addValue("id", entity.getId())
                        .addValue("mobileUserUuid", entity.getMobileUserUuid())
                        .addValue("firstName", entity.getFirstName())
                        .addValue("lastName", entity.getLastName())
                        .addValue("addressLine", entity.getAddressLine())
                        .addValue("province", entity.getProvince())
                        .addValue("district", entity.getDistrict())
                        .addValue("subDistrict", entity.getSubDistrict())
                        .addValue("postalCode", entity.getPostalCode()));
        requireOne(rows, "documentRenewalDeliveryAddressSnapshot");
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
