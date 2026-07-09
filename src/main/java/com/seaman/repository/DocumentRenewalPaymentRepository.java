package com.seaman.repository;

import com.seaman.constant.AppStatus;
import com.seaman.entity.PaymentTransactionEntity;
import com.seaman.exception.BusinessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DocumentRenewalPaymentRepository extends CommonRepository {

    public PaymentTransactionEntity findOwnedPayment(
            String requestId, String transactionId, String mobileUserUuid) {
        List<PaymentTransactionEntity> rows = template.query(
                "SELECT p.* FROM m_payment_transaction p "
                        + "INNER JOIN m_document_request r ON r.id = p.request_id "
                        + "WHERE p.request_id = :requestId AND p.id = :transactionId "
                        + "AND r.mobile_user_uuid = :mobileUserUuid",
                new MapSqlParameterSource().addValue("requestId", requestId)
                        .addValue("transactionId", transactionId)
                        .addValue("mobileUserUuid", mobileUserUuid),
                new BeanPropertyRowMapper<>(PaymentTransactionEntity.class));
        if (rows.isEmpty()) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "paymentTransaction");
        }
        return rows.get(0);
    }

    public PaymentTransactionEntity findByIdempotencyKey(
            String requestId, String idempotencyKey, String mobileUserUuid) {
        List<PaymentTransactionEntity> rows = template.query(
                "SELECT p.* FROM m_payment_transaction p "
                        + "INNER JOIN m_document_request r ON r.id = p.request_id "
                        + "WHERE p.request_id = :requestId AND p.idempotency_key = :idempotencyKey "
                        + "AND r.mobile_user_uuid = :mobileUserUuid",
                new MapSqlParameterSource().addValue("requestId", requestId)
                        .addValue("idempotencyKey", idempotencyKey)
                        .addValue("mobileUserUuid", mobileUserUuid),
                new BeanPropertyRowMapper<>(PaymentTransactionEntity.class));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public void insert(PaymentTransactionEntity entity) {
        int inserted = template.update(
                "INSERT INTO m_payment_transaction "
                        + "(id, request_id, transaction_no, transaction_type, channel, payment_method, "
                        + "amount, currency, status, provider, provider_charge_id, provider_source_id, "
                        + "provider_transaction_id, provider_status, provider_response, idempotency_key, "
                        + "description, return_uri, authorize_uri, failure_code, failure_message, "
                        + "is_livemode, expires_at, paid_at) "
                        + "VALUES (:id, :requestId, :transactionNo, 'CHARGE', :channel, :paymentMethod, "
                        + ":amount, 'THB', :status, 'OMISE', :providerChargeId, :providerSourceId, "
                        + ":providerTransactionId, :providerStatus, :providerResponse, :idempotencyKey, "
                        + ":description, :returnUri, :authorizeUri, :failureCode, :failureMessage, "
                        + ":isLivemode, :expiresAt, :paidAt)",
                parameters(entity));
        if (inserted != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "paymentTransaction");
        }
    }

    public PaymentTransactionEntity lockByProviderChargeId(String providerChargeId) {
        List<PaymentTransactionEntity> rows = template.query(
                "SELECT * FROM m_payment_transaction "
                        + "WHERE provider = 'OMISE' AND provider_charge_id = :providerChargeId "
                        + "FOR UPDATE",
                new MapSqlParameterSource("providerChargeId", providerChargeId),
                new BeanPropertyRowMapper<>(PaymentTransactionEntity.class));
        if (rows.isEmpty()) {
            throw new BusinessException(AppStatus.DATA_NOT_FOUND, "paymentTransaction");
        }
        return rows.get(0);
    }

    public void updateFromProvider(PaymentTransactionEntity entity) {
        int updated = template.update(
                "UPDATE m_payment_transaction SET status = :status, provider_source_id = :providerSourceId, "
                        + "provider_transaction_id = :providerTransactionId, provider_status = :providerStatus, "
                        + "provider_response = :providerResponse, return_uri = :returnUri, "
                        + "authorize_uri = :authorizeUri, failure_code = :failureCode, "
                        + "failure_message = :failureMessage, is_livemode = :isLivemode, "
                        + "expires_at = :expiresAt, paid_at = :paidAt, failed_at = :failedAt, "
                        + "updated_at = NOW() WHERE id = :id",
                parameters(entity));
        if (updated != 1) {
            throw new BusinessException(AppStatus.EXCEPTION_DATABASE, "paymentTransaction");
        }
    }

    private MapSqlParameterSource parameters(PaymentTransactionEntity entity) {
        return new MapSqlParameterSource()
                .addValue("id", entity.getId())
                .addValue("requestId", entity.getRequestId())
                .addValue("transactionNo", entity.getTransactionNo())
                .addValue("channel", entity.getChannel())
                .addValue("paymentMethod", entity.getPaymentMethod())
                .addValue("amount", entity.getAmount())
                .addValue("status", entity.getStatus())
                .addValue("providerChargeId", entity.getProviderChargeId())
                .addValue("providerSourceId", entity.getProviderSourceId())
                .addValue("providerTransactionId", entity.getProviderTransactionId())
                .addValue("providerStatus", entity.getProviderStatus())
                .addValue("providerResponse", entity.getProviderResponse())
                .addValue("idempotencyKey", entity.getIdempotencyKey())
                .addValue("description", entity.getDescription())
                .addValue("returnUri", entity.getReturnUri())
                .addValue("authorizeUri", entity.getAuthorizeUri())
                .addValue("failureCode", entity.getFailureCode())
                .addValue("failureMessage", entity.getFailureMessage())
                .addValue("isLivemode", Boolean.TRUE.equals(entity.getIsLivemode()) ? 1 : 0)
                .addValue("expiresAt", entity.getExpiresAt())
                .addValue("paidAt", entity.getPaidAt())
                .addValue("failedAt", entity.getFailedAt());
    }
}
