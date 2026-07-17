package com.seaman.service;

import com.seaman.constant.AppStatus;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.PaymentTransactionEntity;
import com.seaman.entity.UsersEntity;
import com.seaman.exception.BusinessException;
import com.seaman.model.external.response.OmiseChargeResponse;
import com.seaman.model.request.DocumentRenewalPaymentRequest;
import com.seaman.model.response.DocumentRenewalPaymentResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import com.seaman.repository.DocumentRenewalPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentRenewalPaymentService {
    private final DocumentRenewalFoundationRepository foundationRepository;
    private final DocumentRenewalPaymentRepository paymentRepository;
    private final OmisePaymentClient omiseClient;
    private final HttpServletRequest httpServletRequest;

    @Transactional
    public DocumentRenewalPaymentResponse create(String requestId, DocumentRenewalPaymentRequest input) {
        validateUuid(requestId, "requestId");
        UsersEntity user = currentUser();
        DocumentRenewalRequestEntity request =
                foundationRepository.lockOwnedRequest(requestId, user.getMobileUuid());
        if (!DocumentRenewalStatus.PAYMENT_PENDING.getMasterNameEn()
                .equals(request.getStatusNameEn())) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "documentStatus");
        }
        if (foundationRepository.countIncompleteRequestScopedItems(requestId) > 0) {
            throw new BusinessException(AppStatus.MISSING_PARAMETER, "requestDocumentItems");
        }
        validatePaymentInput(input);

        PaymentTransactionEntity existing = paymentRepository.findByIdempotencyKey(
                requestId, input.getIdempotencyKey(), user.getMobileUuid());
        if (existing != null) {
            return map(existing);
        }

        OmiseChargeResponse charge = omiseClient.createCharge(
                request.getAmount(), request.getRequestNo(), input);
        PaymentTransactionEntity entity = new PaymentTransactionEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setRequestId(requestId);
        entity.setRequestNo(request.getRequestNo());
        entity.setTransactionNo("PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase(Locale.ROOT));
        entity.setChannel(channel(input.getPaymentMethod()));
        entity.setPaymentMethod(input.getPaymentMethod());
        entity.setAmount(request.getAmount());
        entity.setCurrency("THB");
        entity.setStatus(normalizeStatus(charge.getStatus()));
        entity.setProvider("OMISE");
        entity.setProviderChargeId(charge.getId());
        entity.setProviderSourceId(charge.getSourceId());
        entity.setProviderTransactionId(charge.getTransactionId());
        entity.setProviderStatus(charge.getStatus());
        entity.setProviderResponse(omiseClient.rawJson(charge.getRaw()));
        entity.setIdempotencyKey(input.getIdempotencyKey());
        entity.setDescription("Document renewal " + request.getRequestNo());
        entity.setReturnUri(charge.getReturnUri());
        entity.setAuthorizeUri(charge.getAuthorizeUri());
        entity.setFailureCode(charge.getFailureCode());
        entity.setFailureMessage(charge.getFailureMessage());
        entity.setIsLivemode(charge.getLivemode());
        entity.setExpiresAt(charge.getExpiresAt());
        entity.setPaidAt(charge.getPaidAt());
        paymentRepository.insert(entity);
        return map(entity, charge.getQrCodeDownloadUri());
    }

    public DocumentRenewalPaymentResponse status(String requestId, String transactionId) {
        validateUuid(requestId, "requestId");
        validateUuid(transactionId, "transactionId");
        UsersEntity user = currentUser();
        return map(paymentRepository.findOwnedPayment(requestId, transactionId, user.getMobileUuid()));
    }

    private void validatePaymentInput(DocumentRenewalPaymentRequest input) {
        String method = input.getPaymentMethod().trim().toLowerCase(Locale.ROOT);
        input.setPaymentMethod(method);
        input.setIdempotencyKey(input.getIdempotencyKey().trim());
        if (isMobileBanking(method)
                && (input.getReturnUri() == null || input.getReturnUri().trim().isEmpty())) {
            throw new BusinessException(AppStatus.MISSING_PARAMETER, "returnUri");
        }
        if ("promptpay".equals(method)) {
            input.setReturnUri(null);
        }
    }

    private String channel(String paymentMethod) {
        return isMobileBanking(paymentMethod) ? "MOBILE_BANKING" : "PROMPTPAY";
    }

    private boolean isMobileBanking(String paymentMethod) {
        return paymentMethod != null && paymentMethod.startsWith("mobile_banking_");
    }

    String normalizeStatus(String providerStatus) {
        if ("successful".equals(providerStatus)) {
            return "SUCCESS";
        }
        if ("failed".equals(providerStatus)) {
            return "FAILED";
        }
        if ("expired".equals(providerStatus)) {
            return "EXPIRED";
        }
        if ("pending".equals(providerStatus)) {
            return "PENDING";
        }
        return "PROCESSING";
    }

    DocumentRenewalPaymentResponse map(PaymentTransactionEntity entity) {
        return map(entity, omiseClient.qrCodeDownloadUri(entity.getProviderResponse()));
    }

    private DocumentRenewalPaymentResponse map(PaymentTransactionEntity entity, String qrCodeDownloadUri) {
        DocumentRenewalPaymentResponse response = new DocumentRenewalPaymentResponse();
        response.setRequestId(entity.getRequestId());
        response.setRequestNo(entity.getRequestNo());
        response.setTransactionId(entity.getId());
        response.setTransactionNo(entity.getTransactionNo());
        response.setStatus(entity.getStatus());
        response.setChannel(entity.getChannel());
        response.setPaymentMethod(entity.getPaymentMethod());
        response.setAmount(entity.getAmount());
        response.setCurrency(entity.getCurrency());
        response.setProvider(entity.getProvider());
        response.setProviderChargeId(entity.getProviderChargeId());
        response.setProviderSourceId(entity.getProviderSourceId());
        response.setAuthorizeUri(entity.getAuthorizeUri());
        response.setQrCodeDownloadUri(qrCodeDownloadUri);
        response.setExpiresAt(entity.getExpiresAt());
        response.setPaidAt(entity.getPaidAt());
        response.setFailureCode(entity.getFailureCode());
        response.setFailureMessage(entity.getFailureMessage());
        return response;
    }

    private UsersEntity currentUser() {
        UsersEntity user = (UsersEntity) httpServletRequest.getAttribute("userObject");
        if (user == null || user.getMobileUuid() == null || user.getMobileUuid().trim().isEmpty()) {
            throw new BusinessException(AppStatus.USERNAME_IS_NOT_FOUND_SECURITY_CONTEXT, "userObject");
        }
        return user;
    }

    private void validateUuid(String value, String field) {
        try {
            UUID.fromString(value);
        } catch (Exception ex) {
            throw new BusinessException(AppStatus.INVALID_UUID, field);
        }
    }
}
