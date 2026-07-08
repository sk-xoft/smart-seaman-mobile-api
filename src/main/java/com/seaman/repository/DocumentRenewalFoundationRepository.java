package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.constant.DocumentRenewalAction;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.DocumentRenewalTransactionEntity;
import com.seaman.entity.PaymentTransactionEntity;
import com.seaman.entity.RenewalRequestItemEntity;
import com.seaman.entity.DeptSubmissionEntity;
import com.seaman.entity.DeliveryEntity;
import com.seaman.exception.BusinessException;
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

    public DocumentRenewalRequestEntity lockOwnedRequestByNo(
            String requestNo, String mobileUserUuid) {
        List<DocumentRenewalRequestEntity> rows = template.query(
                "SELECT r.*, s.name_en AS status_name_en FROM m_document_request r "
                        + "INNER JOIN m_document_status s ON s.id = r.document_status_id "
                        + "WHERE r.request_no = :requestNo AND r.mobile_user_uuid = :mobileUserUuid "
                        + "FOR UPDATE",
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
        String sql = "SELECT r.*, s.name_en AS status_name_en FROM m_document_request r "
                + "INNER JOIN m_document_status s ON s.id = r.document_status_id "
                + "WHERE r.id = :requestId AND r.mobile_user_uuid = :mobileUserUuid"
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

    public String findActiveStatusId(DocumentRenewalStatus status) {
        List<String> ids = template.query(
                "SELECT id FROM m_document_status WHERE name_en = :nameEn AND is_active = 'YES'",
                new MapSqlParameterSource("nameEn", status.getMasterNameEn()),
                (rs, rowNum) -> rs.getString("id"));
        if (ids.size() != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE,
                    "Active renewal status is missing or duplicated: " + status.name());
        }
        return ids.get(0);
    }

    public List<RenewalRequestItemEntity> findOwnedRequestItems(
            String requestId, String mobileUserUuid) {
        return template.query("SELECT i.* FROM m_document_request_items i "
                        + "INNER JOIN m_document_request r ON r.id = i.request_id "
                        + "WHERE i.request_id = :requestId AND r.mobile_user_uuid = :mobileUserUuid "
                        + "ORDER BY i.created_at, i.id",
                ownedParameters(requestId, mobileUserUuid),
                new BeanPropertyRowMapper<>(RenewalRequestItemEntity.class));
    }

    public RenewalRequestItemEntity lockOwnedRequestItem(
            String requestNo, String documentRequestItemCode, String mobileUserUuid) {
        List<RenewalRequestItemEntity> rows = template.query(
                "SELECT i.*, s.name_en AS status_name_en FROM m_document_request_items i "
                        + "INNER JOIN m_document_request r ON r.id = i.request_id "
                        + "INNER JOIN m_document_status s ON s.id = r.document_status_id "
                        + "WHERE r.request_no = :requestNo "
                        + "AND i.document_master_request_item_code = :documentRequestItemCode "
                        + "AND r.mobile_user_uuid = :mobileUserUuid FOR UPDATE",
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
                + "((SELECT COUNT(DISTINCT p.slot_code) FROM m_document_profile_request_item p "
                + "WHERE p.mobile_user_uuid = :mobileUserUuid "
                + "AND p.document_master_request_item_code = i.document_master_request_item_code "
                + "AND p.document_type = 'ID_CARD' AND p.slot_code IN ('FRONT','BACK') "
                + "AND p.file_uploaded = 1 AND (p.check_result IS NULL OR p.check_result <> 'fix')) = 2 "
                + "OR EXISTS (SELECT 1 FROM m_document_profile_request_item p "
                + "WHERE p.mobile_user_uuid = :mobileUserUuid "
                + "AND p.document_master_request_item_code = i.document_master_request_item_code "
                + "AND p.document_type = 'PASSPORT' AND p.slot_code = 'MAIN' "
                + "AND p.file_uploaded = 1 AND (p.check_result IS NULL OR p.check_result <> 'fix'))) "
                + "AND EXISTS (SELECT 1 FROM m_document_profile_request_item p "
                + "WHERE p.mobile_user_uuid = :mobileUserUuid "
                + "AND p.document_master_request_item_code = i.document_master_request_item_code "
                + "AND p.file_uploaded = 1 AND p.is_updated = 1 "
                + "AND (p.check_result IS NULL OR p.check_result <> 'fix'))) "
                + "OR (i.document_master_request_item_code <> 'MRI001' AND EXISTS ("
                + "SELECT 1 FROM m_document_profile_request_item p "
                + "WHERE p.mobile_user_uuid = :mobileUserUuid "
                + "AND p.document_master_request_item_code = i.document_master_request_item_code "
                + "AND p.document_type = 'GENERAL' AND p.slot_code = 'MAIN' "
                + "AND p.file_uploaded = 1 AND p.is_updated = 1 "
                + "AND (p.check_result IS NULL OR p.check_result <> 'fix'))))";
        Integer count = template.queryForObject(sql,
                new MapSqlParameterSource().addValue("requestId", requestId)
                        .addValue("mobileUserUuid", mobileUserUuid), Integer.class);
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

    public List<DocumentRenewalTransactionEntity> findOwnedTransactions(
            String requestId, String mobileUserUuid) {
        return template.query("SELECT t.* FROM m_document_transaction t "
                        + "INNER JOIN m_document_request r ON r.id = t.request_id "
                        + "WHERE t.request_id = :requestId AND r.mobile_user_uuid = :mobileUserUuid "
                        + "ORDER BY t.actioned_at, t.id",
                ownedParameters(requestId, mobileUserUuid),
                new BeanPropertyRowMapper<>(DocumentRenewalTransactionEntity.class));
    }

    public List<PaymentTransactionEntity> findOwnedPayments(
            String requestId, String mobileUserUuid) {
        return template.query("SELECT p.* FROM m_payment_transaction p "
                        + "INNER JOIN m_document_request r ON r.id = p.request_id "
                        + "WHERE p.request_id = :requestId AND r.mobile_user_uuid = :mobileUserUuid "
                        + "ORDER BY p.created_at, p.id",
                ownedParameters(requestId, mobileUserUuid),
                new BeanPropertyRowMapper<>(PaymentTransactionEntity.class));
    }

    public List<DeptSubmissionEntity> findOwnedDeptSubmissions(
            String requestId, String mobileUserUuid) {
        return template.query("SELECT d.* FROM m_dept_submission d "
                        + "INNER JOIN m_document_request r ON r.id = d.request_id "
                        + "WHERE d.request_id = :requestId AND r.mobile_user_uuid = :mobileUserUuid",
                ownedParameters(requestId, mobileUserUuid),
                new BeanPropertyRowMapper<>(DeptSubmissionEntity.class));
    }

    public List<DeliveryEntity> findOwnedDeliveries(String requestId, String mobileUserUuid) {
        return template.query("SELECT d.* FROM m_delivery d "
                        + "INNER JOIN m_document_request r ON r.id = d.request_id "
                        + "WHERE d.request_id = :requestId AND r.mobile_user_uuid = :mobileUserUuid",
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
                        + "AND document_status_id = :currentStatusId",
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
