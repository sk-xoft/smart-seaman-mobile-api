package com.seaman.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seaman.constant.AppStatus;
import com.seaman.constant.AppSys;
import com.seaman.constant.DocumentRenewalAction;
import com.seaman.constant.DocumentRenewalStatus;
import com.seaman.entity.DocumentRenewalRequestEntity;
import com.seaman.entity.PaymentTransactionEntity;
import com.seaman.event.DocumentRenewalPaymentSucceededEvent;
import com.seaman.exception.BusinessException;
import com.seaman.model.external.response.OmiseChargeResponse;
import com.seaman.repository.DocumentRenewalFoundationRepository;
import com.seaman.repository.DocumentRenewalPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class OmiseWebhookService {
    private final ObjectMapper objectMapper;
    private final OmisePaymentClient omiseClient;
    private final DocumentRenewalPaymentService paymentService;
    private final DocumentRenewalPaymentRepository paymentRepository;
    private final DocumentRenewalFoundationRepository foundationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Environment environment;

    @Value("${omise.webhook-secret:}")
    private String webhookSecret;

    @Transactional
    public void handle(String rawBody, String signature, String timestamp) {
        verifySignature(rawBody, signature, timestamp);
        JsonNode event = parse(rawBody);
        String key = text(event, "key");
        if (!"charge.complete".equals(key)) {
            return;
        }
        String chargeId = text(event.path("data"), "id");
        if (chargeId == null || chargeId.trim().isEmpty()) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "omiseChargeId");
        }

        OmiseChargeResponse verified = omiseClient.retrieveCharge(chargeId);
        PaymentTransactionEntity payment =
                paymentRepository.lockByProviderChargeId(verified.getId());
        payment.setStatus(paymentService.normalizeStatus(verified.getStatus()));
        payment.setProviderSourceId(verified.getSourceId());
        payment.setProviderTransactionId(verified.getTransactionId());
        payment.setProviderStatus(verified.getStatus());
        payment.setProviderResponse(omiseClient.rawJson(verified.getRaw()));
        payment.setReturnUri(verified.getReturnUri());
        payment.setAuthorizeUri(verified.getAuthorizeUri());
        payment.setFailureCode(verified.getFailureCode());
        payment.setFailureMessage(verified.getFailureMessage());
        payment.setIsLivemode(verified.getLivemode());
        payment.setExpiresAt(verified.getExpiresAt());
        payment.setPaidAt(verified.getPaidAt());
        if ("FAILED".equals(payment.getStatus())) {
            payment.setFailedAt(new Date());
        }
        paymentRepository.updateFromProvider(payment);

        if (!"SUCCESS".equals(payment.getStatus())) {
            return;
        }
        DocumentRenewalRequestEntity request =
                foundationRepository.lockRequest(payment.getRequestId());
        if (!DocumentRenewalStatus.PAYMENT_PENDING.getMasterNameEn()
                .equals(request.getStatusNameEn())) {
            return;
        }
        String targetStatusId = foundationRepository.findActiveStatusId(
                DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW);
        foundationRepository.updateStatus(
                request.getId(), request.getDocumentStatusId(), targetStatusId);
        foundationRepository.appendTransaction(request.getId(),
                DocumentRenewalAction.PAYMENT_SUCCESS,
                DocumentRenewalStatus.PAYMENT_PENDING,
                DocumentRenewalStatus.PENDING_DOCUMENT_REVIEW,
                "Payment succeeded by Omise webhook", null);
        eventPublisher.publishEvent(new DocumentRenewalPaymentSucceededEvent(
                request.getMobileUserUuid(), request.getRequestNo()));
    }

    private void verifySignature(String rawBody, String signature, String timestamp) {
        if (webhookSecret == null || webhookSecret.trim().isEmpty()) {
            if (isProdProfile()) {
                throw new BusinessException(AppStatus.INVALID_FORMAT, "omiseSignature");
            }
            return;
        }
        if (signature == null || timestamp == null) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "omiseSignature");
        }
        try {
            byte[] secret = Base64.getDecoder().decode(webhookSecret.trim());
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] digest = mac.doFinal((timestamp + "." + rawBody)
                    .getBytes(StandardCharsets.UTF_8));
            String expected = hex(digest);
            for (String value : signature.split(",")) {
                if (MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                        value.trim().getBytes(StandardCharsets.UTF_8))) {
                    return;
                }
            }
        } catch (Exception ex) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "omiseSignature");
        }
        throw new BusinessException(AppStatus.INVALID_FORMAT, "omiseSignature");
    }

    private boolean isProdProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if (AppSys.PROFILE_PROD.equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    private JsonNode parse(String rawBody) {
        try {
            return objectMapper.readTree(rawBody);
        } catch (Exception ex) {
            throw new BusinessException(AppStatus.INVALID_FORMAT, "omiseWebhook");
        }
    }

    private String text(JsonNode node, String field) {
        return node != null && node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    private String hex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(String.format("%02x", value));
        }
        return builder.toString();
    }
}
