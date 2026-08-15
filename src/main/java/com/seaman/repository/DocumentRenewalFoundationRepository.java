package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.constant.BusinessConstant;
import com.seaman.constant.DocumentRenewalAction;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRenewalTransactionEntity;
import com.seaman.entity.PaymentTransactionEntity;
import com.seaman.entity.RenewalRequestItemEntity;
import com.seaman.entity.DeptSubmissionEntity;
import com.seaman.entity.DeliveryEntity;
import com.seaman.exception.BusinessException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class DocumentRenewalFoundationRepository extends CommonRepository {

    public DocumentRenewalRequestEntity findOwnedRequest(String requestId, String mobileUserUuid) {
        return findOwned(requestId, mobileUserUuid, false);
    }

    public DocumentRenewalRequestEntity lockOwnedRequest(String requestId, String mobileUserUuid) {
        return findOwned(requestId, mobileUserUuid, true);
    }

    public DocumentRenewalRequestEntity lockRequest(String requestId) {
        List<DocumentRenewalRequestEntity> rows = template.query(
                "SELECT r.*, s.document_status_code AS status_code, "
                        + "s.name_en AS status_name_en, s.name_th AS status_name_th, "
                        + "s.css_color AS status_css_color, "
                        + "s.document_mobile_status_code, "
                        + "s.document_mobile_status_name_th, "
                        + "s.document_mobile_status_name_en "
                        + "FROM m_document_request r "
                        + "INNER JOIN m_document_status s ON s.id = r.document_status_id "
                        + "WHERE r.id = :requestId AND r.is_active = 'YES' FOR UPDATE",
                new MapSqlParameterSource("requestId", requestId),
                new BeanPropertyRowMapper<>(DocumentRenewalRequestEntity.class));
        if (rows.isEmpty()) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "documentRenewalRequest");
        }
        return rows.get(0);
    }

    public DocumentRenewalRequestEntity lockOwnedRequestByNo(
            String requestNo, String mobileUserUuid) {
        return findOwnedRequestByNo(requestNo, mobileUserUuid, true);
    }

    public DocumentRenewalRequestEntity findOwnedRequestByNo(
            String requestNo, String mobileUserUuid) {
        return findOwnedRequestByNo(requestNo, mobileUserUuid, false);
    }

    private DocumentRenewalRequestEntity findOwnedRequestByNo(
            String requestNo, String mobileUserUuid, boolean lock) {
        List<DocumentRenewalRequestEntity> rows = template.query(
                "SELECT r.*, s.document_status_code AS status_code, "
                        + "s.name_en AS status_name_en, s.name_th AS status_name_th, "
                        + "s.css_color AS status_css_color, "
                        + "s.document_mobile_status_code, "
                        + "s.document_mobile_status_name_th, "
                        + "s.document_mobile_status_name_en "
                        + "FROM m_document_request r "
                        + "INNER JOIN m_document_status s ON s.id = r.document_status_id "
                        + "WHERE r.request_no = :requestNo AND r.mobile_user_uuid = :mobileUserUuid"
                        + " AND r.is_active = 'YES'"
                        + (lock ? " FOR UPDATE" : ""),
                new MapSqlParameterSource().addValue("requestNo", requestNo)
                        .addValue("mobileUserUuid", mobileUserUuid),
                new BeanPropertyRowMapper<>(DocumentRenewalRequestEntity.class));
        if (rows.isEmpty()) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "documentRenewalRequest");
        }
        return rows.get(0);
    }

    private DocumentRenewalRequestEntity findOwned(
            String requestId, String mobileUserUuid, boolean lock) {
        String sql = "SELECT r.*, s.document_status_code AS status_code, "
                + "s.name_en AS status_name_en, s.name_th AS status_name_th, "
                + "s.css_color AS status_css_color, "
                + "s.document_mobile_status_code, "
                + "s.document_mobile_status_name_th, "
                + "s.document_mobile_status_name_en "
                + "FROM m_document_request r "
                + "INNER JOIN m_document_status s ON s.id = r.document_status_id "
                + "WHERE r.id = :requestId AND r.mobile_user_uuid = :mobileUserUuid"
                + " AND r.is_active = 'YES'"
                + (lock ? " FOR UPDATE" : "");
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("requestId", requestId)
                .addValue("mobileUserUuid", mobileUserUuid);
        List<DocumentRenewalRequestEntity> rows = template.query(
                sql, parameters, new BeanPropertyRowMapper<>(DocumentRenewalRequestEntity.class));
        if (rows.isEmpty()) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "documentRenewalRequest");
        }
        return rows.get(0);
    }

    @Cacheable(cacheNames = BusinessConstant.MASTER_DOCUMENT_RENEWAL_STATUS, key = "#status.name()", sync = true)
    public String findActiveStatusId(DocumentRenewalStatus status) {
        List<String> ids = template.query(
                "SELECT id FROM m_document_status WHERE document_status_code = :statusCode "
                        + "AND is_active = 'YES'",
                new MapSqlParameterSource("statusCode", status.name()),
                (rs, rowNum) -> rs.getString("id"));
        if (ids.size() != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE,
                    "Active renewal status is missing or duplicated: " + status.name());
        }
        return ids.get(0);
    }

    public List<RenewalRequestItemEntity> findOwnedRequestItems(
            String requestId, String mobileUserUuid) {
        return template.query("SELECT i.*, m.document_master_items_name AS document_name_th, "
                        + "m.document_master_items_name AS document_name_en, m.sort_order, "
                        + "m.storage_scope "
                        + "FROM m_document_request_items i "
                        + "INNER JOIN m_document_request r ON r.id = i.request_id "
                        + "INNER JOIN m_document_master_request_item m "
                        + "ON m.document_master_items_code = i.document_master_request_item_code "
                        + "WHERE i.request_id = :requestId AND r.mobile_user_uuid = :mobileUserUuid "
                        + "AND r.is_active = 'YES' "
                        + "ORDER BY m.sort_order, i.id",
                ownedParameters(requestId, mobileUserUuid),
                new BeanPropertyRowMapper<>(RenewalRequestItemEntity.class));
    }

    public RenewalRequestItemEntity lockOwnedRequestItem(
            String requestNo, String documentRequestItemCode, String mobileUserUuid) {
        return findOwnedRequestItem(requestNo, documentRequestItemCode, mobileUserUuid, true);
    }

    public RenewalRequestItemEntity findOwnedRequestItem(
            String requestNo, String documentRequestItemCode, String mobileUserUuid) {
        return findOwnedRequestItem(requestNo, documentRequestItemCode, mobileUserUuid, false);
    }

    private RenewalRequestItemEntity findOwnedRequestItem(
            String requestNo, String documentRequestItemCode, String mobileUserUuid, boolean lock) {
        List<RenewalRequestItemEntity> rows = template.query(
                "SELECT i.*, s.name_en AS status_name_en, m.storage_scope, "
                        + "m.document_master_items_name AS document_name_th, "
                        + "m.document_master_items_name AS document_name_en, m.sort_order "
                        + "FROM m_document_request_items i "
                        + "INNER JOIN m_document_request r ON r.id = i.request_id "
                        + "INNER JOIN m_document_status s ON s.id = r.document_status_id "
                        + "INNER JOIN m_document_master_request_item m "
                        + "ON m.document_master_items_code = i.document_master_request_item_code "
                        + "WHERE r.request_no = :requestNo "
                        + "AND i.document_master_request_item_code = :documentRequestItemCode "
                        + "AND r.mobile_user_uuid = :mobileUserUuid AND r.is_active = 'YES'"
                        + (lock ? " FOR UPDATE" : ""),
                new MapSqlParameterSource().addValue("requestNo", requestNo)
                        .addValue("documentRequestItemCode", documentRequestItemCode)
                        .addValue("mobileUserUuid", mobileUserUuid),
                new BeanPropertyRowMapper<>(RenewalRequestItemEntity.class));
        if (rows.isEmpty()) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "documentRenewalRequestItem");
        }
        return rows.get(0);
    }

    public int countFixItems(String requestId) {
        Integer count = template.queryForObject(
                "SELECT COUNT(*) FROM m_document_request_items "
                        + "WHERE request_id = :requestId AND approve_status = 'FIX'",
                new MapSqlParameterSource("requestId", requestId), Integer.class);
        return count == null ? 0 : count;
    }

    public int countIncompleteCorrectedFixItems(String requestId, String mobileUserUuid) {
        String sql = "SELECT COUNT(*) FROM m_document_request_items i "
                + "WHERE i.request_id = :requestId AND i.approve_status = 'FIX' AND NOT ("
                + "(i.document_master_request_item_code = 'MRI001' AND ("
                + "((SELECT COUNT(DISTINCT f.slot_code) FROM m_document_request_item_files f "
                + "WHERE f.request_item_id = i.id "
                + "AND f.document_type = 'ID_CARD' AND f.slot_code IN ('FRONT','BACK') "
                + "AND f.file_uploaded = 1 AND f.is_updated = 1 "
                + "AND (f.check_result IS NULL OR f.check_result <> 'fix')) = 2) "
                + "OR EXISTS (SELECT 1 FROM m_document_request_item_files f "
                + "WHERE f.request_item_id = i.id "
                + "AND f.document_type = 'ID_CARD' AND f.slot_code = 'MAIN' "
                + "AND f.file_uploaded = 1 AND f.is_updated = 1 "
                + "AND (f.check_result IS NULL OR f.check_result <> 'fix')) "
                + "OR EXISTS (SELECT 1 FROM m_document_request_item_files f "
                + "WHERE f.request_item_id = i.id "
                + "AND f.document_type = 'PASSPORT' AND f.slot_code = 'MAIN' "
                + "AND f.file_uploaded = 1 AND f.is_updated = 1 "
                + "AND (f.check_result IS NULL OR f.check_result <> 'fix')))) "
                + "OR (i.document_master_request_item_code <> 'MRI001' "
                + "AND EXISTS (SELECT 1 FROM m_document_request_item_files f "
                + "WHERE f.request_item_id = i.id "
                + "AND f.document_type = 'GENERAL' AND f.slot_code = 'MAIN' "
                + "AND f.file_uploaded = 1 AND f.is_updated = 1 "
                + "AND (f.check_result IS NULL OR f.check_result <> 'fix'))))";
        Integer count = template.queryForObject(sql,
                new MapSqlParameterSource().addValue("requestId", requestId)
                        .addValue("mobileUserUuid", mobileUserUuid), Integer.class);
        return count == null ? 0 : count;
    }

    public int countIncompleteRequestScopedItems(String requestId) {
        String sql = "SELECT COUNT(*) FROM m_document_request_items i "
                + "INNER JOIN m_document_master_request_item m "
                + "ON m.document_master_items_code = i.document_master_request_item_code "
                + "WHERE i.request_id = :requestId AND m.storage_scope = 'REQUEST' AND NOT ("
                + "(i.document_master_request_item_code = 'MRI001' AND ("
                + "((SELECT COUNT(DISTINCT f.slot_code) FROM m_document_request_item_files f "
                + "WHERE f.request_item_id = i.id "
                + "AND f.document_type = 'ID_CARD' AND f.slot_code IN ('FRONT','BACK') "
                + "AND f.file_uploaded = 1 AND (f.check_result IS NULL OR f.check_result <> 'fix')) = 2) "
                + "OR EXISTS (SELECT 1 FROM m_document_request_item_files f "
                + "WHERE f.request_item_id = i.id "
                + "AND f.document_type = 'ID_CARD' AND f.slot_code = 'MAIN' "
                + "AND f.file_uploaded = 1 AND (f.check_result IS NULL OR f.check_result <> 'fix')) "
                + "OR EXISTS (SELECT 1 FROM m_document_request_item_files f "
                + "WHERE f.request_item_id = i.id "
                + "AND f.document_type = 'PASSPORT' AND f.slot_code = 'MAIN' "
                + "AND f.file_uploaded = 1 AND (f.check_result IS NULL OR f.check_result <> 'fix')))) "
                + "OR (i.document_master_request_item_code <> 'MRI001' "
                + "AND EXISTS (SELECT 1 FROM m_document_request_item_files f "
                + "WHERE f.request_item_id = i.id "
                + "AND f.document_type = 'GENERAL' AND f.slot_code = 'MAIN' "
                + "AND f.file_uploaded = 1 AND (f.check_result IS NULL OR f.check_result <> 'fix'))))";
        Integer count = template.queryForObject(sql,
                new MapSqlParameterSource("requestId", requestId), Integer.class);
        return count == null ? 0 : count;
    }

    public void resetFixItemsForReview(String requestId) {
        int updated = template.update(
                "UPDATE m_document_request_items SET approve_status = 'PENDING', updated_at = NOW() "
                        + "WHERE request_id = :requestId AND approve_status = 'FIX'",
                new MapSqlParameterSource("requestId", requestId));
        if (updated < 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE,
                    "documentRenewalRequestItems");
        }
    }

    public void markResubmitted(String requestId) {
        int updated = template.update(
                "UPDATE m_document_request SET is_resubmit = 1, updated_at = NOW() "
                        + "WHERE id = :requestId AND is_active = 'YES'",
                new MapSqlParameterSource("requestId", requestId));
        if (updated != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE,
                    "documentRenewalRequest");
        }
    }

    public void softDeleteRequest(String requestId) {
        int updated = template.update(
                "UPDATE m_document_request SET is_active = 'NO', updated_at = NOW() "
                        + "WHERE id = :requestId AND is_active = 'YES'",
                new MapSqlParameterSource("requestId", requestId));
        if (updated != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE,
                    "documentRenewalRequest");
        }
    }

    public void hardDeleteRequest(String requestId) {
        MapSqlParameterSource requestIdParam = new MapSqlParameterSource("requestId", requestId);
        template.update(
                "DELETE f FROM m_document_request_item_files f "
                        + "INNER JOIN m_document_request_items i ON i.id = f.request_item_id "
                        + "WHERE i.request_id = :requestId", requestIdParam);
        template.update(
                "DELETE FROM m_document_request_items WHERE request_id = :requestId",
                requestIdParam);
        template.update(
                "DELETE FROM m_delivery WHERE request_id = :requestId", requestIdParam);
        template.update(
                "DELETE FROM m_dept_submission WHERE request_id = :requestId", requestIdParam);
        template.update(
                "DELETE FROM m_document_request_delivery_address WHERE request_id = :requestId",
                requestIdParam);
        template.update(
                "DELETE FROM m_payment_transaction WHERE request_id = :requestId",
                requestIdParam);
        template.update(
                "DELETE FROM m_document_transaction WHERE request_id = :requestId",
                requestIdParam);
        int deleted = template.update(
                "DELETE FROM m_document_request WHERE id = :requestId", requestIdParam);
        if (deleted != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "documentRenewalRequest");
        }
    }

    public void updateRequestMobileNumber(String requestId, String mobileNumber) {
        int updated = template.update(
                "UPDATE m_document_request SET mobile_number = :mobileNumber, updated_at = NOW() "
                        + "WHERE id = :requestId AND is_active = 'YES'",
                new MapSqlParameterSource().addValue("requestId", requestId)
                        .addValue("mobileNumber", mobileNumber));
        if (updated != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE,
                    "documentRenewalRequest");
        }
    }

    public void updateDeliveryAddressMobileNumber(
            String requestId, String mobileUserUuid, String mobileNumber) {
        int updated = template.update(
                "UPDATE m_document_request_delivery_address "
                        + "SET mobile_number = :mobileNumber "
                        + "WHERE request_id = :requestId AND mobile_user_uuid = :mobileUserUuid",
                new MapSqlParameterSource().addValue("requestId", requestId)
                        .addValue("mobileUserUuid", mobileUserUuid)
                        .addValue("mobileNumber", mobileNumber));
        if (updated != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE,
                    "documentRenewalDeliveryAddressSnapshot");
        }
    }

    public List<DocumentRenewalTransactionEntity> findOwnedTransactions(
            String requestId, String mobileUserUuid) {
        return template.query("SELECT t.id, t.request_id, t.action, "
                        + "COALESCE(fs.document_mobile_status_code, t.from_status) AS from_status, "
                        + "COALESCE(ts.document_mobile_status_code, t.to_status) AS to_status, "
                        + "t.note, t.actioned_at, t.actioned_by "
                        + "FROM m_document_transaction t "
                        + "INNER JOIN m_document_request r ON r.id = t.request_id "
                        + "LEFT JOIN m_document_status fs "
                        + "ON fs.document_status_code = t.from_status "
                        + "LEFT JOIN m_document_status ts "
                        + "ON ts.document_status_code = t.to_status "
                        + "WHERE t.request_id = :requestId AND r.mobile_user_uuid = :mobileUserUuid "
                        + "AND r.is_active = 'YES' "
                        + "ORDER BY t.actioned_at, t.id",
                ownedParameters(requestId, mobileUserUuid),
                new BeanPropertyRowMapper<>(DocumentRenewalTransactionEntity.class));
    }

    public List<PaymentTransactionEntity> findOwnedPayments(
            String requestId, String mobileUserUuid) {
        return template.query("SELECT p.* FROM m_payment_transaction p "
                        + "INNER JOIN m_document_request r ON r.id = p.request_id "
                        + "WHERE p.request_id = :requestId AND r.mobile_user_uuid = :mobileUserUuid "
                        + "AND r.is_active = 'YES' "
                        + "ORDER BY p.created_at, p.id",
                ownedParameters(requestId, mobileUserUuid),
                new BeanPropertyRowMapper<>(PaymentTransactionEntity.class));
    }

    public List<DeptSubmissionEntity> findOwnedDeptSubmissions(
            String requestId, String mobileUserUuid) {
        return template.query("SELECT d.* FROM m_dept_submission d "
                        + "INNER JOIN m_document_request r ON r.id = d.request_id "
                        + "WHERE d.request_id = :requestId AND r.mobile_user_uuid = :mobileUserUuid "
                        + "AND r.is_active = 'YES'",
                ownedParameters(requestId, mobileUserUuid),
                new BeanPropertyRowMapper<>(DeptSubmissionEntity.class));
    }

    public List<DeliveryEntity> findOwnedDeliveries(String requestId, String mobileUserUuid) {
        return template.query("SELECT d.* FROM m_delivery d "
                        + "INNER JOIN m_document_request r ON r.id = d.request_id "
                        + "WHERE d.request_id = :requestId AND r.mobile_user_uuid = :mobileUserUuid "
                        + "AND r.is_active = 'YES'",
                ownedParameters(requestId, mobileUserUuid),
                new BeanPropertyRowMapper<>(DeliveryEntity.class));
    }

    private MapSqlParameterSource ownedParameters(String requestId, String mobileUserUuid) {
        return new MapSqlParameterSource().addValue("requestId", requestId)
                .addValue("mobileUserUuid", mobileUserUuid);
    }

    public void updateStatus(String requestId, String currentStatusId, String targetStatusId) {
        int updated = template.update(
                "UPDATE m_document_request SET document_status_id = :targetStatusId, "
                        + "updated_at = NOW() WHERE id = :requestId "
                        + "AND document_status_id = :currentStatusId AND is_active = 'YES'",
                new MapSqlParameterSource().addValue("targetStatusId", targetStatusId)
                        .addValue("requestId", requestId)
                        .addValue("currentStatusId", currentStatusId));
        if (updated != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE,
                    "Concurrent document renewal status update");
        }
    }

    public void appendTransaction(String requestId, DocumentRenewalAction action,
                                  DocumentRenewalStatus fromStatus,
                                  DocumentRenewalStatus toStatus, String note, String actionedBy) {
        int inserted = template.update(
                "INSERT INTO m_document_transaction "
                        + "(id, request_id, action, from_status, to_status, note, actioned_by) "
                        + "VALUES (:id, :requestId, :action, :fromStatus, :toStatus, :note, :actionedBy)",
                new MapSqlParameterSource().addValue("id", UUID.randomUUID().toString())
                        .addValue("requestId", requestId).addValue("action", action.name())
                        .addValue("fromStatus", fromStatus == null ? null : fromStatus.name())
                        .addValue("toStatus", toStatus.name()).addValue("note", note)
                        .addValue("actionedBy", actionedBy));
        if (inserted != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE,
                    "Cannot append document renewal transaction");
        }
    }
}
