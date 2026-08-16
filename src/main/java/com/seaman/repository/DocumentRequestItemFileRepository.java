package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.DocumentRequestItemEntity;
import com.seaman.entity.DocumentRequestItemFileEntity;
import com.seaman.exception.BusinessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class DocumentRequestItemFileRepository extends CommonRepository {
    public boolean isActiveItem(String itemCode) {
        Integer count = template.queryForObject(
                "SELECT COUNT(*) FROM m_document_master_request_item WHERE document_master_items_code = :itemCode AND is_active = 'YES'",
                new MapSqlParameterSource("itemCode", itemCode), Integer.class);
        return count != null && count == 1;
    }

    public List<DocumentRequestItemFileEntity> findFiles(String mobileUserUuid, String itemCode) {
        return template.query("SELECT id, id AS profile_request_item_id, document_type, slot_code, "
                        + "file_path AS storage_key, original_file_name, mime_type, file_size, file_uploaded, file_uploaded_at, "
                        + "check_result, check_note, is_updated FROM m_document_profile_request_item "
                        + "WHERE mobile_user_uuid = :mobileUserUuid AND document_master_request_item_code = :itemCode "
                        + "AND document_type IS NOT NULL ORDER BY document_type, slot_code",
                params(mobileUserUuid, itemCode, null, null),
                new BeanPropertyRowMapper<>(DocumentRequestItemFileEntity.class));
    }

    public List<DocumentRequestItemFileEntity> findFilesByItemCodes(
            String mobileUserUuid, List<String> itemCodes) {
        if (itemCodes == null || itemCodes.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return template.query("SELECT id, id AS profile_request_item_id, "
                        + "document_master_request_item_code, document_type, slot_code, "
                        + "file_path AS storage_key, original_file_name, mime_type, file_size, "
                        + "file_uploaded, file_uploaded_at, check_result, check_note, is_updated "
                        + "FROM m_document_profile_request_item "
                        + "WHERE mobile_user_uuid = :mobileUserUuid "
                        + "AND document_master_request_item_code IN (:itemCodes) "
                        + "AND document_type IS NOT NULL "
                        + "ORDER BY document_master_request_item_code, document_type, slot_code",
                new MapSqlParameterSource().addValue("mobileUserUuid", mobileUserUuid)
                        .addValue("itemCodes", itemCodes),
                new BeanPropertyRowMapper<>(DocumentRequestItemFileEntity.class));
    }

    public List<DocumentRequestItemEntity> findProfileItems(String mobileUserUuid) {
        String sql = "SELECT m.id, :mobileUserUuid AS mobile_user_uuid, "
                + "m.document_master_items_code AS document_master_request_item_code, "
                + "m.storage_scope, p.document_type, "
                + "m.document_master_items_name AS document_name, "
                + "CASE "
                + "WHEN m.document_master_items_code = 'MRI001' "
                + "AND (COALESCE(ps.valid_id_front_back_slots, 0) = 2 "
                + "OR COALESCE(ps.valid_id_main, 0) > 0 "
                + "OR COALESCE(ps.valid_passport_main, 0) > 0) THEN 'COMPLETE' "
                + "WHEN m.document_master_items_code <> 'MRI001' "
                + "AND COALESCE(ps.valid_general_main, 0) > 0 THEN 'COMPLETE' "
                + "WHEN COALESCE(ps.fix_count, 0) > 0 THEN 'NEED_FIX' "
                + "WHEN p.id IS NULL THEN 'MISSING' "
                + "WHEN COALESCE(ps.uploaded_count, 0) = 0 THEN 'NOT_UPLOADED' "
                + "ELSE 'INCOMPLETE' END AS document_status, "
                + "m.sort_order, "
                + "CASE WHEN COALESCE(ps.uploaded_count, 0) > 0 THEN 1 ELSE 0 END AS file_uploaded, "
                + "0 AS request_item_file_uploaded, "
                + "p.file_path, p.file_uploaded_at, p.check_result, p.check_note, "
                + "p.id AS profile_request_item_id "
                + "FROM m_document_master_request_item m "
                + "LEFT JOIN (SELECT document_master_request_item_code, MIN(id) AS profile_item_id "
                + "FROM m_document_profile_request_item "
                + "WHERE mobile_user_uuid = :mobileUserUuid "
                + "GROUP BY document_master_request_item_code) pmin "
                + "ON pmin.document_master_request_item_code = m.document_master_items_code "
                + "LEFT JOIN m_document_profile_request_item p ON p.id = pmin.profile_item_id "
                + "LEFT JOIN (SELECT document_master_request_item_code, "
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
                + "GROUP BY document_master_request_item_code) ps "
                + "ON ps.document_master_request_item_code = m.document_master_items_code "
                + "WHERE m.is_active = 'YES' AND m.storage_scope = 'PROFILE' "
                + "ORDER BY m.sort_order, m.document_master_items_code";
        return template.query(sql,
                new MapSqlParameterSource("mobileUserUuid", mobileUserUuid),
                new BeanPropertyRowMapper<>(DocumentRequestItemEntity.class));
    }

    public void deleteOtherTypes(String mobileUserUuid, String itemCode, String documentType) {
        template.update("DELETE FROM m_document_profile_request_item WHERE mobile_user_uuid = :mobileUserUuid "
                        + "AND document_master_request_item_code = :itemCode AND document_type IS NOT NULL "
                        + "AND document_type <> :documentType",
                params(mobileUserUuid, itemCode, documentType, null));
    }

    public String upsertFile(String mobileUserUuid, String itemCode, String documentType, String slotCode,
                             String storageKey, String originalName, String mimeType, long size) {
        List<String> ids = template.query("SELECT id FROM m_document_profile_request_item "
                        + "WHERE mobile_user_uuid = :mobileUserUuid AND document_master_request_item_code = :itemCode "
                        + "AND document_type = :documentType AND slot_code = :slotCode FOR UPDATE",
                params(mobileUserUuid, itemCode, documentType, slotCode), (rs, n) -> rs.getString("id"));
        String id = ids.isEmpty() ? UUID.randomUUID().toString() : ids.get(0);
        MapSqlParameterSource p = params(mobileUserUuid, itemCode, documentType, slotCode)
                .addValue("id", id).addValue("storageKey", storageKey).addValue("originalName", originalName)
                .addValue("mimeType", mimeType).addValue("size", size);
        int rows;
        if (ids.isEmpty()) {
            rows = template.update("INSERT INTO m_document_profile_request_item "
                    + "(id, mobile_user_uuid, document_master_request_item_code, document_type, slot_code, sort_order, "
                    + "file_uploaded, file_path, original_file_name, mime_type, file_size, file_uploaded_at) "
                    + "SELECT :id, :mobileUserUuid, document_master_items_code, :documentType, :slotCode, sort_order, "
                    + "1, :storageKey, :originalName, :mimeType, :size, NOW() FROM m_document_master_request_item "
                    + "WHERE document_master_items_code = :itemCode AND is_active = 'YES'", p);
        } else {
            rows = template.update("UPDATE m_document_profile_request_item SET file_uploaded = 1, file_path = :storageKey, "
                    + "original_file_name = :originalName, mime_type = :mimeType, file_size = :size, file_uploaded_at = NOW(), "
                    + "check_result = NULL, check_note = NULL, is_updated = 1, checked_at = NULL, checked_by = NULL, updated_at = NOW() "
                    + "WHERE id = :id", p);
        }
        if (rows != 1) throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "documentRequestItemFile");
        return id;
    }

    public boolean isComplete(String mobileUserUuid, String itemCode, String documentType) {
        String sql;
        if ("ID_CARD".equals(documentType)) {
            sql = "SELECT CASE WHEN ("
                    + "(SELECT COUNT(DISTINCT slot_code) FROM m_document_profile_request_item "
                    + "WHERE mobile_user_uuid = :mobileUserUuid "
                    + "AND document_master_request_item_code = :itemCode AND document_type = 'ID_CARD' "
                    + "AND slot_code IN ('FRONT','BACK') AND file_uploaded = 1 "
                    + "AND (check_result IS NULL OR check_result <> 'fix')) = 2 "
                    + "OR EXISTS (SELECT 1 FROM m_document_profile_request_item "
                    + "WHERE mobile_user_uuid = :mobileUserUuid "
                    + "AND document_master_request_item_code = :itemCode AND document_type = 'ID_CARD' "
                    + "AND slot_code = 'MAIN' AND file_uploaded = 1 "
                    + "AND (check_result IS NULL OR check_result <> 'fix'))) THEN 1 ELSE 0 END";
        } else {
            sql = "SELECT COUNT(*) FROM m_document_profile_request_item WHERE mobile_user_uuid = :mobileUserUuid "
                    + "AND document_master_request_item_code = :itemCode AND document_type = :documentType "
                    + "AND slot_code = 'MAIN' AND file_uploaded = 1 AND (check_result IS NULL OR check_result <> 'fix')";
        }
        Integer count = template.queryForObject(sql, params(mobileUserUuid, itemCode, documentType, null), Integer.class);
        return count != null && count == 1;
    }

    private MapSqlParameterSource params(String user, String item, String type, String slot) {
        return new MapSqlParameterSource().addValue("mobileUserUuid", user).addValue("itemCode", item)
                .addValue("documentType", type).addValue("slotCode", slot);
    }
}
