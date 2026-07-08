package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.exception.BusinessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

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

    public void insertRequest(String id, String requestNo, String mobileUserUuid,
                              String documentCode, String statusId, String priceSettingId,
                              String deliveryAddressId, java.math.BigDecimal amount) {
        int rows = template.update("INSERT INTO m_document_request "
                        + "(id, request_no, mobile_user_uuid, document_code, document_status_id, "
                        + "price_setting_id, delivery_address_id, amount) "
                        + "VALUES (:id, :requestNo, :mobileUserUuid, :documentCode, :statusId, "
                        + ":priceSettingId, :deliveryAddressId, :amount)",
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
